package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up Profile button to lead to Profile activity
        val profileButton: Button = findViewById(R.id.view_profile_button)
        profileButton.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        // Set up Send button to lead to VanishMode activity
        val sendButton: ImageView = findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            val intent = Intent(this, VanishMode::class.java)
            startActivity(intent)
        }

        //set back button
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        //set call button
        val callButton: ImageView = findViewById(R.id.call)
        callButton.setOnClickListener {
            val intent = Intent(this, VoiceCall::class.java)
            startActivity(intent)
        }

        //set call button
        val videoCallButton: ImageView = findViewById(R.id.video_call)
        videoCallButton.setOnClickListener {
            val intent = Intent(this, VideoCall::class.java)
            startActivity(intent)
        }

    }
}