package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        val registerButton: Button = findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }
}