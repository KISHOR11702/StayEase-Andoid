package com.example.stayeaseapp.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPreordersScreen(
    navController: NavController,
    studentEmail: String,
    studentName: String
) {
    val db = FirebaseFirestore.getInstance()
    var preorders by remember { mutableStateOf(listOf<PreorderItem>()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    suspend fun refreshPreorders() {
        isLoading = true
        try {
            val snapshot = db.collection("preorderslist")
                .whereEqualTo("studentId", studentEmail)
                .get()
                .await()

            preorders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PreorderItem::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.orderTime }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to fetch orders ❌", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(true) {
        refreshPreorders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Preorders") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                preorders.isEmpty() -> {
                    Text(
                        text = "No preorders found!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(preorders, key = { it.id }) { preorder ->
                            PreorderItemCard(
                                preorder = preorder,
                                onOrderDeleted = { deletedOrderId ->
                                    preorders = preorders.filterNot { it.id == deletedOrderId }
                                }
                            )
                            Divider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreorderItemCard(
    preorder: PreorderItem,
    onOrderDeleted: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var showQR by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🍽️ Food: ${preorder.food}", style = MaterialTheme.typography.titleMedium)
            Text("📅 Day: ${preorder.day}")
            Text("🔢 Quantity: ${preorder.quantity}")
            Text("🕒 Ordered at: ${preorder.orderTime}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showConfirmDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel Order", color = MaterialTheme.colorScheme.onError)
            }

            Button(
                onClick = { showQR = !showQR },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showQR) "Hide QR Code" else "View QR Code")
            }

            AnimatedVisibility(
                visible = showQR,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                val qrData = """
                    Order ID: ${preorder.id}
                    Student: ${preorder.studentName}
                    Food: ${preorder.food}
                    Day: ${preorder.day}
                    Quantity: ${preorder.quantity}
                    Time: ${preorder.orderTime}
                """.trimIndent()

                val qrBitmap = generatePreorderQRCode(qrData)

                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Cancel Order?") },
            text = { Text("Are you sure you want to cancel and delete this preorder? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                db.collection("preorderslist")
                                    .document(preorder.id)
                                    .delete()
                                    .await()

                                Toast.makeText(context, "Order cancelled ✅", Toast.LENGTH_SHORT).show()
                                onOrderDeleted(preorder.id) // ✅ Remove it locally
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to delete ❌", Toast.LENGTH_SHORT).show()
                                e.printStackTrace()
                            } finally {
                                showConfirmDialog = false
                            }
                        }
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

fun generatePreorderQRCode(text: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
