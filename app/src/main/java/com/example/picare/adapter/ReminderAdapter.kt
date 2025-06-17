package com.example.picare.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.example.picare.R
import com.example.picare.model.Reminder

// ReminderAdapter now takes a MutableList so we can remove items dynamically
class ReminderAdapter(
    private val reminders: MutableList<Pair<String, Reminder>>
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val buttonComplete: ImageButton = itemView.findViewById(R.id.buttonComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val (docId, reminder) = reminders[position]

        holder.title.text = reminder.animalName
        holder.description.text = reminder.reminderText

        // Color based on timestamp
        val now = Timestamp.now()
        reminder.timestamp?.let {
            if (it.toDate().before(now.toDate())) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#A5D6A7")) // greenish
            } else {
                holder.cardView.setCardBackgroundColor(Color.GRAY)
            }
        } ?: holder.cardView.setCardBackgroundColor(Color.GRAY)

        holder.buttonComplete.setOnClickListener {
            FirebaseFirestore.getInstance()
                .collection("reminders")
                .document(docId)
                .delete()
                .addOnSuccessListener {
                    // Remove item from local list and notify adapter
                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        reminders.removeAt(pos)
                        notifyItemRemoved(pos)
                    }
                    Toast.makeText(
                        holder.itemView.context,
                        "Reminder completed and removed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        holder.itemView.context,
                        "Failed to delete reminder: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun getItemCount(): Int = reminders.size
}
