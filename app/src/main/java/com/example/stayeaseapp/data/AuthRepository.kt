package com.example.stayeaseapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Login Function
    suspend fun login(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user // Return Firebase user object
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null on failure
        }
    }

    // Get Current Logged-in User
    suspend fun getUserDetails(): Map<String, String>? {
        val user = auth.currentUser
        return if (user != null) {
            mapOf(
                "name" to (user.displayName ?: "Unknown"),
                "email" to (user.email ?: "N/A"),
                "uid" to user.uid
            )
        } else {
            null
        }
    }

    // Logout Function
    fun logout() {
        auth.signOut()
    }
}
