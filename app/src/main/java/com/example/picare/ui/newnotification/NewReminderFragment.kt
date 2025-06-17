package com.example.picare.ui.newnotification  // adjust package as needed

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.picare.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class NewReminderFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    // Views
    private var autoCompletePet: AutoCompleteTextView? = null
    private var layoutSelectPet: TextInputLayout? = null

    private var inputReminderText: TextInputEditText? = null
    private var layoutReminderText: TextInputLayout? = null

    private var inputReminderTime: TextInputEditText? = null
    private var layoutSelectTime: TextInputLayout? = null

    private var buttonSaveReminder: MaterialButton? = null

    // Data
    private val petNames = mutableListOf<String>()
    private var selectedPetName: String? = null

    // Time selection
    private var selectedHour = -1
    private var selectedMinute = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        // Bind views
        autoCompletePet = view.findViewById(R.id.autoCompletePet)
        layoutSelectPet = view.findViewById(R.id.layoutSelectPet)

        inputReminderText = view.findViewById(R.id.inputReminderText)
        layoutReminderText = view.findViewById(R.id.layoutReminderText)

        inputReminderTime = view.findViewById(R.id.inputReminderTime)
        layoutSelectTime = view.findViewById(R.id.layoutSelectTime)

        buttonSaveReminder = view.findViewById(R.id.buttonSaveReminder)

        setupPetDropdown()
        setupTimePicker()
        setupSaveButton()
    }

    private fun setupPetDropdown() {
        // Load pet names from Firestore
        db.collection("animals")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                petNames.clear()
                for (doc in documents) {
                    val name = doc.getString("name")
                    if (!name.isNullOrBlank()) {
                        petNames.add(name)
                    }
                }
                if (petNames.isEmpty()) {
                    // No pets: show message and disable dropdown
                    layoutSelectPet?.error = "No pets available. Add a pet first."
                    autoCompletePet?.setText("") // clear
                    autoCompletePet?.isEnabled = false
                } else {
                    layoutSelectPet?.error = null
                    autoCompletePet?.isEnabled = true
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        petNames
                    )
                    autoCompletePet?.setAdapter(adapter)
                    // When user taps the field or arrow, the dropdown appears
                    autoCompletePet?.setOnItemClickListener { parent, _, position, _ ->
                        selectedPetName = parent.getItemAtPosition(position) as String
                        layoutSelectPet?.error = null
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load pets: ${e.message}", Toast.LENGTH_LONG).show()
                layoutSelectPet?.error = "Error loading pets"
                autoCompletePet?.isEnabled = false
            }
    }

    private fun setupTimePicker() {
        // When user taps the time field or end icon, show TimePickerDialog
        inputReminderTime?.setOnClickListener {
            showTimePicker()
        }
        // Also allow tapping the end icon
        layoutSelectTime?.setEndIconOnClickListener {
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                val timeText = String.format("%02d:%02d", hourOfDay, minute)
                inputReminderTime?.setText(timeText)
                layoutSelectTime?.error = null
            },
            initialHour,
            initialMinute,
            true
        )
        timePicker.show()
    }

    private fun setupSaveButton() {
        buttonSaveReminder?.setOnClickListener {
            val petName = autoCompletePet?.text?.toString()?.trim()
            val reminderText = inputReminderText?.text?.toString()?.trim()
            val timeText = inputReminderTime?.text?.toString()?.trim()

            // Validate pet
            if (petName.isNullOrEmpty()) {
                layoutSelectPet?.error = "Please select a pet"
                autoCompletePet?.requestFocus()
                return@setOnClickListener
            }
            // Ensure the petName is one from the list
            if (!petNames.contains(petName)) {
                layoutSelectPet?.error = "Select a valid pet"
                autoCompletePet?.requestFocus()
                return@setOnClickListener
            }
            // Validate reminder text
            if (reminderText.isNullOrEmpty()) {
                layoutReminderText?.error = "Please enter reminder text"
                inputReminderText?.requestFocus()
                return@setOnClickListener
            } else {
                layoutReminderText?.error = null
            }
            // Validate time
            if (timeText.isNullOrEmpty() || selectedHour == -1 || selectedMinute == -1) {
                layoutSelectTime?.error = "Please select time"
                inputReminderTime?.requestFocus()
                return@setOnClickListener
            } else {
                layoutSelectTime?.error = null
            }

            // Compute exact reminderTime in millis: today or tomorrow if time passed
            val now = Calendar.getInstance()
            val reminderCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DATE, 1)
                }
            }
            val reminderMillis = reminderCal.timeInMillis

            // Prepare data
            val reminderData = hashMapOf(
                "animalName" to petName,
                "reminderText" to reminderText,
                "reminderTime" to reminderMillis,
                "timestamp" to Timestamp(reminderCal.time)
            )

            // Save to Firestore
            db.collection("reminders")
                .add(reminderData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reminder set for $petName", Toast.LENGTH_SHORT).show()
                    // Navigate back
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to set reminder: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoCompletePet = null
        layoutSelectPet = null
        inputReminderText = null
        layoutReminderText = null
        inputReminderTime = null
        layoutSelectTime = null
        buttonSaveReminder = null
    }
}
