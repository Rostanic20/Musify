package com.musify.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.musify.R
import com.musify.domain.entity.User
import com.musify.ui.screens.artist.dashboard.ArtistDashboardScreen
import com.musify.ui.screens.artist.songs.ArtistSongsScreen
import com.musify.ui.screens.artist.upload.UploadSongScreen
import com.musify.ui.screens.user.library.LibraryScreen
import com.musify.ui.screens.profile.ProfileScreen
import com.musify.ui.components.MiniPlayer
import com.musify.ui.screens.user.search.SearchScreen

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val user = state.user
    
    LaunchedEffect(state.shouldNavigateToLogin) {
        if (state.shouldNavigateToLogin) {
            navController.navigate(Routes.AUTH) {
                popUpTo(Routes.MAIN) { inclusive = true }
            }
        }
    }
    
    val musicPlayerManager = viewModel.musicPlayerManager

    when {
        state.shouldNavigateToLogin -> { }
        user != null -> {
            if (user.isArtist) {
                ArtistMainScreen(navController, user, musicPlayerManager)
            } else {
                UserMainScreen(navController, user, musicPlayerManager)
            }
        }
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Failed to load user data")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    }) {
                        Text("Go to Login")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserMainScreen(
    navController: NavController,
    user: User,
    musicPlayerManager: com.musify.player.MusicPlayerManager
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val bottomNavItems = listOf(
        BottomNavItem("home", stringResource(R.string.home), Icons.Default.Home),
        BottomNavItem("search", stringResource(R.string.search), Icons.Default.Search),
        BottomNavItem("library", stringResource(R.string.library), Icons.Default.LibraryMusic),
        BottomNavItem("profile", stringResource(R.string.profile), Icons.Default.Person)
    )
    
    Scaffold(
        bottomBar = {
            Column {
                MiniPlayer(
                    musicPlayerManager = musicPlayerManager,
                    onTap = { navController.navigate(Routes.NOW_PLAYING) },
                    onNext = { musicPlayerManager.next() }
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            ),
                            onClick = {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                com.musify.ui.screens.user.home.HomeScreen(navController)
            }
            composable("search") { SearchScreen(navController) }
            composable("library") { LibraryScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
        }
    }
}

@Composable
private fun ArtistMainScreen(
    navController: NavController,
    user: User,
    musicPlayerManager: com.musify.player.MusicPlayerManager
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val bottomNavItems = listOf(
        BottomNavItem("artist_dashboard", "Dashboard", Icons.Default.Dashboard),
        BottomNavItem("artist_songs", "My Songs", Icons.Default.MusicNote),
        BottomNavItem("artist_analytics", "Analytics", Icons.Default.Analytics),
        BottomNavItem("profile", stringResource(R.string.profile), Icons.Default.Person)
    )
    
    Scaffold(
        bottomBar = {
            Column {
                MiniPlayer(
                    musicPlayerManager = musicPlayerManager,
                    onTap = { navController.navigate(Routes.NOW_PLAYING) },
                    onNext = { musicPlayerManager.next() }
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            ),
                            onClick = {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "artist_dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("artist_dashboard") {
                ArtistDashboardScreen(bottomNavController)
            }
            composable("artist_songs") {
                ArtistSongsScreen(
                    navController = bottomNavController,
                    rootNavController = navController
                )
            }
            composable("artist_analytics") {
                @OptIn(ExperimentalMaterial3Api::class)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Analytics") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("0", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                    Text("Total Plays", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("0", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                    Text("Songs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("0", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                    Text("Followers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("0", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                    Text("Likes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Performance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Monthly Listeners", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("0 this month", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(16.dp))
                                @Suppress("DEPRECATION")
                                LinearProgressIndicator(
                                    progress = 0f,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Top Song", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("No songs uploaded yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Engagement Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("0%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Likes per play", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
            composable("artist_upload") { 
                UploadSongScreen(bottomNavController)
            }
            composable("profile") { ProfileScreen(navController) }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)