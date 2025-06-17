package com.example.picare.ui.newpet  // adjust package

import PetType
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.picare.R
import com.google.android.material.card.MaterialCardView

class PetTypeAdapter(
    private val types: List<PetType>,
    private val onItemSelected: (PetType) -> Unit
) : RecyclerView.Adapter<PetTypeAdapter.PetTypeViewHolder>() {

    // Holds the currently selected adapter position, or null if none
    private var selectedPosition: Int? = null

    inner class PetTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardPetType)
        val icon: ImageView = itemView.findViewById(R.id.imageTypeIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_type, parent, false)
        return PetTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetTypeViewHolder, position: Int) {
        val petType = types[position]
        // Set icon resource
        holder.icon.setImageResource(petType.iconResId)
        // Accessibility
        holder.icon.contentDescription = petType.name

        // Highlight if this position is selected
        if (selectedPosition == position) {
            // Selected: orange border, thicker
            holder.card.strokeColor = Color.parseColor("#FF8C42")
            // If you want dp-based strokeWidth:
            val strokeDp = 4
            val scale = holder.card.context.resources.displayMetrics.density
            holder.card.strokeWidth = (strokeDp * scale).toInt()
        } else {
            // Default: gray border, thin
            holder.card.strokeColor = Color.parseColor("#E0E0E0")
            val strokeDp = 1
            val scale = holder.card.context.resources.displayMetrics.density
            holder.card.strokeWidth = (strokeDp * scale).toInt()
        }

        // Set click listener
        holder.card.setOnClickListener {
            // Use adapterPosition instead of bindingAdapterPosition
            val adapterPos = holder.adapterPosition
            if (adapterPos == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            if (selectedPosition == adapterPos) {
                return@setOnClickListener
            }
            val previousPos = selectedPosition
            selectedPosition = adapterPos
            if (previousPos != null) {
                notifyItemChanged(previousPos)
            }
            notifyItemChanged(adapterPos)
            onItemSelected(types[adapterPos])
        }
    }

    override fun getItemCount(): Int = types.size

    /** Expose current selected PetType, if needed */
    fun getSelectedType(): PetType? =
        selectedPosition?.let { types[it] }

    /** Optionally expose selectedPosition */
    fun getSelectedPosition(): Int? = selectedPosition

    /** If you need to pre-select something (e.g. after rotation), you can add a method:
    fun setSelectedPosition(pos: Int) {
    selectedPosition = pos
    notifyItemChanged(pos)
    }
     */
}