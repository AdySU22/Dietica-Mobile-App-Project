package com.example.dietica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Calendar
import java.util.TimeZone

class NotificationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        val btnBack: Button = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        initNotification()
    }

    private fun initNotification() {
        val notificationLayout = findViewById<LinearLayout>(R.id.notificationLayout)

        // Clear all existing views
        notificationLayout.removeAllViews()

        // Remove vertical gravity and only keep horizontal gravity
        notificationLayout.gravity = Gravity.START

        // List of titles and messages for each notification item
        val notifications = listOf(
            Pair("Don't forget to input your food!", "We need the data to help you!"),
            Pair("Do your daily routine!", "Remember to log your exercise activities!"),
            Pair("Hey, don't forget to drink water today!", "Your body is 70% water, so make sure to hydrate!")
        )

        // Get the current UTC hour
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Determine the order based on the current UTC time
        val reorderedNotifications = when {
            // 23:00 - 04:59
            currentHour in 23..23 || currentHour in 0..4 -> listOf(notifications[0], notifications[2], notifications[1])
            // 05:00 - 11:59
            currentHour in 5..11 -> listOf(notifications[1], notifications[0], notifications[2])
            // 12:00 - 22:59
            currentHour in 12..22 -> listOf(notifications[2], notifications[1], notifications[0])
            else -> notifications // Fallback in case of unexpected hour value
        }

        // Loop through the notifications list and add each item to the layout
        for (notification in reorderedNotifications) {
            // Inflate the item_notification layout
            val notificationItem = LayoutInflater
                .from(this).inflate(R.layout.item_notification, notificationLayout, false)

            // Adjust the notificationTitle and notificationMessage
            val notificationTitle = notificationItem.findViewById<TextView>(R.id.notificationTitle)
            val notificationMessage = notificationItem.findViewById<TextView>(R.id.notificationMessage)

            // Set the title and message
            notificationTitle.text = notification.first
            notificationMessage.text = notification.second

            // Add the notification item to the notificationLayout
            notificationLayout.addView(notificationItem)
        }
    }
}