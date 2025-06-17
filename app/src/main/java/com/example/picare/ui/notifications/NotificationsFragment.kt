package com.example.picare.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.picare.R
import com.example.picare.model.Reminder
import com.example.picare.adapter.ReminderAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val reminders = mutableListOf<Pair<String, Reminder>>()
    private lateinit var newReminderFab: FloatingActionButton;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewNotifications)
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadReminders()


        newReminderFab = view.findViewById(R.id.newReminderFab);

        newReminderFab.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notifications_to_newReminderFragment);
        }


        return view

    }

    private fun loadReminders() {
        db.collection("reminders")
            .get()
            .addOnSuccessListener { result ->
                reminders.clear()
                for (document in result) {
                    val reminder = document.toObject(Reminder::class.java)
                    val id = document.id
                    reminders.add(Pair(id, reminder))
                }
                // Sort reminders by reminderTime ascending (soonest first)
                reminders.sortBy { it.second.reminderTime }  // If reminderTime is Long
                // or if it's Timestamp: reminders.sortBy { it.second.timestamp?.toDate() }

                recyclerView.adapter = ReminderAdapter(reminders)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load reminders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
