package com.example.stayeaseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.stayeaseapp.ui.*
import com.example.stayeaseapp.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(navController, loginViewModel)
                }

                composable(
                    "dashboard/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: "unknown"
                    DashboardScreen(navController, email)
                }

                composable("foodMenu") {
                    FoodMenuScreen(navController)
                }
                composable(
                    "preorder/{email}/{name}",
                    arguments = listOf(
                        navArgument("email") { type = NavType.StringType },
                        navArgument("name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    PreorderScreen(navController, email, name)
                }
                composable(
                    "qrSuccess/{qrData}",
                    arguments = listOf(navArgument("qrData") { type = NavType.StringType })
                ) { backStackEntry ->
                    val qrData = backStackEntry.arguments?.getString("qrData") ?: ""
                    QRSuccessScreen(navController, qrData)
                }
                composable(
                    "viewPreorders/{email}/{name}",
                    arguments = listOf(
                        navArgument("email") { type = NavType.StringType },
                        navArgument("name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    ViewPreordersScreen(navController, email, name)
                }


                composable("complaint/{studentId}/{studentName}") { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getString("studentId") ?: "unknown"
                    val studentName = backStackEntry.arguments?.getString("studentName") ?: "unknown"
                    ComplaintScreen(navController, studentId, studentName)
                }

            }
        }
    }
}
