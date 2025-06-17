package com.example.picare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.picare.R
import com.example.picare.model.Animal

class AnimalAdapter(
    private val animals: List<Animal>,
    private val onClick: (Animal) -> Unit,
    private val onDelete: (Animal) -> Unit
) : RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    inner class AnimalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.animalName)
        private val typeTv: TextView = itemView.findViewById(R.id.animalType)
        private val descTv: TextView = itemView.findViewById(R.id.animalDescription)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(animal: Animal) {
            nameTv.text = animal.name
            typeTv.text = animal.type
            descTv.text = animal.description
            itemView.setOnClickListener { onClick(animal) }
            deleteBtn.setOnClickListener { onDelete(animal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal_card, parent, false)
        return AnimalViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        holder.bind(animals[position])
    }

    override fun getItemCount() = animals.size
}
