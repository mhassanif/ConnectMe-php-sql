package com.hassanimran.i220813

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val IS_LOGGED_IN = "IsLoggedIn"
        const val USER_ID = "id"
        const val USERNAME = "username"
        const val EMAIL = "email"
        const val NAME = "name"
        const val PHONE = "phone"
        const val IS_FIRST_LOGIN = "IsFirstLogin"
        const val PROFILE_PICTURE = "profile_picture"
    }

    fun createLoginSession(userId: String, username: String, email: String, name: String, phone: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_ID, userId)
        editor.putString(USERNAME, username)
        editor.putString(EMAIL, email)
        editor.putString(NAME, name)
        editor.putString(PHONE, phone)
        editor.putBoolean(IS_FIRST_LOGIN, true) // Default to true for new logins
        editor.apply()
    }

    fun saveUserFromJson(userJson: JSONObject) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_ID, userJson.optString("id"))
        editor.putString(USERNAME, userJson.optString("username"))
        editor.putString(EMAIL, userJson.optString("email"))
        editor.putString(NAME, userJson.optString("name"))
        editor.putString(PHONE, userJson.optString("phone"))

        // Check if profile picture exists
        if (userJson.has(PROFILE_PICTURE) && !userJson.isNull(PROFILE_PICTURE)) {
            editor.putString(PROFILE_PICTURE, userJson.optString(PROFILE_PICTURE))
        }

        // Check if this is a first-time login (no profile picture means first login)
        val isFirstLogin = !userJson.has(PROFILE_PICTURE) || userJson.isNull(PROFILE_PICTURE)
        editor.putBoolean(IS_FIRST_LOGIN, isFirstLogin)

        editor.apply()
    }

    fun updateProfile(name: String, username: String, phone: String, profilePicture: String? = null) {
        editor.putString(NAME, name)
        editor.putString(USERNAME, username)
        editor.putString(PHONE, phone)

        if (profilePicture != null) {
            editor.putString(PROFILE_PICTURE, profilePicture)
        }

        // After updating profile, it's no longer first login
        editor.putBoolean(IS_FIRST_LOGIN, false)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun isFirstLogin(): Boolean {
        return prefs.getBoolean(IS_FIRST_LOGIN, true)
    }

    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[USER_ID] = prefs.getString(USER_ID, null)
        user[USERNAME] = prefs.getString(USERNAME, null)
        user[EMAIL] = prefs.getString(EMAIL, null)
        user[NAME] = prefs.getString(NAME, null)
        user[PHONE] = prefs.getString(PHONE, null)
        user[PROFILE_PICTURE] = prefs.getString(PROFILE_PICTURE, null)
        return user
    }

    fun logoutUser() {
        editor.clear()
        editor.apply()
    }
}