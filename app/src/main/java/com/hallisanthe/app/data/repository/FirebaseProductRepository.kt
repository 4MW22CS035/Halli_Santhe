package com.hallisanthe.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val productsCollection = firestore.collection("products")

    // Reads all products and sorts newest first for marketplace feed.
    suspend fun fetchProducts(): Resource<List<Product>> = try {
        val snapshot = productsCollection.get().await()
        val products = snapshot.documents.mapNotNull { document ->
            document.toObject(Product::class.java)?.copy(id = document.id)
        }.sortedByDescending { it.createdAt }
        Resource.Success(products)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch products")
    }

    // Reads a single product document by id for detail screen.
    suspend fun fetchProductById(productId: String): Resource<Product> = try {
        val snapshot = productsCollection.document(productId).get().await()
        val product = snapshot.toObject(Product::class.java)?.copy(id = snapshot.id)
        if (product == null) Resource.Error("Product not found") else Resource.Success(product)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to fetch product details")
    }

    // Uploads image to Storage first, then persists full product to Firestore.
    suspend fun uploadProduct(
        product: Product,
        compressedImageUri: Uri
    ): Resource<Unit> = try {
        val fileName = "products/${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child(fileName)
        imageRef.putFile(compressedImageUri).await()
        val imageUrl = imageRef.downloadUrl.await().toString()

        val docRef = productsCollection.document()
        val payload = product.copy(
            id = docRef.id,
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(payload).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to upload product")
    }
}
