package com.hannahlab.ecoapp.ui.main.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ArrayRes
import androidx.fragment.app.Fragment
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentNosotrosBinding
import com.hannahlab.ecoapp.databinding.ItemServicioBinding

/** Quiénes somos, cómo trabajamos, valores y fortalezas de Hannah Lab. */
class NosotrosFragment : Fragment(R.layout.fragment_nosotros) {

    private var _binding: FragmentNosotrosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNosotrosBinding.bind(view)

        llenarTarjetas(binding.llValores, R.array.valores_titulos, R.array.valores_descripciones)
        llenarTarjetas(binding.llFortalezas, R.array.fortalezas_titulos, R.array.fortalezas_descripciones)
    }

    private fun llenarTarjetas(contenedor: LinearLayout, @ArrayRes titulos: Int, @ArrayRes descripciones: Int) {
        resources.getStringArray(titulos).zip(resources.getStringArray(descripciones))
            .forEachIndexed { indice, (titulo, descripcion) ->
                val tarjeta = ItemServicioBinding.inflate(layoutInflater, contenedor, true)
                tarjeta.tvNumero.text = "%02d".format(indice + 1)
                tarjeta.tvTitulo.text = titulo
                tarjeta.tvDescripcion.text = descripcion
            }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
