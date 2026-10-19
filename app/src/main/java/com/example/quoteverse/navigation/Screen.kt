package com.example.quoteverse.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // Bottom navigation destinations
    object Home : Screen("home")
    object Explore : Screen("explore")
    object CreatePost : Screen("create_post")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")

    // Auth destinations
    object Login : Screen("login")
    object Register : Screen("register")

    // Detail destinations
    object Comments : Screen("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object EditProfile : Screen("edit_profile")
    object CategoryPosts : Screen("category_posts/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: String, categoryName: String) = "category_posts/$categoryId/$categoryName"
    }
    object Settings : Screen("settings")
    object Guidelines : Screen("guidelines")
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Explore, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    BottomNavItem(Screen.CreatePost, "Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    BottomNavItem(Screen.Notifications, "Activity", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)
