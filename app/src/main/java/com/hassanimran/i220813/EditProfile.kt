package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditProfile : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var contactNumberInput: EditText
    private lateinit var bioInput: EditText
    private lateinit var profileImage: ImageView
    private lateinit var doneButton: TextView
    private lateinit var userNameText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var apiClient: ApiClient
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize components
        nameInput = findViewById(R.id.name_input)
        usernameInput = findViewById(R.id.username_input)
        contactNumberInput = findViewById(R.id.contact_number_input)
        bioInput = findViewById(R.id.bio_input)
        profileImage = findViewById(R.id.profile_image)
        doneButton = findViewById(R.id.done_button)
        userNameText = findViewById(R.id.user_name)
        progressBar = findViewById(R.id.progress_bar) // Add this to your layout

        apiClient = ApiClient.getInstance(this)
        sessionManager = SessionManager(this)

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        // Load user data
        loadUserData()

        doneButton.setOnClickListener {
            updateProfile()
        }

        // Optional: Add profile image selection
        profileImage.setOnClickListener {
            // TODO: Implement image selection
            Toast.makeText(this, "Image selection will be implemented later", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val userDetails = sessionManager.getUserDetails()

        nameInput.setText(userDetails[SessionManager.NAME])
        usernameInput.setText(userDetails[SessionManager.USERNAME])
        contactNumberInput.setText(userDetails[SessionManager.PHONE])
        userNameText.text = userDetails[SessionManager.NAME] ?: "User"

        // TODO: Load profile image if available
    }

    private fun updateProfile() {
        val name = nameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val phone = contactNumberInput.text.toString().trim()
        val bio = bioInput.text.toString().trim()

        // Validate inputs
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            return
        }

        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            return
        }

        // Show progress
        progressBar.visibility = View.VISIBLE
        doneButton.isEnabled = false

        val userId = sessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            sessionManager.logoutUser()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        apiClient.updateProfile(userId, name, username, bio, phone) { success, message ->
            // Hide progress
            runOnUiThread {
                progressBar.visibility = View.GONE
                doneButton.isEnabled = true

                if (success) {
                    // Update local session
                    sessionManager.updateProfile(name, username, phone)
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                    // Navigate to profile/home
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, message ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}