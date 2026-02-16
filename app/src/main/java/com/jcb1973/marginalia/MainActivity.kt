package com.jcb1973.marginalia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jcb1973.marginalia.ui.navigation.NavGraph
import com.jcb1973.marginalia.ui.navigation.Screen
import com.jcb1973.marginalia.ui.theme.MarginaliaTheme
import dagger.hilt.android.AndroidEntryPoint

private enum class BottomTab(
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Default.Home, "tabHome"),
    ADD("Add", Icons.Default.Add, "tabAdd"),
    LIBRARY("Library", Icons.Default.LocalLibrary, "tabLibrary")
}

private val tabRoutes = setOf(Screen.Home.route, "library", "library?status={status}")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarginaliaTheme {
                val navController = rememberNavController()
                MainScaffold(navController)
            }
        }
    }
}

@Composable
private fun MainScaffold(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        val selected = when (tab) {
                            BottomTab.HOME -> currentRoute == Screen.Home.route
                            BottomTab.LIBRARY -> currentRoute?.startsWith("library") == true
                            BottomTab.ADD -> false
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                when (tab) {
                                    BottomTab.HOME -> navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    BottomTab.ADD -> navController.navigate(Screen.BookForm.createRoute()) {
                                        launchSingleTop = true
                                    }
                                    BottomTab.LIBRARY -> navController.navigate(Screen.Library.createRoute()) {
                                        popUpTo(Screen.Home.route)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
