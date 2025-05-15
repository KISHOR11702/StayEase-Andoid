package com.example.stayeaseapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.stayease.ui.LeaveFormScreen
import com.example.stayeaseapp.ui.*
import com.example.stayeaseapp.viewmodel.LoginViewModel
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().subscribeToTopic("allStudents")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to allStudents topic")
                } else {
                    Log.e("FCM", "Subscription failed: ${task.exception?.message}")
                }
            }

        setContent {
            val navController = rememberNavController()
            val loginState by loginViewModel.loginState.collectAsState()

            /**
             * Automatically navigate on login/logout.
             */
            LaunchedEffect(loginState) {
                loginState?.let { user ->
                    user.email?.let { email ->
                        navController.navigate("dashboard/$email") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } ?: run {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(navController, loginViewModel)
                }

                composable("forgot_password") {
                    ForgotPasswordScreen(navController)
                }

                composable(
                    "dashboard/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: "unknown"
                    DashboardScreen(navController, email, loginViewModel)
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

                composable(
                    "leaveform/{email}/{name}",
                    arguments = listOf(
                        navArgument("email") { type = NavType.StringType },
                        navArgument("name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    LeaveFormScreen(navController, email, name)
                }

                composable(
                    "leavepass/{email}/{name}",
                    arguments = listOf(
                        navArgument("email") { type = NavType.StringType },
                        navArgument("name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    LeavePassScreen(navController, email, name)
                }
            }
        }
    }
}
