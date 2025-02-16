package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerButton: Button = findViewById(R.id.register_button)
        val loginButton: Button = findViewById(R.id.login_button)

        registerButton.setOnClickListener {
            // Navigate to HomeActivity
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}