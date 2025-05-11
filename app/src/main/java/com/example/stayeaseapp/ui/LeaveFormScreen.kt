package com.example.stayease.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LeaveFormScreen(navController: NavController, email: String, name: String) {
    val context = LocalContext.current
    val roomNo = remember { mutableStateOf("") }
    val block = remember { mutableStateOf("") }
    val reason = remember { mutableStateOf("") }
    val fromDate = remember { mutableStateOf("") }
    val toDate = remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time)
                onDateSelected(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Form") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Name: $name", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = roomNo.value,
                onValueChange = { roomNo.value = it },
                label = { Text("Room No") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = block.value,
                onValueChange = { block.value = it },
                label = { Text("Block") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fromDate.value,
                onValueChange = {},
                label = { Text("From Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker { fromDate.value = it } }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick From Date")
                    }
                }
            )

            OutlinedTextField(
                value = toDate.value,
                onValueChange = {},
                label = { Text("To Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker { toDate.value = it } }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick To Date")
                    }
                }
            )

            OutlinedTextField(
                value = reason.value,
                onValueChange = { reason.value = it },
                label = { Text("Reason") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (roomNo.value.isNotEmpty() && block.value.isNotEmpty()
                        && fromDate.value.isNotEmpty() && toDate.value.isNotEmpty()
                        && reason.value.isNotEmpty()
                    ) {
                        val leaveData = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "roomNo" to roomNo.value,
                            "block" to block.value,
                            "reason" to reason.value,
                            "fromDate" to fromDate.value,
                            "toDate" to toDate.value,
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("leave_requests").document(email)
                            .collection("requests")
                            .add(leaveData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Leave Submitted!", Toast.LENGTH_SHORT).show()
                                navController.navigate("leavepass/$email/$name")
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Submission failed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate("leavepass/$email/$name") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Pass")
            }
        }
    }
}
