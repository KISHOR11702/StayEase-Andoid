package com.example.stayeaseapp.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.stayeaseapp.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navController: NavController, context: Context, viewModel: LoginViewModel = viewModel())  {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current  // ✅ Use LocalContext.current instead of passing context manually

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        Button(onClick = {
            viewModel.login(email, password) { response ->
                if (response != null) {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()

                    // ✅ Ensure all parameters match the navigation destination
                    navController.navigate("dashboard/${response.student.name}/${response.student.email}/${response.student.course}/ClassA/${response.student.room_no}")
                } else {
                    Toast.makeText(context, "Login Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("Login")
        }
    }
}
