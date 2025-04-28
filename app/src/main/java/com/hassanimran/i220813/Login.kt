package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Test Firebase Authentication
        if (auth.currentUser != null) {
            println("User is logged in: ${auth.currentUser?.uid}")
            navigateToHome()
        } else {
            println("No user is logged in")
        }

        // if not logged in the load login screen
        setContentView(R.layout.activity_login)

        val usernameField: EditText = findViewById(R.id.username_field)
        val passwordField: EditText = findViewById(R.id.password_field)
        val loginButton: Button = findViewById(R.id.login_button)
        val registerButton: Button = findViewById(R.id.register_button)



        loginButton.setOnClickListener {
            val email = usernameField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navigateToHome()
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
