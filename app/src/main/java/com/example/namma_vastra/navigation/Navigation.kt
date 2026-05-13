package com.example.namma_vastra.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Trends : Screen("trends", "Trends", Icons.AutoMirrored.Filled.TrendingUp)
    object Gallery : Screen("gallery", "Gallery", Icons.Default.Image)
    object Pricing : Screen("pricing", "Pricing", Icons.Default.Calculate)
    object Story : Screen("story", "Story", Icons.Default.AutoStories)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Trends,
    Screen.Gallery,
    Screen.Pricing,
    Screen.Story,
    Screen.Profile
)
