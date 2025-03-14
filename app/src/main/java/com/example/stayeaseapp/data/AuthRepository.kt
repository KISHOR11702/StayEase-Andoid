package com.example.stayeaseapp.data

import com.example.stayeaseapp.model.LoginRequest
import com.example.stayeaseapp.model.LoginResponse

class AuthRepository(private val api: ApiService) {
    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email, password))
    }
}
