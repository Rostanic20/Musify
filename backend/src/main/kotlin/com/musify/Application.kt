package com.musify

import com.musify.core.config.AppConfig
import com.musify.core.config.EnvironmentConfig
import com.musify.database.DatabaseFactory
import com.musify.di.appModule
import com.musify.plugins.configureEnhancedRateLimiting
import com.musify.plugins.configureSecurityHeaders
import com.musify.core.monitoring.SentryConfig
import com.musify.core.storage.StorageService
import com.musify.core.tasks.ScheduledTasks
import com.musify.presentation.controller.*
import com.musify.presentation.websocket.searchAnalyticsWebSocket
import com.musify.routes.*
import com.musify.security.configureSecurity
import com.musify.core.monitoring.CloudWatchMetricsPublisher
import com.musify.core.monitoring.AlertManager
import com.musify.infrastructure.cache.EnhancedRedisCacheManager
import com.musify.infrastructure.middleware.configureErrorHandling
import com.musify.infrastructure.middleware.configureRequestIdTracking
import com.musify.infrastructure.middleware.configureInputValidation
import com.musify.infrastructure.logging.CorrelationIdInterceptor.configureCorrelationId
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.launch
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlin.system.exitProcess
import kotlinx.serialization.json.Json
import org.koin.core.error.ApplicationAlreadyStartedException
import org.koin.ktor.plugin.Koin
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.get
import java.time.Duration

