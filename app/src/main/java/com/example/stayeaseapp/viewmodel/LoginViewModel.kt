package com.example.stayeaseapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stayeaseapp.data.AuthRepository
import com.example.stayeaseapp.data.ApiService
import com.example.stayeaseapp.model.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository(ApiService.create())

    fun login(email: String, password: String, onResult: (LoginResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                onResult(response)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
