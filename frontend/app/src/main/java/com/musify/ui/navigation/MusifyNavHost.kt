package com.musify.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.musify.ui.screens.auth.LoginScreen
import com.musify.ui.screens.auth.RegisterScreen
import com.musify.ui.screens.auth.VerificationScreen
import com.musify.utils.TokenManager
import kotlinx.coroutines.delay

@Composable
fun MusifyNavHost(
    navController: NavHostController,
    tokenManager: TokenManager = hiltViewModel<SplashViewModel>().tokenManager
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        navigation(
            startDestination = Routes.LOGIN,
            route = Routes.AUTH
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onNavigateToHome = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEmailVerification = { email ->
                        navController.navigate(Routes.emailVerification(email))
                    }
                )
            }

            composable(
                route = Routes.EMAIL_VERIFICATION,
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val verificationData = backStackEntry.arguments?.getString("email") ?: "email:"
                VerificationScreen(
                    verificationData = verificationData,
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.EMAIL_VERIFICATION) { inclusive = true }
                        }
                    },
                    onVerificationSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.EMAIL_VERIFICATION) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.FORGOT_PASSWORD) {
                com.musify.ui.screens.auth.ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        navigation(
            startDestination = Routes.HOME,
            route = Routes.MAIN
        ) {
            composable(Routes.HOME) {
                MainScreen(navController = navController)
            }
            composable(
                route = Routes.SONG_DETAIL,
                arguments = listOf(navArgument("songId") { type = NavType.IntType })
            ) {
                com.musify.ui.screens.detail.SongDetailScreen(navController)
            }
            composable(
                route = Routes.ALBUM_DETAIL,
                arguments = listOf(navArgument("albumId") { type = NavType.IntType })
            ) {
                com.musify.ui.screens.detail.AlbumDetailScreen(navController)
            }
            composable(
                route = Routes.ARTIST_DETAIL,
                arguments = listOf(navArgument("artistId") { type = NavType.IntType })
            ) {
                com.musify.ui.screens.detail.ArtistDetailScreen(navController)
            }
            composable(
                route = Routes.PLAYLIST_DETAIL,
                arguments = listOf(navArgument("playlistId") { type = NavType.IntType })
            ) {
                com.musify.ui.screens.detail.PlaylistDetailScreen(navController)
            }
            composable(Routes.NOW_PLAYING) {
                com.musify.ui.screens.player.NowPlayingScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQueue = { navController.navigate(Routes.QUEUE) }
                )
            }
            composable(Routes.QUEUE) {
                com.musify.ui.screens.player.QueueScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                com.musify.ui.screens.settings.SettingsScreen(navController)
            }
            composable(Routes.SUBSCRIPTION) {
                com.musify.ui.screens.subscription.SubscriptionScreen(navController)
            }
        }
    }
}

@Composable
private fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        delay(1500)

        if (viewModel.isLoggedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Musify",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
