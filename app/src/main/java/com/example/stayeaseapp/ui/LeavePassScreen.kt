package com.example.stayeaseapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class LeaveRequest(
    val roomNo: String = "",
    val block: String = "",
    val reason: String = "",
    val fromDate: String = "",
    val toDate: String = "",
    val timestamp: Long = 0L
)

@Composable
fun LeavePassScreen(navController: NavController, email: String, name: String) {
    val db = FirebaseFirestore.getInstance()
    var leaveRequests by remember { mutableStateOf<List<LeaveRequest>>(emptyList()) }

    LaunchedEffect(true) {
        db.collection("leave_requests").document(email)
            .collection("requests")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                leaveRequests = result.documents.mapNotNull { it.toObject(LeaveRequest::class.java) }
            }
    }

    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    val today = remember { Calendar.getInstance().time }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Pass") },
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
                .fillMaxSize()
        ) {
            Text("PS College of Technology, Coimbatore", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(16.dp))

            val (activeLeaves, pastLeaves) = leaveRequests.partition {
                try {
                    val toDate = dateFormat.parse(it.toDate)
                    toDate?.after(today) ?: false
                } catch (e: Exception) {
                    false
                }
            }

            if (activeLeaves.isNotEmpty()) {
                val latest = activeLeaves.first()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🧑 Name: $name")
                        Text("🏠 Room: ${latest.roomNo}, Block: ${latest.block}")
                        Text("📅 From: ${latest.fromDate} → To: ${latest.toDate}")
                        Text("📝 Reason: ${latest.reason}")
                        Text("✅ Leave Approved")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Previous Leave History", style = MaterialTheme.typography.subtitle1)

            val allPrevious = pastLeaves + activeLeaves.drop(1)

            if (allPrevious.isNotEmpty()) {
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(allPrevious) { leave ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📅 ${leave.fromDate} to ${leave.toDate}")
                                Text("🏠 Room: ${leave.roomNo}, Block: ${leave.block}")
                                Text("📝 ${leave.reason}")
                            }
                        }
                    }
                }
            } else {
                Text("No previous leave records.")
            }
        }
    }
}