fun main(args: Array<String>) {
    try {
        dotenv {
            ignoreIfMissing = true
        }.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    } catch (e: Exception) {
        println("INFO: No .env file found, using system environment variables")
    }

    try {
        EnvironmentConfig.validateForProduction()
        EnvironmentConfig.printConfiguration()
    } catch (e: IllegalStateException) {
        System.err.println("Configuration error: ${e.message}")
        exitProcess(1)
    }

    SentryConfig.initialize()

    embeddedServer(Netty, port = AppConfig.serverPort, host = AppConfig.serverHost, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val isTestEnvironment = System.getProperty("testing") == "true" ||
                           EnvironmentConfig.ENVIRONMENT == "test"

    if (!isTestEnvironment) {
        try {
            EnvironmentConfig.validateForProduction()
            EnvironmentConfig.validateSecurityForProduction()
        } catch (e: SecurityException) {
            log.error("Security validation failed: ${e.message}")
            throw e
        } catch (e: Exception) {
            log.error("Configuration validation failed: ${e.message}")
            throw e
        }
    }

    DatabaseFactory.init()

    if (System.getProperty("SENTRY_ENABLED", "true").toBoolean()) {
        with(SentryConfig) {
            configureSentry()
        }
    }

    environment.monitor.subscribe(ApplicationStarted) {
        // Initialize Redis cache
        try {
            val enhancedCacheManager = org.koin.core.context.GlobalContext.get().get<EnhancedRedisCacheManager>()
            enhancedCacheManager.initialize()

            if (EnvironmentConfig.REDIS_ENABLED) {
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        enhancedCacheManager.warmup()
                    } catch (e: Exception) {
                        log.warn("Cache warmup failed: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to initialize Redis cache: ${e.message}")
        }

        // Start scheduled tasks
        if (System.getProperty("SCHEDULED_TASKS_ENABLED", "true").toBoolean()) {
            try {
                val storageService = org.koin.core.context.GlobalContext.get().get<StorageService>()
                val sessionService = org.koin.core.context.GlobalContext.get().get<com.musify.core.streaming.StreamingSessionService>()
                val sessionCleanupTask = com.musify.core.tasks.StreamingSessionCleanupTask(sessionService)
                val scheduledTasks = ScheduledTasks(storageService, sessionCleanupTask)
                scheduledTasks.start()

                it.environment.monitor.subscribe(ApplicationStopped) {
                    scheduledTasks.stop()
                }
            } catch (e: Exception) {
                log.warn("Failed to start scheduled tasks: ${e.message}")
            }
        }

        // Start monitoring services
        if (System.getProperty("MONITORING_ENABLED", "true").toBoolean()) {
            try {
                val cloudWatchPublisher = org.koin.core.context.GlobalContext.get().get<CloudWatchMetricsPublisher>()
                val alertManager = org.koin.core.context.GlobalContext.get().get<AlertManager>()
                val databasePoolMonitor = org.koin.core.context.GlobalContext.get().get<com.musify.core.monitoring.DatabasePoolMonitor>()

                cloudWatchPublisher.start()
                alertManager.start()

                it.environment.monitor.subscribe(ApplicationStopped) {
                    cloudWatchPublisher.stop()
                    alertManager.stop()
                    databasePoolMonitor.shutdown()

                    try {
                        val enhancedCacheManager = org.koin.core.context.GlobalContext.get().get<EnhancedRedisCacheManager>()
                        enhancedCacheManager.shutdown()
                    } catch (e: Exception) {
                        log.warn("Failed to shutdown Redis cache: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                log.warn("Failed to start monitoring services: ${e.message}")
            }
        }
    }

    try {
        install(Koin) {
            modules(appModule)
        }
    } catch (e: ApplicationAlreadyStartedException) {
        // Koin already started globally, skip
    } catch (e: DuplicatePluginException) {
        // Plugin already installed, skip
    }

    if (pluginOrNull(ContentNegotiation) == null) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    if (pluginOrNull(CORS) == null) {
        install(CORS) {
            val allowedHosts = EnvironmentConfig.CORS_ALLOWED_HOSTS

            if (EnvironmentConfig.ENVIRONMENT == "development") {
                allowHost("localhost:3000")
                allowHost("localhost:8080")
                allowHost("127.0.0.1:3000")
                allowHost("127.0.0.1:8080")

                allowedHosts.forEach { origin ->
                    if (origin != "*" && (origin.contains("localhost") || origin.contains("127.0.0.1"))) {
                        if (origin.startsWith("http://") || origin.startsWith("https://")) {
                            allowHost(origin.substringAfter("://"), schemes = listOf(origin.substringBefore("://")))
                        } else {
                            allowHost(origin, schemes = listOf("http", "https"))
                        }
                    }
                }
            } else {
                if (allowedHosts.contains("*") && EnvironmentConfig.IS_PRODUCTION) {
                    throw SecurityException("CORS wildcard (*) is not allowed in production")
                }

                allowedHosts.forEach { origin ->
                    if (origin.isNotBlank() && origin != "*") {
                        if (origin.startsWith("http://") || origin.startsWith("https://")) {
                            allowHost(origin.substringAfter("://"), schemes = listOf(origin.substringBefore("://")))
                        } else {
                            allowHost(origin, schemes = listOf("https"))
                        }
                    }
                }
            }

            allowHeader("Authorization")
            allowHeader("Content-Type")
            allowHeader("X-Requested-With")
            allowHeader("X-Network-Type")

            allowMethod(io.ktor.http.HttpMethod.Options)
            allowMethod(io.ktor.http.HttpMethod.Get)
            allowMethod(io.ktor.http.HttpMethod.Post)
            allowMethod(io.ktor.http.HttpMethod.Put)
            allowMethod(io.ktor.http.HttpMethod.Delete)
            allowMethod(io.ktor.http.HttpMethod.Patch)

            allowCredentials = true
            maxAgeInSeconds = 3600
        }
    }

    if (pluginOrNull(PartialContent) == null) {
        install(PartialContent)
    }

    if (pluginOrNull(WebSockets) == null) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = 65536 // 64KB
            masking = false
        }
    }

    configureSecurity()
    configureEnhancedRateLimiting()
    configureSecurityHeaders()
    configureInputValidation()
    configureErrorHandling()
    configureRequestIdTracking()
    configureCorrelationId()

    routing {
        authController()
        songController()
        playlistController()
        subscriptionController()
        stripeWebhookController()
        socialController(
            followUserUseCase = get(),
            unfollowUserUseCase = get(),
            followArtistUseCase = get(),
            unfollowArtistUseCase = get(),
            followPlaylistUseCase = get(),
            unfollowPlaylistUseCase = get(),
            getFollowersUseCase = get(),
            getFollowingUseCase = get(),
            getFollowedArtistsUseCase = get(),
            getFollowedPlaylistsUseCase = get(),
            getUserProfileUseCase = get(),
            getActivityFeedUseCase = get(),
            shareItemUseCase = get(),
            getInboxUseCase = get(),
            markAsReadUseCase = get(),
            getFollowStatsUseCase = get(),
            jwtService = get()
        )
        oAuth2Controller()
        searchController()
        searchAnalyticsController()
        searchABTestingController()
        searchPerformanceController()
        searchAnalyticsWebSocket()
        healthController()
        monitoringController()
        monitoringDashboardController()
        bufferingController()
        recommendationController()
        interactionController()
        queueController()
        artistController()

        userRoutes()
        offlineDownloadRoutes()
        albumRoutes()
        uploadRoutes()
        recommendationRoutes()
        webSocketRoutes()
        podcastRoutes()
        adminRoutes()
        analyticsRoutes()
    }
}
