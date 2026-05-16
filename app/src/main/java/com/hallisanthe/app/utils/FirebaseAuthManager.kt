package com.hallisanthe.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private const val PREFS_NAME = "halli_santhe_prefs"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"

    fun isLoggedIn(): Boolean = auth.currentUser != null && auth.currentUser?.isAnonymous == false

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun logout() {
        auth.signOut()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserSession(context: Context, role: String, name: String, email: String) {
        prefs(context).edit()
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun clearUserSession(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun getSavedRole(context: Context): String {
        return prefs(context).getString(KEY_USER_ROLE, "Buyer").orEmpty()
    }
}
