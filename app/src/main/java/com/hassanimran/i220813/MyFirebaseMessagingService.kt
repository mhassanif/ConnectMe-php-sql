package com.hassanimran.i220813

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelId = "notification_channel_id"
const val channelName = "com.hassanimran.i220813"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // generate the notification
    //attach the notification with the custom layout
    // show the notification

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d("FCM", "Message Notification Body: ${remoteMessage.notification!!.body}")
            val title = remoteMessage.notification!!.title ?: "New Notification"
            val body = remoteMessage.notification!!.body ?: "You have a new notification"
            showNotification(this, title, body)
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            val messageType = remoteMessage.data["messageType"]
            when (messageType) {
                "newMessage" -> {
                    val sender = remoteMessage.data["sender"] ?: "Unknown"
                    val message = remoteMessage.data["message"] ?: "New message"
                    showNotification(this, "New Message from $sender", message)
                }
                "followRequest" -> {
                    val follower = remoteMessage.data["follower"] ?: "Unknown"
                    showNotification(this, "New Follow Request", "$follower wants to follow you")
                }
                else -> {
                    // Handle other message types or unknown types
                    showNotification(this, "New Notification", "You have a new notification")
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server.
        Log.d("FCM", "sendRegistrationTokenToServer($token)")
    }

    private fun getRemoteView(title: String, message: String): RemoteViews {
        val remoteView = RemoteViews(packageName, R.layout.item_notification)
        remoteView.setTextViewText(R.id.title, title)
        remoteView.setTextViewText(R.id.notification_message, message)
        remoteView.setImageViewResource(R.id.app_logo, R.drawable.logo)
        return remoteView
    }

    private fun showNotification(context: Context, title: String, message: String) {
        // 1. Create a Notification Channel (Required for Android 8.0+)
        createNotificationChannel(context, channelId)

        // 2. Create an Intent for when the user taps the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. Build the Notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo) // Replace with your icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setContent(getRemoteView(title, message))

        // 4. Show the Notification
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            val notificationId = 1 // Choose a unique ID for each notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notify(notificationId, builder.build())
                }
            } else {
                notify(notificationId, builder.build())
            }
        }
    }

    // Helper function to create a notification channel
    private fun createNotificationChannel(context: Context, channelId: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = channelName // Replace with your channel name
            val descriptionText = "My Channel Description" // Replace with your channel description
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}