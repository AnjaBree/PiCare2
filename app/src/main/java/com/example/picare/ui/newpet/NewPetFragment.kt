package com.example.picare.ui.newpet

import GridSpacingItemDecoration
import PetType
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picare.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class NewPetFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    // Step layouts
    private var step1Layout: LinearLayout? = null
    private var step2Layout: ScrollView? = null

    // Step1 views
    private var recyclerViewPetTypes: RecyclerView? = null
    private lateinit var petTypeAdapter: PetTypeAdapter

    // Selected type
    private var selectedPetType: PetType? = null

    // Step2 views
    private var textSelectedType: TextView? = null
    private var inputName: EditText? = null
    private var inputDescription: EditText? = null
    private var buttonBack: MaterialButton? = null
    private var buttonSavePet: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_pet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        // Bind step layouts
        step1Layout = view.findViewById(R.id.step1Layout)
        step2Layout = view.findViewById(R.id.step2Layout)

        // Step1 view
        recyclerViewPetTypes = view.findViewById(R.id.recyclerViewPetTypes)

        // Step2 views
        textSelectedType = view.findViewById(R.id.textSelectedType)
        inputName = view.findViewById(R.id.inputName)
        inputDescription = view.findViewById(R.id.inputDescription)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonSavePet = view.findViewById(R.id.buttonSavePet)

        setupStep1()
        setupStep2()
    }

    private fun setupStep1() {
        // Prepare pet types
        val types = listOf(
            PetType("Pas", R.drawable.ic_dog),
            PetType("Mačka", R.drawable.ic_cat),
            PetType("Riba", R.drawable.ic_fish),
            PetType("Papagaj", R.drawable.ic_bird),
            PetType("Kornjača", R.drawable.ic_turtle)
        )

        petTypeAdapter = PetTypeAdapter(types) { petType ->
            // Immediately on selection: record and go to step 2
            selectedPetType = petType
            // Transition to step 2
            step1Layout?.visibility = View.GONE
            step2Layout?.visibility = View.VISIBLE
            textSelectedType?.text = "Type: ${petType.name}"
        }

        // GridLayoutManager with 2 columns
        recyclerViewPetTypes?.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerViewPetTypes?.adapter = petTypeAdapter

        // Spacing decoration (optional but recommended)
        val spacingDp = 8
        val spacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            spacingDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        recyclerViewPetTypes?.addItemDecoration(GridSpacingItemDecoration(2, spacingPx, includeEdge = true))
    }

    private fun setupStep2() {
        // Back button: return to step 1
        buttonBack?.setOnClickListener {
            // Show step1 again; keep previous selection highlighted
            step2Layout?.visibility = View.GONE
            step1Layout?.visibility = View.VISIBLE
            // Leave selectedPetType and adapter’s selectedPosition intact so grid remains highlighted
        }
        // Save button
        buttonSavePet?.setOnClickListener {
            val type = selectedPetType?.name ?: ""
            val name = inputName?.text.toString().trim()
            val description = inputDescription?.text.toString().trim()

            if (name.isEmpty()) {
                inputName?.error = "Please enter name"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                inputDescription?.error = "Please enter description"
                return@setOnClickListener
            }
            if (type.isEmpty()) {
                Toast.makeText(requireContext(), "Pet type missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val animalData = hashMapOf(
                "type" to type,
                "name" to name,
                "description" to description,
                "timestamp" to Timestamp.now()
            )
            db.collection("animals")
                .add(animalData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "$type added", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to add: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        step1Layout = null
        step2Layout = null
        recyclerViewPetTypes = null
        textSelectedType = null
        inputName = null
        inputDescription = null
        buttonBack = null
        buttonSavePet = null
    }
}
