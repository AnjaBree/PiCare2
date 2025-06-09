package com.example.picare.model  // Use your actual package name

import com.google.firebase.Timestamp

data class Reminder(
    val animalName: String = "",
    val reminderText: String = "",
    val reminderTime: Long = 0L, // stored as millis
    val timestamp: Timestamp? = null
)
