package com.example.stayeaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log  // ✅ Add this
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Token: $token")  // ✅ This works now
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "StayEase"
        val body = remoteMessage.notification?.body ?: "You have a new message"

        Log.d("FCM_DEBUG", "Message received: Title=$title, Body=$body")

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, "Notification: $title - $body", Toast.LENGTH_LONG).show()
        }

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "stayease_notifications"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "StayEase Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
