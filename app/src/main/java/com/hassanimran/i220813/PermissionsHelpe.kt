package com.hassanimran.i220813.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsHelper {
    fun requestPermissions(activity: Activity, permissions: Array<String>) {
        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, neededPermissions.toTypedArray(), 1)
        }
    }
}
