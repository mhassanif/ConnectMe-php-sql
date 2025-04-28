package com.hassanimran.i220813

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hassanimran.i220813.utils.CallManager
import com.hassanimran.i220813.utils.PermissionsHelper

class VideoCall : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_call)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check and request necessary permissions
        PermissionsHelper.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))

        // Start Video Call
        CallManager.startCall(this)

        // Handle Video Call Button Click
        val videoCall: ImageView = findViewById(R.id.voice_call_optn)
        videoCall.setOnClickListener {
            val intent = Intent(this, VoiceCall::class.java)
            startActivity(intent)
        }

        // Handle End Call Button Click
        val endCall: ImageView = findViewById(R.id.end_call)
        endCall.setOnClickListener {
            CallManager.endCall()
            val intent = Intent(this, Chat::class.java)
            startActivity(intent)
            finish()
        }
    }
}
