package com.example.stayeaseapp.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

@Composable
fun ComplaintSubmissionScreen(navController: NavController, studentId: String, studentName: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Complaint") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Complaint Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Complaint Description") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    submitComplaint(db, studentId, studentName, title, description) { success, complaintId ->
                        if (success && complaintId != null) {
                            sendComplaintToMySQL(studentId, studentName, title, description, complaintId) { mysqlSuccess ->
                                isSubmitting = false
                                if (mysqlSuccess) {
                                    Toast.makeText(context, "Complaint Submitted Successfully!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Sync to Server Failed!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            isSubmitting = false
                            Toast.makeText(context, "Submission Failed! Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                Text("Submit Complaint")
            }
        }
    }
}

fun submitComplaint(
    db: FirebaseFirestore,
    studentId: String,
    studentName: String,
    title: String,
    description: String,
    callback: (Boolean, String?) -> Unit
) {
    val complaintRef = db.collection("complaints").document()
    val complaintData = hashMapOf(
        "studentId" to studentId,
        "studentName" to studentName,
        "title" to title,
        "description" to description,
        "status" to "Pending",
        "timestamp" to System.currentTimeMillis()
    )

    complaintRef.set(complaintData)
        .addOnSuccessListener {
            Log.d("ComplaintSubmission", "Complaint successfully added to Firebase")
            callback(true, complaintRef.id)
        }
        .addOnFailureListener { e ->
            Log.e("ComplaintSubmission", "Error adding complaint", e)
            callback(false, null)
        }
}

fun sendComplaintToMySQL(studentId: String, studentName: String, title: String, description: String, firebaseId: String, callback: (Boolean) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            val json = JSONObject().apply {
                put("studentId", studentId)
                put("studentName", studentName)
                put("title", title)
                put("description", description)
                put("firebaseId", firebaseId)
            }
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
            val request = Request.Builder()
                .url("https://yourserver.com/api/complaints") // Replace with your API endpoint
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            withContext(Dispatchers.Main) {
                callback(response.isSuccessful)
            }
        } catch (e: Exception) {
            Log.e("ComplaintSubmission", "Error syncing to MySQL", e)
            withContext(Dispatchers.Main) {
                callback(false)
            }
        }
    }
}
