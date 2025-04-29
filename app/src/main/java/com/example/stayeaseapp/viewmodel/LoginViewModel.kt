package com.example.stayeaseapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val queue: RequestQueue = Volley.newRequestQueue(application.applicationContext)

    private val _loginState = MutableStateFlow<FirebaseUser?>(null)
    val loginState: StateFlow<FirebaseUser?> = _loginState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, password: String, onLoginComplete: (Boolean) -> Unit) {
        _errorMessage.value = null

        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty."
            onLoginComplete(false)
            return
        }

        if (!isNetworkAvailable()) {
            _errorMessage.value = "No internet connection. Check your network and try again."
            onLoginComplete(false)
            return
        }

        val requestBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST,
            " https://2aca-14-139-180-67.ngrok-free.app/api/auth/login", // ✅ No extra spaces
            requestBody,
            { response ->
                val firebaseToken = response.optString("firebaseToken", null)
                if (firebaseToken != null) {
                    Log.d("LoginViewModel", "Firebase Token Received: $firebaseToken")
                    clearOldToken()
                    saveToken(firebaseToken)
                    signInWithFirebase(firebaseToken, onLoginComplete)
                } else {
                    _errorMessage.value = "Invalid Firebase token received."
                    onLoginComplete(false)
                }
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: -1
                val errorMessage = error.message ?: "Unknown error"
                Log.e("LoginViewModel", "Volley Error: Code $statusCode - $errorMessage")

                _errorMessage.value = when (statusCode) {
                    -1 -> "Network error: Check internet or backend server."
                    302 -> "Redirect issue: Ensure the backend URL is correct."
                    400 -> "Invalid email or password."
                    401 -> "Unauthorized access. Check credentials."
                    500 -> "Server error. Try again later."
                    else -> "Login failed: $errorMessage"
                }
                onLoginComplete(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Content-Type" to "application/json")
            }
        }

        queue.add(request)
    }

    private fun signInWithFirebase(firebaseToken: String, onLoginComplete: (Boolean) -> Unit) {
        auth.signInWithCustomToken(firebaseToken)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    _loginState.value = user
                    Log.d("LoginViewModel", "Firebase login successful: ${user.email}")
                    onLoginComplete(true)
                } else {
                    _errorMessage.value = "Login failed: User data is missing."
                    Log.e("LoginViewModel", "Firebase login failed: No user returned")
                    onLoginComplete(false)
                }
            }
            .addOnFailureListener {
                _errorMessage.value = "Firebase login failed: ${it.message}"
                Log.e("LoginViewModel", "Firebase Login Error: ${it.message}")
                onLoginComplete(false)
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getApplication<Application>()
            .applicationContext.getSharedPreferences("StayEasePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("authToken", token).apply()
    }

    private fun clearOldToken() {
        val sharedPreferences = getApplication<Application>()
            .applicationContext.getSharedPreferences("StayEasePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("authToken").apply()
    }

    override fun onCleared() {
        super.onCleared()
        queue.cancelAll { true }
    }
}
