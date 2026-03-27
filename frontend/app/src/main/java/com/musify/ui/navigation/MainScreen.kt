package com.musify.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
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
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
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
                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(title = { Text("Analytics") })
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analytics",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Coming Soon",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
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