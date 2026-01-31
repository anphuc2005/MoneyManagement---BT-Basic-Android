package com.example.moneymanagement.data.data_class

data class UserProfile(
    val userId: String = "",
    val fullName: String = "",
    val dateOfBirth: String = "",
    val description: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "", 0L, 0L)
}