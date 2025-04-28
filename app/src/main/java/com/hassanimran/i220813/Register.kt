package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference // Initialize Realtime Database reference

        // Initialize UI components
        val name = findViewById<EditText>(R.id.name)
        val username = findViewById<EditText>(R.id.user_name)
        val phoneNumber = findViewById<EditText>(R.id.phone_number)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginButton = findViewById<Button>(R.id.login_button)

        // Set click listener for register button
        registerButton.setOnClickListener {
            handleRegistration(
                name.text.toString().trim(),
                username.text.toString().trim(),
                phoneNumber.text.toString().trim(),
                email.text.toString().trim(),
                password.text.toString().trim()
            )
        }

        // Set click listener for login button
        loginButton.setOnClickListener {
            navigateToLogin()
        }
    }

    /**
     * Handles user registration by validating inputs, creating a Firebase user,
     * and saving additional user data to Realtime Database.
     */
    private fun handleRegistration(
        userName: String,
        userUsername: String,
        userPhone: String,
        userEmail: String,
        userPassword: String
    ) {
        // Validate input fields
        if (userName.isEmpty() || userUsername.isEmpty() || userPhone.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserData(userId, userName, userUsername, userPhone, userEmail)
                } else {
                    Toast.makeText(
                        this,
                        "Registration Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Saves additional user data to Realtime Database using the unique user ID (UID).
     */
    private fun saveUserData(userId: String, name: String, username: String, phone: String, email: String) {
        val userMap = hashMapOf(
            "name" to name,
            "username" to username,
            "phone" to phone,
            "email" to email,
            "bio" to "",
            "profile_picture" to ""  // Changed from profileImage to profile_picture
        )

        db.child("users").child(userId).setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                navigateToEditProfile()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Navigates the user to the Home activity.
     */
    private fun navigateToEditProfile() {
        startActivity(Intent(this, EditProfile::class.java))
        finish()
    }

    /**
     * Navigates the user to the Login activity.
     */
    private fun navigateToLogin() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }
}
