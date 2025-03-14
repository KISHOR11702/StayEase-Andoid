package com.example.stayeaseapp.model

data class LoginResponse(
    val message: String,
    val token: String,
    val student: Student
)

data class Student(
    val id: Int,
    val name: String,
    val email: String,
    val course: String,
    val classSection: String,
    val room_no: String
)
