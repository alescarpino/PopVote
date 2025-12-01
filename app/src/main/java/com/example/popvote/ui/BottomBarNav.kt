
package com.example.popvote.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding

@Composable
fun BottomBarNav(
    navController: NavHostController,
    onAddClick: () -> Unit
) {
    val items = listOf(
        BottomNavItem("Library", Icons.Filled.Folder, "library"),
        BottomNavItem("All Films", Icons.Filled.Movie, "all_films"),
        BottomNavItem("Wishlist", Icons.Filled.Favorite, "wishlist"),
        BottomNavItem("Add", Icons.Filled.AddCircle, "add")
    )

    // ✅ aktuelle Route beobachten (reagiert auf Navigation)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "library"

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    // ✅ Hintergrund + Schatten beim aktiven Icon
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = if (selected) {
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(Color(0xFFE8EAF6)) // leichtes helles Lila/Gray
                                .shadow(6.dp, MaterialTheme.shapes.small)
                                .padding(8.dp)
                        } else {
                            Modifier
                        }
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    if (item.route == "add") {
                        // ✅ Add nur im Library-Tab aktiv
                        if (currentRoute == "library") {
                            onAddClick()
                        } else {
                            // Optional: Snackbar/Toast einbauen
                            // println("Add ist nur im Library-Tab verfügbar")
                        }
                    } else {
                        // ✅ Navigation zum Ziel-Tab
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    // Wir nutzen eigenen Hintergrund → Indikator transparent
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)
