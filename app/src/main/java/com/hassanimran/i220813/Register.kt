package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Register : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var apiClient: ApiClient
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize components
        nameInput = findViewById(R.id.name_input) // Make sure to add this to your layout
        usernameInput = findViewById(R.id.username_input) // Make sure to add this to your layout
        phoneInput = findViewById(R.id.phone_input) // Make sure to add this to your layout
        emailInput = findViewById(R.id.email_input) // Make sure to add this to your layout
        passwordInput = findViewById(R.id.password_input) // Make sure to add this to your layout
        registerButton = findViewById(R.id.register_button)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.progress_bar) // Add this to your layout

        apiClient = ApiClient.getInstance(this)
        sessionManager = SessionManager(this)

        registerButton.setOnClickListener {
            registerUser()
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val name = nameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validate inputs
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            return
        }

        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            return
        }

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            return
        }

        // Show progress
        progressBar.visibility = View.VISIBLE
        registerButton.isEnabled = false

        apiClient.register(name, username, email, password, phone) { success, message, user ->
            // Hide progress
            runOnUiThread {
                progressBar.visibility = View.GONE
                registerButton.isEnabled = true

                if (success && user != null) {
                    // Save user session
                    sessionManager.saveUserFromJson(user)
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                    // New users always go to profile setup
                    val intent = Intent(this, EditProfile::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}