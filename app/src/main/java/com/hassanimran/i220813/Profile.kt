package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //add button to lead to follower and follwoing page
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

        // Initialize and set up the NavBar
        val navBar = NavBar(this)
        navBar.setupNavBar()

    }
}