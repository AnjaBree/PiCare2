package com.example.picare.ui.home

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picare.R
import com.example.picare.databinding.FragmentHomeBinding
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.example.picare.adapter.AnimalAdapter
import com.example.picare.model.Animal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AnimalAdapter
    private val animalsList = mutableListOf<Animal>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        db = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        binding.recyclerViewAnimals.layoutManager = LinearLayoutManager(requireActivity())
        adapter = AnimalAdapter(
            animalsList,
            onClick = { animal -> showReminderDialog(animal) },
            onDelete = { animal -> deleteAnimal(animal) }
        )

        binding.recyclerViewAnimals.adapter = adapter

        loadAnimals()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.let {
            binding.floatingActionButton.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_home_to_newPetFragment)
            }
        }
    }



    private fun loadAnimals() {
        db.collection("animals")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                animalsList.clear()
                for (doc in documents) {
                    val animal = doc.toObject(Animal::class.java)
                    animalsList.add(animal)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load animals", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAnimal(animal: Animal) {
        db.collection("animals")
            .whereEqualTo("name", animal.name)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    db.collection("animals").document(doc.id).delete()
                }
                Toast.makeText(requireContext(), "${animal.name} deleted", Toast.LENGTH_SHORT).show()
                loadAnimals()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error deleting ${animal.name}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showReminderDialog(animal: Animal) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reminder, null)

        val editReminderText = dialogView.findViewById<EditText>(R.id.editReminderText)
        val buttonSelectTime = dialogView.findViewById<Button>(R.id.buttonSelectTime)
        val textSelectedTime = dialogView.findViewById<TextView>(R.id.textSelectedTime)
        val buttonDone = dialogView.findViewById<Button>(R.id.buttonDone)

        var selectedHour = -1
        var selectedMinute = -1

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        buttonSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedHour = hourOfDay
                    selectedMinute = minute
                    val timeText = String.format("%02d:%02d", hourOfDay, minute)
                    textSelectedTime.text = "Selected time: $timeText"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }

        buttonDone.setOnClickListener {
            val reminderText = editReminderText.text.toString().trim()
            if (reminderText.isEmpty()) {
                editReminderText.error = "Please enter reminder text"
                return@setOnClickListener
            }
            if (selectedHour == -1 || selectedMinute == -1) {
                Toast.makeText(requireContext(), "Please select time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calculate the time for when the reminder will show
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If time has already passed today, set reminder for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }

            // Save reminder to Firestore
            val reminderData = hashMapOf(
                "animalName" to animal.name,
                "reminderText" to reminderText,
                "reminderTime" to calendar.timeInMillis,
                "timestamp" to com.google.firebase.Timestamp(calendar.time) // to help order reminders
            )

            db.collection("reminders")
                .add(reminderData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reminder set for ${animal.name}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    // Optional: Refresh the notification list if you're already viewing the notification screen
                    // If you're using ViewModel, you can trigger it to load reminders here
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to set reminder: ${exception.message}", Toast.LENGTH_LONG).show()
                    Log.e("HomeFragment", "Reminder save failed", exception)
                }
        }

        dialog.show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
