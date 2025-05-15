package com.example.stayeaseapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<FirebaseUser?>(auth.currentUser) // Default to the current user
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

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginState.value = auth.currentUser
                    onLoginComplete(true)
                } else {
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                    Log.e("LoginViewModel", "Login failed: ${task.exception}")
                    onLoginComplete(false)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _loginState.value = null
    }
}
