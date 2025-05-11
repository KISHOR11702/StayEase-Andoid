package com.example.stayeaseapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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

        // 🔔 Subscribe to "allStudents" topic
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

                // ✅ Leave Form Screen
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

                // ✅ Leave Pass Screen
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
