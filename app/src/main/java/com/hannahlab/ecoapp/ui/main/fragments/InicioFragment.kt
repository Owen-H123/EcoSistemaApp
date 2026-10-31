package com.hannahlab.ecoapp.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentInicioBinding
import com.hannahlab.ecoapp.databinding.ItemServicioBinding
import com.hannahlab.ecoapp.ui.main.MainActivity

/** Presentación pública de Hannah Lab con sus seis líneas de servicio. */
class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInicioBinding.bind(view)

        val titulos = resources.getStringArray(R.array.servicios_titulos)
        val descripciones = resources.getStringArray(R.array.servicios_descripciones)
        titulos.zip(descripciones).forEachIndexed { indice, (titulo, descripcion) ->
            val tarjeta = ItemServicioBinding.inflate(layoutInflater, binding.llServicios, true)
            tarjeta.tvNumero.text = "%02d".format(indice + 1)
            tarjeta.tvTitulo.text = titulo
            tarjeta.tvDescripcion.text = descripcion
        }

        binding.btnContacto.setOnClickListener {
            (requireActivity() as MainActivity).navigateTo(R.id.nav_contactos)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
