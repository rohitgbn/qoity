package com.example.quoteverse.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quoteverse.data.repository.PostRepository
import com.example.quoteverse.feature.auth.AuthViewModel
import com.example.quoteverse.feature.auth.LoginScreen
import com.example.quoteverse.feature.auth.RegisterScreen
import com.example.quoteverse.feature.comments.CommentsScreen
import com.example.quoteverse.feature.comments.CommentsViewModel
import com.example.quoteverse.feature.createpost.CreatePostScreen
import com.example.quoteverse.feature.createpost.CreatePostViewModel
import com.example.quoteverse.feature.explore.CategoryPostsScreen
import com.example.quoteverse.feature.explore.ExploreScreen
import com.example.quoteverse.feature.explore.ExploreViewModel
import com.example.quoteverse.feature.home.HomeScreen
import com.example.quoteverse.feature.home.HomeViewModel
import com.example.quoteverse.feature.moderation.CommunityGuidelinesScreen
import com.example.quoteverse.feature.notifications.NotificationsScreen
import com.example.quoteverse.feature.notifications.NotificationsViewModel
import com.example.quoteverse.feature.profile.EditProfileScreen
import com.example.quoteverse.feature.profile.ProfileScreen
import com.example.quoteverse.feature.profile.ProfileViewModel
import com.example.quoteverse.feature.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    postRepository: PostRepository,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible = bottomNavItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (authViewModel.isUserLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth destinations
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Bottom Navigation Destinations
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToComments = { postId ->
                        navController.navigate(Screen.Comments.createRoute(postId))
                    },
                    onNavigateToUserProfile = { uid ->
                        navController.navigate(Screen.UserProfile.createRoute(uid))
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToExplore = {
                        navController.navigate(Screen.Explore.route)
                    }
                )
            }

            composable(Screen.Explore.route) {
                val exploreViewModel: ExploreViewModel = hiltViewModel()
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onNavigateToCategory = { catId, catName ->
                        navController.navigate(Screen.CategoryPosts.createRoute(catId, catName))
                    },
                    onNavigateToUserProfile = { uid ->
                        navController.navigate(Screen.UserProfile.createRoute(uid))
                    },
                    onNavigateToComments = { postId ->
                        navController.navigate(Screen.Comments.createRoute(postId))
                    }
                )
            }

            composable(Screen.CreatePost.route) {
                val createPostViewModel: CreatePostViewModel = hiltViewModel()
                CreatePostScreen(
                    viewModel = createPostViewModel,
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Notifications.route) {
                val notifViewModel: NotificationsViewModel = hiltViewModel()
                NotificationsScreen(
                    viewModel = notifViewModel,
                    onNavigateToUserProfile = { uid ->
                        navController.navigate(Screen.UserProfile.createRoute(uid))
                    },
                    onNavigateToComments = { postId ->
                        navController.navigate(Screen.Comments.createRoute(postId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToComments = { postId ->
                        navController.navigate(Screen.Comments.createRoute(postId))
                    }
                )
            }

            // Detail Destinations
            composable(
                route = Screen.Comments.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val commentsViewModel: CommentsViewModel = hiltViewModel()
                CommentsScreen(
                    postId = postId,
                    viewModel = commentsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToUserProfile = { uid ->
                        navController.navigate(Screen.UserProfile.createRoute(uid))
                    }
                )
            }

            composable(
                route = Screen.UserProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    userId = userId,
                    viewModel = profileViewModel,
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToComments = { postId ->
                        navController.navigate(Screen.Comments.createRoute(postId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EditProfile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CategoryPosts.route,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                CategoryPostsScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    postRepository = postRepository,
                    currentUserId = authViewModel.currentUserId ?: "",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToComments = { postId ->
                        navController.navigate(Screen.Comments.createRoute(postId))
                    },
                    onNavigateToUserProfile = { uid ->
                        navController.navigate(Screen.UserProfile.createRoute(uid))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToGuidelines = {
                        navController.navigate(Screen.Guidelines.route)
                    }
                )
            }

            composable(Screen.Guidelines.route) {
                CommunityGuidelinesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
