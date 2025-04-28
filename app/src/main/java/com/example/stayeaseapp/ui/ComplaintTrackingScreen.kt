package com.example.stayeaseapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

data class Complaint(
    val id: String,
    val title: String,
    val description: String,
    val status: String
)

@Composable
fun ComplaintTrackingScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("complaints")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ComplaintTracking", "Error fetching complaints", error)
                        return@addSnapshotListener
                    }
                    val fetchedComplaints = snapshot?.documents?.mapNotNull { doc ->
                        Complaint(
                            id = doc.id,
                            title = doc.getString("title") ?: "No Title",
                            description = doc.getString("description") ?: "No Description",
                            status = doc.getString("status") ?: "Pending"
                        )
                    } ?: emptyList()
                    complaints = fetchedComplaints
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Complaints") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (complaints.isEmpty()) {
                Text("No complaints found.", style = MaterialTheme.typography.body1)
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(complaints) { complaint ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = complaint.title, style = MaterialTheme.typography.h6)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = complaint.description, style = MaterialTheme.typography.body1)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Status: ${complaint.status}",
                                    style = MaterialTheme.typography.body2,
                                    color = if (complaint.status == "Resolved") MaterialTheme.colors.primary else MaterialTheme.colors.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
