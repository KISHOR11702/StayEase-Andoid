package com.example.stayeaseapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DashboardScreen(
    navController: NavController, // ✅ Add this parameter
    name: String,
    email: String,
    course: String,
    className: String,
    roomNo: String
) {
    Button(onClick = { navController.navigate("login") }) {  // ✅ Now this works!
        Text("Logout")
    }
}
