package com.hassanimran.i220813.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

object CallManager {
    fun startCall(context: Context) {
        Toast.makeText(context, "Starting Video Call...", Toast.LENGTH_SHORT).show()
        Log.d("CallManager", "Video Call Started")
    }

    fun startVoiceCall(context: Context) {
        Toast.makeText(context, "Starting Voice Call...", Toast.LENGTH_SHORT).show()
        Log.d("CallManager", "Voice Call Started")
    }

    fun endCall() {
        Log.d("CallManager", "Call Ended")
    }
}
