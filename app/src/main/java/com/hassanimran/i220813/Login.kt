package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var apiClient: ApiClient
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize components
        usernameInput = findViewById(R.id.username_input) // Make sure to add this to your layout
        passwordInput = findViewById(R.id.password_input) // Make sure to add this to your layout
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        progressBar = findViewById(R.id.progress_bar) // Add this to your layout

        apiClient = ApiClient.getInstance(this)
        sessionManager = SessionManager(this)

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateBasedOnLoginStatus()
            return
        }

        loginButton.setOnClickListener {
            loginUser()
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validate inputs
        if (username.isEmpty()) {
            usernameInput.error = "Username or Email is required"
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            return
        }

        // Show progress
        progressBar.visibility = View.VISIBLE
        loginButton.isEnabled = false

        apiClient.login(username, password) { success, message, user ->
            // Hide progress
            runOnUiThread {
                progressBar.visibility = View.GONE
                loginButton.isEnabled = true

                if (success && user != null) {
                    // Save user session
                    sessionManager.saveUserFromJson(user)
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    // Navigate based on first login status
                    navigateBasedOnLoginStatus()
                } else {
                    Toast.makeText(this, message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateBasedOnLoginStatus() {
        if (sessionManager.isFirstLogin()) {
            // First-time user, go to profile setup
            startActivity(Intent(this, EditProfile::class.java))
        } else {
            // Regular user, go to home/profile
            startActivity(Intent(this, Profile::class.java))
        }
        finish()
    }
}