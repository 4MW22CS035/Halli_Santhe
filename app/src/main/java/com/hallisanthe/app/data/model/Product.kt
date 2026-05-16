package com.hallisanthe.app.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val sellerId: String = "",
    val sellerName: String = "Local Artisan",
    val inStock: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
