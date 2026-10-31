package com.hannahlab.ecoapp.ui.planilla

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hannahlab.ecoapp.data.local.entity.Trabajador
import com.hannahlab.ecoapp.databinding.ItemTrabajadorBinding

class TrabajadorAdapter : ListAdapter<Trabajador, TrabajadorAdapter.ViewHolder>(DIFF) {

    class ViewHolder(val binding: ItemTrabajadorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemTrabajadorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trabajador = getItem(position)
        holder.binding.tvNombre.text = trabajador.nombre
        holder.binding.tvCargo.text = trabajador.cargo
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Trabajador>() {
            override fun areItemsTheSame(oldItem: Trabajador, newItem: Trabajador) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Trabajador, newItem: Trabajador) = oldItem == newItem
        }
    }
}
