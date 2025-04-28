// 📁 File: ui/PreorderScreen.kt
package com.example.stayeaseapp.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text

import java.util.*

// Data class for menu item from preorders collection
data class MenuItem(
    val id: String = "",
    val createdAt: Timestamp? = null, // ← use Firebase Timestamp
    val day: String = "",
    val deadline: String = "",
    val food: String = "",
    val imageUrl: String = ""
)

// Data class for student's order in preorderslist collection
data class PreorderItem(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val menuItemId: String = "",
    val food: String = "",
    val day: String = "",
    val quantity: Int = 1,
    val orderTime: String = "",
    val status: String = "pending"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreorderScreen(navController: NavController, studentEmail: String, studentName: String) {
    val context = LocalContext.current
    var menuItems by remember { mutableStateOf(listOf<MenuItem>()) }
    var preorderItems by remember { mutableStateOf(listOf<PreorderItem>()) }
    val db = FirebaseFirestore.getInstance()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preorder Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Everything else inside here (your previous Column content)
        }
    }

    // Function to refresh orders
    val refreshOrders = {
        db.collection("preorderslist")
            .whereEqualTo("studentId", studentEmail)
            .orderBy("orderTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                preorderItems = snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(PreorderItem::class.java)
                    item?.copy(id = doc.id)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to refresh orders", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(true) {
        try {
            // Fetch menu items from preorders collection
            val menuSnapshot = db.collection("preorders")
                .get()
                .await()

            menuItems = menuSnapshot.documents.mapNotNull { doc ->
                val item = doc.toObject<MenuItem>()
                item?.copy(id = doc.id)
            }

            // Initial fetch of orders
            refreshOrders()
        } catch (e: Exception) {
            Log.e("PreorderScreen", "Error fetching data", e)
            Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Available Menu Items",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(menuItems) { menuItem ->
                MenuItemCard(
                    menuItem = menuItem,
                    studentEmail = studentEmail,
                    studentName = studentName,
                    db = db,
                    navController = navController,
                    onOrderPlaced = { refreshOrders() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Preorders",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(preorderItems) { item ->
                PreorderCard(
                    item = item,
                    db = db,
                    onOrderCancelled = { refreshOrders() }
                )
            }
        }
    }
}

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    studentEmail: String,
    studentName: String,
    db: FirebaseFirestore,
    navController: NavController,
    onOrderPlaced: () -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Display menu item image from Cloudinary
            Image(
                painter = rememberAsyncImagePainter(menuItem.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🍽️ ${menuItem.food}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "📅 ${menuItem.day}", fontSize = 14.sp)
            Text(text = "⏰ Deadline: ${menuItem.deadline}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Quantity:", fontSize = 16.sp)
                Button(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                Text("$quantity", fontSize = 18.sp)
                Button(onClick = { if (quantity < 5) quantity++ }) { Text("+") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date())

                    val preorder = hashMapOf(
                        "studentId" to studentEmail,
                        "studentName" to studentName,
                        "menuItemId" to menuItem.id,
                        "food" to menuItem.food,
                        "day" to menuItem.day,
                        "quantity" to quantity,
                        "orderTime" to currentTime,
                        "status" to "pending"
                    )

                    // Add to preorderslist collection (creates it if doesn't exist)
                    db.collection("preorderslist")
                        .add(preorder)
                        .addOnSuccessListener { documentRef ->
                            // Create QR code data
                            val qrData = """
                                Order ID: ${documentRef.id}
                                Student: $studentName
                                Food: ${menuItem.food}
                                Day: ${menuItem.day}
                                Quantity: $quantity
                                Time: $currentTime
                            """.trimIndent()

                            // Navigate to success screen with QR data
                            navController.navigate("qrSuccess/${qrData}")

                            quantity = 1
                            onOrderPlaced()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to place order ❌", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Place Order")
            }
        }
    }
}

@Composable
fun PreorderCard(
    item: PreorderItem,
    db: FirebaseFirestore,
    onOrderCancelled: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🍽️ ${item.food}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "📅 ${item.day}", fontSize = 14.sp)
            Text(text = "🔢 Quantity: ${item.quantity}", fontSize = 14.sp)
            Text(text = "🕒 Order Time: ${item.orderTime}", fontSize = 14.sp)
            Text(
                text = "📋 Status: ${item.status}",
                fontSize = 14.sp,
                color = when (item.status) {
                    "pending" -> androidx.compose.ui.graphics.Color.Blue
                    "completed" -> androidx.compose.ui.graphics.Color.Green
                    "cancelled" -> androidx.compose.ui.graphics.Color.Red
                    else -> androidx.compose.ui.graphics.Color.Gray
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.status == "pending") {
                    Button(
                        onClick = {
                            db.collection("preorderslist").document(item.id)
                                .update("status", "cancelled")
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
                                    onOrderCancelled()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to cancel order", Toast.LENGTH_SHORT).show()
                                }
                        }
                    ) {
                        Text("Cancel Order")
                    }
                }
            }
        }
    }
}

// ✅ Add navigation route in MainActivity.kt
// composable("preorder") { PreorderScreen(navController, email, name) }
