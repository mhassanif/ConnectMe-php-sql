package com.hassanimran.i220813

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hassanimran.i220813.utils.CallManager

class VoiceCall : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voice_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Start Voice Call Logic
        CallManager.startVoiceCall(this)

        val videoCall: ImageView = findViewById(R.id.video_call_optn)
        videoCall.setOnClickListener {
            val intent = Intent(this, VideoCall::class.java)
            startActivity(intent)
        }

        val endCall: ImageView = findViewById(R.id.end_call)
        endCall.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

}