package com.example.ponenciapp.navigation

import androidx.compose.ui.graphics.vector.ImageVector

// Modelo para los items del BottomBar
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)