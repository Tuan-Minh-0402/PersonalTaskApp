package com.example.personaltaskapp.repository

import android.content.Context
import com.example.personaltaskapp.model.User
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class AuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun register(email: String, password: String): Result<Unit> {
        val users = getUsers().toMutableList()
        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Email already exists"))
        }

        users.add(User(email = email.trim(), password = password))
        saveUsers(users)
        return Result.success(Unit)
    }

    fun login(email: String, password: String): Result<Unit> {
        val user = getUsers().find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(IllegalArgumentException("Email not found"))

        if (user.password != password) {
            return Result.failure(IllegalArgumentException("Wrong password"))
        }

        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_CURRENT_EMAIL, user.email)
        }

        return Result.success(Unit)
    }

    fun logout() {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_CURRENT_EMAIL)
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun emailExists(email: String): Boolean =
        getUsers().any { it.email.equals(email.trim(), ignoreCase = true) }

    private fun getUsers(): List<User> {
        val jsonString = prefs.getString(KEY_USERS, null) ?: return emptyList()
        val array = JSONArray(jsonString)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    User(
                        email = obj.optString("email"),
                        password = obj.optString("password")
                    )
                )
            }
        }
    }

    private fun saveUsers(users: List<User>) {
        val array = JSONArray()
        users.forEach { user ->
            array.put(
                JSONObject()
                    .put("email", user.email)
                    .put("password", user.password)
            )
        }
        prefs.edit { putString(KEY_USERS, array.toString()) }
    }

    private companion object {
        const val KEY_USERS = "users_json"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_CURRENT_EMAIL = "current_user_email"
    }
}