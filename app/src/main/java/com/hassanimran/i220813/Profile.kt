package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: ApiClient
    private lateinit var profileNameText: TextView // Add this to your layout
    private lateinit var profileUsernameText: TextView // Add this to your layout
    private lateinit var logoutButton: Button // Add this to your layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        apiClient = ApiClient.getInstance(this)

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        // Initialize UI components
        profileNameText = findViewById(R.id.user_name) // Add this to your layout
        profileUsernameText = findViewById(R.id.user_name) // Add this to your layout
        logoutButton = findViewById(R.id.logout_button) // Add this to your layout

        // Load user data
        loadUserData()

        // Setup navigation
        val followers: LinearLayout = findViewById(R.id.followers)
        followers.setOnClickListener {
            val intent = Intent(this, Followers::class.java)
            startActivity(intent)
        }

        val following: LinearLayout = findViewById(R.id.following)
        following.setOnClickListener {
            val intent = Intent(this, Following::class.java)
            startActivity(intent)
        }

        val editProfile: ImageView = findViewById(R.id.edit_profile)
        editProfile.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        // Add logout button
        logoutButton.setOnClickListener {
            logoutUser()
        }

        // Initialize and set up the NavBar
        val navBar = NavBar(this)
        navBar.setupNavBar()
    }

    private fun loadUserData() {
        val userDetails = sessionManager.getUserDetails()

        profileNameText.text = userDetails[SessionManager.NAME] ?: "User"
        profileUsernameText.text = "@${userDetails[SessionManager.USERNAME] ?: "username"}"

        // TODO: Load profile image if available
    }

    private fun logoutUser() {
        val userId = sessionManager.getUserId()
        if (userId != null) {
            apiClient.logout(userId) { success ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Logout failed on server, but session cleared locally", Toast.LENGTH_SHORT).show()
                    }

                    // Clear session and go to login
                    sessionManager.logoutUser()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }
            }
        } else {
            // Just clear session and go to login
            sessionManager.logoutUser()
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}