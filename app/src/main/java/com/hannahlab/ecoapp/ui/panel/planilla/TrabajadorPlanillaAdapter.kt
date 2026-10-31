package com.hannahlab.ecoapp.ui.panel.planilla

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.databinding.ItemTrabajadorBinding
import com.hannahlab.ecoapp.util.CalculoPlanilla
import com.hannahlab.ecoapp.util.Formato

data class FilaPlanilla(val usuario: Usuario, val calculo: CalculoPlanilla)

class TrabajadorPlanillaAdapter(private val onClick: (Usuario) -> Unit) :
    ListAdapter<FilaPlanilla, TrabajadorPlanillaAdapter.ViewHolder>(DIFF) {

    class ViewHolder(val binding: ItemTrabajadorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemTrabajadorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (usuario, calculo) = getItem(position)
        with(holder.binding) {
            tvInicial.text = usuario.nombre.take(1).uppercase()
            tvNombre.text = usuario.nombre
            tvCargo.text = usuario.cargo
            tvDetalle.text = root.context.getString(
                R.string.planilla_detalle_fila, Formato.horas(calculo.horas), Formato.soles(usuario.tarifaHora)
            )
            tvNeto.text = Formato.soles(calculo.neto)
            root.setOnClickListener { onClick(usuario) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FilaPlanilla>() {
            override fun areItemsTheSame(oldItem: FilaPlanilla, newItem: FilaPlanilla) = oldItem.usuario.id == newItem.usuario.id
            override fun areContentsTheSame(oldItem: FilaPlanilla, newItem: FilaPlanilla) = oldItem == newItem
        }
    }
}
