package com.hallisanthe.app.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.data.model.UserProfile
import com.hallisanthe.app.utils.Resource
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseMarketplaceRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val usersCollection = firestore.collection("users")
    private val productsCollection = firestore.collection("products")
    private val wishlistCollection = firestore.collection("wishlist")

    fun currentUserId(): String? = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unable to login")
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String
    ): Resource<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid.orEmpty()
            if (uid.isBlank()) {
                Resource.Error("Registration failed")
            } else {
                val profile = UserProfile(uid = uid, name = name, email = email, role = role)
                usersCollection.document(uid).set(profile).await()
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unable to register")
        }
    }

    fun logout() = auth.signOut()

    suspend fun fetchProducts(): Resource<List<Product>> {
        return try {
            val snapshot = productsCollection.get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt }
            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch products")
        }
    }

    suspend fun fetchProductById(productId: String): Resource<Product> {
        return try {
            val snapshot = productsCollection.document(productId).get().await()
            val product = snapshot.toObject(Product::class.java)?.copy(id = snapshot.id)
            if (product == null) Resource.Error("Product not found") else Resource.Success(product)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch product")
        }
    }

    suspend fun uploadProduct(
        product: Product,
        compressedImageUri: Uri,
        onProgress: (Int) -> Unit
    ): Resource<Unit> {
        return try {
            val uid = currentUserId().orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("Login required to upload")
            }

            val fileName = "products/$uid/${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child(fileName)
            val uploadTask = imageRef.putFile(compressedImageUri)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = ((taskSnapshot.bytesTransferred * 100) / taskSnapshot.totalByteCount)
                    .toInt()
                onProgress(progress)
            }
            uploadTask.await()
            val imageUrl = imageRef.downloadUrl.await().toString()
            val user = fetchUserProfile(uid)
            val sellerName = if (user is Resource.Success) user.data.name else "Local Artisan"

            val docRef = productsCollection.document()
            val payload = product.copy(
                id = docRef.id,
                imageUrl = imageUrl,
                sellerId = uid,
                sellerName = sellerName,
                createdAt = System.currentTimeMillis()
            )
            docRef.set(payload).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload product")
        }
    }

    suspend fun fetchUserProfile(uid: String = currentUserId().orEmpty()): Resource<UserProfile> {
        return try {
            if (uid.isBlank()) {
                return Resource.Error("User not logged in")
            }
            val snapshot = usersCollection.document(uid).get().await()
            val profile = snapshot.toObject(UserProfile::class.java)?.copy(uid = uid)
            if (profile == null) Resource.Error("User profile not found") else Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    suspend fun updateProfileImage(imageUri: Uri): Resource<String> {
        return try {
            val uid = currentUserId().orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("User not logged in")
            }
            val imageRef = storage.reference.child("profile_images/$uid.jpg")
            imageRef.putFile(imageUri).await()
            val url = imageRef.downloadUrl.await().toString()
            usersCollection.document(uid).update("profileImageUrl", url).await()
            Resource.Success(url)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile image")
        }
    }

    suspend fun fetchUploadedProductsCount(): Resource<Int> {
        return try {
            val uid = currentUserId().orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("User not logged in")
            }
            val snapshot = productsCollection.whereEqualTo("sellerId", uid).get().await()
            Resource.Success(snapshot.size())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch upload count")
        }
    }

    suspend fun addToWishlist(productId: String): Resource<Unit> {
        return try {
            val uid = currentUserId().orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("Login required")
            }
            wishlistCollection.document(uid).set(
                mapOf("productIds" to FieldValue.arrayUnion(productId)),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add wishlist")
        }
    }

    suspend fun removeFromWishlist(productId: String): Resource<Unit> {
        return try {
            val uid = currentUserId().orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("Login required")
            }
            wishlistCollection.document(uid).set(
                mapOf("productIds" to FieldValue.arrayRemove(productId)),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove wishlist")
        }
    }

    suspend fun isWishlisted(productId: String): Resource<Boolean> {
        return try {
            val uid = currentUserId().orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("Login required")
            }
            val snapshot = wishlistCollection.document(uid).get().await()
            val ids = snapshot.get("productIds") as? List<*> ?: emptyList<Any>()
            Resource.Success(ids.contains(productId))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check wishlist")
        }
    }

    suspend fun fetchWishlistProducts(): Resource<List<Product>> {
        return try {
            val uid = currentUserId().orEmpty()
            if (uid.isBlank()) {
                return Resource.Error("Login required")
            }
            val snapshot = wishlistCollection.document(uid).get().await()
            val ids = (snapshot.get("productIds") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
            if (ids.isEmpty()) {
                return Resource.Success(emptyList())
            }
            val productsSnapshot = productsCollection.whereIn(
                com.google.firebase.firestore.FieldPath.documentId(),
                ids.take(10)
            ).get().await()
            val products = productsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch wishlist")
        }
    }
}
