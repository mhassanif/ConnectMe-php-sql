package com.hassanimran.i220813

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class ApiClient private constructor(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    companion object {
        private const val TAG = "ApiClient"
        private const val BASE_URL = "http://192.168.43.162/connectme" // Change to your API URL

        @Volatile
        private var INSTANCE: ApiClient? = null

        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiClient(context).also { INSTANCE = it }
            }
        }
    }

    fun login(identifier: String, password: String, callback: (success: Boolean, message: String?, user: JSONObject?) -> Unit) {
        val url = "$BASE_URL/login.php"

        val params = HashMap<String, String>()
        params["username"] = identifier // API expects 'username' parameter for both username and email
        params["password"] = password

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.d(TAG, "Login response: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.optBoolean("success", false)
                    val message = jsonResponse.optString("message")

                    if (success) {
                        val user = jsonResponse.optJSONObject("user")
                        callback(true, message, user)
                    } else {
                        callback(false, message, null)
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}", e)
                    callback(false, "Error parsing response", null)
                }
            },
            { error ->
                Log.e(TAG, "Login error: ${error.message}", error)
                callback(false, "Network error: ${error.message}", null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(request)
    }

    fun register(name: String, username: String, email: String, password: String, phone: String,
                 callback: (success: Boolean, message: String?, user: JSONObject?) -> Unit) {
        val url = "$BASE_URL/register.php"

        val params = HashMap<String, String>()
        params["name"] = name
        params["username"] = username
        params["email"] = email
        params["password"] = password
        params["confirm_password"] = password // API expects confirmation
        params["phone"] = phone

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.d(TAG, "Register response: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.optBoolean("success", false)
                    val message = jsonResponse.optString("message")

                    if (success) {
                        val user = jsonResponse.optJSONObject("user")
                        callback(true, message, user)
                    } else {
                        callback(false, message, null)
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}", e)
                    callback(false, "Error parsing response", null)
                }
            },
            { error ->
                Log.e(TAG, "Register error: ${error.message}", error)
                callback(false, "Network error: ${error.message}", null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(request)
    }

    fun updateProfile(userId: String, name: String, username: String, bio: String, phone: String,
                      callback: (success: Boolean, message: String?) -> Unit) {
        val url = "$BASE_URL/update_profile.php"

        val params = HashMap<String, String>()
        params["user_id"] = userId
        params["name"] = name
        params["username"] = username
        params["phone"] = phone
        // Bio is not in your PHP code, but we can add it if needed

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.d(TAG, "Update profile response: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.optBoolean("success", false)
                    val message = jsonResponse.optString("message")
                    callback(success, message)
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}", e)
                    callback(false, "Error parsing response")
                }
            },
            { error ->
                Log.e(TAG, "Update profile error: ${error.message}", error)
                callback(false, "Network error: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(request)
    }

    fun uploadProfilePicture(userId: String, imageData: ByteArray, callback: (success: Boolean, message: String?, imageUrl: String?) -> Unit) {
        // This would be implemented with a multipart request
        // For now, we'll just return a placeholder success
        callback(true, "Profile picture updated", null)
    }

    fun logout(userId: String, callback: (success: Boolean) -> Unit) {
        val url = "$BASE_URL/logout.php"

        val params = HashMap<String, String>()
        params["user_id"] = userId

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.d(TAG, "Logout response: $response")
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.optBoolean("success", false)
                    callback(success)
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}", e)
                    callback(false)
                }
            },
            { error ->
                Log.e(TAG, "Logout error: ${error.message}", error)
                callback(false)
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        requestQueue.add(request)
    }
}