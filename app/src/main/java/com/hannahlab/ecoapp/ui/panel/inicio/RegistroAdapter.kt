package com.hannahlab.ecoapp.ui.panel.inicio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.RegistroDetalle
import com.hannahlab.ecoapp.databinding.ItemRegistroBinding
import com.hannahlab.ecoapp.util.Fechas
import com.hannahlab.ecoapp.util.Formato

/** Registros de horas. El admin ve además el nombre de cada persona. */
class RegistroAdapter(private val mostrarNombre: Boolean) :
    ListAdapter<RegistroDetalle, RegistroAdapter.ViewHolder>(DIFF) {

    var tarifas: Map<Long, Double> = emptyMap()

    class ViewHolder(val binding: ItemRegistroBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRegistroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val registro = item.registro
        val context = holder.itemView.context
        val estado = context.getString(if (registro.completada) R.string.estado_completada else R.string.estado_en_curso)

        with(holder.binding) {
            tvInicial.text = (if (mostrarNombre) item.nombreUsuario else registro.proyecto).take(1).uppercase()
            tvActividad.text = registro.actividad
            tvDetalle.text = if (mostrarNombre) "${item.nombreUsuario} · ${registro.proyecto}" else registro.proyecto
            tvFecha.text = "${Fechas.formatoCorto(registro.fecha)} · $estado"
            tvHoras.text = Formato.horas(registro.horas)
            tvMonto.text = Formato.soles(registro.horas * (tarifas[registro.usuarioId] ?: 0.0))
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RegistroDetalle>() {
            override fun areItemsTheSame(oldItem: RegistroDetalle, newItem: RegistroDetalle) =
                oldItem.registro.id == newItem.registro.id

            override fun areContentsTheSame(oldItem: RegistroDetalle, newItem: RegistroDetalle) = oldItem == newItem
        }
    }
}
