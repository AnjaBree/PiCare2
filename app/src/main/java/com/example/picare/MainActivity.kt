package com.example.picare

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.picare.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore // Firestore instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_notifications
            )
        )

        navView.setupWithNavController(navController)
    }

    private fun showAnimalInputDialog(animalType: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_animal_input, null)

        val inputName = dialogView.findViewById<EditText>(R.id.inputName)
        val inputDescription = dialogView.findViewById<EditText>(R.id.inputDescription)

        AlertDialog.Builder(this)
            .setTitle("Add $animalType")
            .setView(dialogView)
            .setPositiveButton("Done") { dialog, _ ->
                val name = inputName.text.toString().trim()
                val description = inputDescription.text.toString().trim()

                if (name.isNotEmpty() && description.isNotEmpty()) {
                    saveAnimalToFirebase(name, animalType, description)
                } else {
                    Toast.makeText(this, "Name and Description are required", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveAnimalToFirebase(name: String, type: String, description: String) {
        val animal = hashMapOf(
            "name" to name,
            "type" to type,
            "description" to description,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("animals")
            .add(animal)
            .addOnSuccessListener {
                Toast.makeText(this, "Animal added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to add animal: ${exception.message}", Toast.LENGTH_LONG).show()
                exception.printStackTrace()
            }

    }
}
