package com.example.stayeaseapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stayeaseapp.ui.DashboardScreen
import com.example.stayeaseapp.ui.LoginScreen
import com.example.stayeaseapp.ui.theme.StayEaseAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StayEaseAppTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(navController,  this@MainActivity as Context) // ✅ Pass Correct Context
                    }


                    composable(
                        "dashboard/{name}/{email}/{course}/{className}/{roomNo}",
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("email") { type = NavType.StringType },
                            navArgument("course") { type = NavType.StringType },
                            navArgument("className") { type = NavType.StringType },
                            navArgument("roomNo") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        DashboardScreen(
                            navController = navController,
                            name = backStackEntry.arguments?.getString("name") ?: "",
                            email = backStackEntry.arguments?.getString("email") ?: "",
                            course = backStackEntry.arguments?.getString("course") ?: "",
                            className = backStackEntry.arguments?.getString("className") ?: "",
                            roomNo = backStackEntry.arguments?.getString("roomNo") ?: ""
                        )
                    }
                }
            }
        }
    }
}
