package com.hallisanthe.app.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Buyer",
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
