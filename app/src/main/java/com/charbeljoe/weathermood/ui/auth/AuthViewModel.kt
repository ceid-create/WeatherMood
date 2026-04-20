package com.charbeljoe.weathermood.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.charbeljoe.weathermood.data.local.AppDatabase
import com.charbeljoe.weathermood.data.local.User
import com.charbeljoe.weathermood.data.remote.RetrofitClient
import com.charbeljoe.weathermood.data.remote.models.ResetCodeRequest
import com.charbeljoe.weathermood.data.remote.models.VerifyCodeRequest
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    val loginResult = MutableLiveData<String?>()
    val registerResult = MutableLiveData<String?>()
    val changePasswordResult = MutableLiveData<String?>()
    val sendResetCodeResult = MutableLiveData<String?>()   // null = success, non-null = error
    val verifyCodeResult = MutableLiveData<String?>()      // null = success, non-null = error

    fun isLoggedIn() = prefs.getBoolean("is_logged_in", false)

    suspend fun isSessionValid(): Boolean {
        if (!isLoggedIn()) return false
        val username = getUsername()
        if (username.isBlank()) return false
        return db.userDao().findByUsername(username) != null
    }

    fun getUsername() = prefs.getString("username", "") ?: ""

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            loginResult.value = "Please fill in all fields"
            return
        }
        viewModelScope.launch {
            val user = db.userDao().findByUsername(username.trim())
            when {
                user == null -> loginResult.value = "No account found with that username"
                user.password != hashPassword(password) -> loginResult.value = "Incorrect password"
                else -> {
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("username", username.trim())
                        .apply()
                    loginResult.value = null
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, confirm: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            registerResult.value = "Please fill in all fields"
            return
        }
        if (username.trim().length < 3) {
            registerResult.value = "Username must be at least 3 characters"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            registerResult.value = "Please enter a valid email address"
            return
        }
        if (password.length < 6) {
            registerResult.value = "Password must be at least 6 characters"
            return
        }
        if (password != confirm) {
            registerResult.value = "Passwords don't match"
            return
        }
        viewModelScope.launch {
            if (db.userDao().findByUsername(username.trim()) != null) {
                registerResult.value = "Username already taken"
                return@launch
            }
            if (db.userDao().findByEmail(email.trim().lowercase()) != null) {
                registerResult.value = "Email already in use"
                return@launch
            }
            db.userDao().insert(
                User(
                    username = username.trim(),
                    email = email.trim().lowercase(),
                    password = hashPassword(password)
                )
            )
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("username", username.trim())
                .apply()
            registerResult.value = null
        }
    }

    fun changePassword(current: String, newPwd: String, confirm: String) {
        val username = prefs.getString("username", "") ?: ""
        if (current.isBlank() || newPwd.isBlank() || confirm.isBlank()) {
            changePasswordResult.value = "Please fill in all fields"
            return
        }
        if (newPwd.length < 6) {
            changePasswordResult.value = "New password must be at least 6 characters"
            return
        }
        if (newPwd != confirm) {
            changePasswordResult.value = "Passwords don't match"
            return
        }
        viewModelScope.launch {
            val user = db.userDao().findByUsername(username)
            if (user == null || user.password != hashPassword(current)) {
                changePasswordResult.value = "Current password is incorrect"
                return@launch
            }
            db.userDao().updatePassword(username, hashPassword(newPwd))
            changePasswordResult.value = null
        }
    }

    fun sendResetCode(email: String) {
        if (email.isBlank()) {
            sendResetCodeResult.value = "Please enter your email"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            sendResetCodeResult.value = "Please enter a valid email address"
            return
        }
        viewModelScope.launch {
            val user = db.userDao().findByEmail(email.trim().lowercase())
            if (user == null) {
                sendResetCodeResult.value = "No account found with that email"
                return@launch
            }
            try {
                val response = RetrofitClient.backendApi.sendResetCode(
                    ResetCodeRequest(email.trim().lowercase())
                )
                sendResetCodeResult.value = if (response.isSuccessful) null
                else "Failed to send code. Please try again."
            } catch (e: Exception) {
                sendResetCodeResult.value = "Network error. Check your connection."
            }
        }
    }

    fun verifyAndResetPassword(email: String, code: String, newPassword: String, confirm: String) {
        if (code.isBlank() || newPassword.isBlank() || confirm.isBlank()) {
            verifyCodeResult.value = "Please fill in all fields"
            return
        }
        if (newPassword.length < 6) {
            verifyCodeResult.value = "Password must be at least 6 characters"
            return
        }
        if (newPassword != confirm) {
            verifyCodeResult.value = "Passwords don't match"
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.backendApi.verifyResetCode(
                    VerifyCodeRequest(email.trim().lowercase(), code.trim())
                )
                if (response.isSuccessful) {
                    val user = db.userDao().findByEmail(email.trim().lowercase())
                    if (user != null) {
                        db.userDao().updatePassword(user.username, hashPassword(newPassword))
                    }
                    verifyCodeResult.value = null
                } else {
                    val body = response.errorBody()?.string() ?: ""
                    verifyCodeResult.value = when {
                        body.contains("expired") -> "Code has expired. Request a new one."
                        body.contains("Invalid") -> "Incorrect code. Please try again."
                        else -> "Verification failed. Please try again."
                    }
                }
            } catch (e: Exception) {
                verifyCodeResult.value = "Network error. Check your connection."
            }
        }
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).remove("username").apply()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
