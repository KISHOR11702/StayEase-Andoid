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
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import java.util.*

@Composable
fun FoodMenuScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var menuItems by remember { mutableStateOf<List<Triple<String, String, Map<String, String>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val validDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // Fetch menu from Firestore
    LaunchedEffect(Unit) {
        db.collection("food_menu").get()
            .addOnSuccessListener { result ->
                val fetchedMenu = result.documents.map { doc ->
                    val docId = doc.id
                    val rawDay = doc.getString("day")?.trim() ?: docId
                    val day = rawDay.replaceFirstChar { it.uppercaseChar() } // Capitalize first letter

                    val menuString = doc.getString("menu")?.takeIf { it.isNotBlank() } ?: ""
                    val parsedMenu = parseMenu(menuString)

                    Triple(docId, day, parsedMenu)
                }

                // Sort menu by day of the week, invalid days go to the end
                menuItems = fetchedMenu.sortedWith(compareBy {
                    val index = validDays.indexOf(it.second)
                    if (index != -1) index else Int.MAX_VALUE // Invalid days at the end
                })

                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("FoodMenuScreen", "Error fetching menu", e)
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Menu") },
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
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(menuItems, key = { it.first }) { (docId, day, items) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = day, style = MaterialTheme.typography.h6)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (items.isEmpty()) {
                                    Text(text = "No menu available", style = MaterialTheme.typography.body1)
                                } else {
                                    items.forEach { (mealType, meal) ->
                                        Text(text = "$mealType: $meal", style = MaterialTheme.typography.body1)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Doc ID: $docId", style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseMenu(menuString: String): Map<String, String> {
    return menuString.split(";").mapNotNull { entry ->
        val parts = entry.split(":", limit = 2)
        if (parts.size == 2) {
            val mealType = parts[0].trim()
            val mealItems = parts[1].trim().replace(",", ", ")
            mealType to mealItems
        } else null
    }.toMap()
}
