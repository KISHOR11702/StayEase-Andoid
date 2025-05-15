package com.example.stayeaseapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stayeaseapp.viewmodel.LoginViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder

@Composable
fun DashboardScreen(navController: NavController, email: String, loginViewModel: LoginViewModel) {
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var roomNo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(email) {
        db.collection("students")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents.first()
                    name = document.getString("name") ?: ""
                    course = document.getString("course") ?: ""
                    className = document.getString("class") ?: ""
                    roomNo = document.getString("room_no") ?: ""
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("DashboardScreen", "Error fetching data", e)
                isLoading = false
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Welcome, $name!", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))
            Text("📧 Email: $email")
            Text("📚 Course: $course")
            Text("🏫 Class: $className")
            Text("🛏️ Room No: $roomNo")

            Spacer(modifier = Modifier.height(24.dp))
            val buttonModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)

            Button(onClick = { navController.navigate("foodMenu") }, modifier = buttonModifier) {
                Text("🍽️ Food Menu")
            }

            Button(onClick = {
                navController.navigate("preorder/${email}/${name}")
            }, modifier = buttonModifier) {
                Text("📌 Preorder Meal")
            }

            Button(onClick = {
                navController.navigate("leaveform/${email}/${name}")
            }, modifier = buttonModifier) {
                Text("✈️ Leave Application")
            }

            Button(onClick = {
                val studentId = URLEncoder.encode(email, "UTF-8")
                val studentName = URLEncoder.encode(name, "UTF-8")
                navController.navigate("complaint/$studentId/$studentName")
            }, modifier = buttonModifier) {
                Text("⚠️ Complaint Module")
            }

            Button(onClick = {
                navController.navigate("profile")
            }, modifier = buttonModifier) {
                Text("👤 Profile")
            }

            Spacer(modifier = Modifier.weight(1f))

            // ✅ Proper logout that clears session
            Button(
                onClick = {
                    loginViewModel.logout() // ✅ Clear Firebase session
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("🚪 Logout", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}
