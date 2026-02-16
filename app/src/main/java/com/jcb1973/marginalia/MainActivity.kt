package com.jcb1973.marginalia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.jcb1973.marginalia.ui.navigation.NavGraph
import com.jcb1973.marginalia.ui.theme.MarginaliaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarginaliaTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
