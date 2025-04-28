package com.example.stayeaseapp.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ComplaintScreen(navController: NavController, studentId: String, studentName: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Complaint") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Complaint Category", style = MaterialTheme.typography.h6)
            DropdownMenuComponent(
                selectedCategory = category,
                onCategorySelected = { category = it }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Complaint Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                maxLines = 15
            )

            Button(
                onClick = {
                    if (category.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    submitComplaint(db, studentId, studentName, category, description) { success ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "✅ Complaint submitted", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "❌ Submission failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                Text(text = if (isSubmitting) "Submitting..." else "Submit Complaint")
            }
        }
    }
}

@Composable
fun DropdownMenuComponent(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("Wi-Fi", "Water", "Maintenance", "Electricity", "Others")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(text = selectedCategory.ifBlank { "Select Category" })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(onClick = {
                    onCategorySelected(category)
                    expanded = false
                }) {
                    Text(text = category)
                }
            }
        }
    }
}

private fun submitComplaint(
    db: FirebaseFirestore,
    studentId: String,
    studentName: String,
    category: String,
    description: String,
    callback: (Boolean) -> Unit
) {
    val complaintRef = db.collection("complaints").document()

    val complaint = hashMapOf(
        "complaintId" to complaintRef.id,
        "studentId" to studentId,
        "studentName" to studentName,
        "category" to category,
        "description" to description,
        "status" to "Pending",
        "timestamp" to System.currentTimeMillis()
    )

    complaintRef.set(complaint)
        .addOnSuccessListener {
            Log.d("Complaint", "Complaint submitted successfully: ${complaintRef.id}")
            callback(true)
        }
        .addOnFailureListener { e ->
            Log.e("Complaint", "Error submitting complaint", e)
            callback(false)
        }
}
