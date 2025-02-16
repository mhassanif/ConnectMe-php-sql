package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Following : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_following)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val topChat: ImageView = findViewById(R.id.top_chat_icon)
        topChat.setOnClickListener {
            val intent = Intent(this, Chat::class.java)
            startActivity(intent)
        }

        val back: ImageView = findViewById(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        val tabChange: TextView = findViewById(R.id.followers_tab)
        tabChange.setOnClickListener {
            val intent = Intent(this, Followers::class.java)
            startActivity(intent)
        }


    }
}