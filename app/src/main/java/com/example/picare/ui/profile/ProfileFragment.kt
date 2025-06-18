package com.example.picare.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.picare.LoginActivity
import com.example.picare.R
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val displayNameTextView = view.findViewById<TextView>(R.id.displayName)
        val emailTextView = view.findViewById<TextView>(R.id.email)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        displayNameTextView.text = user?.displayName ?: "No Name"
        emailTextView.text = user?.email ?: "No Email"

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view
    }
}
