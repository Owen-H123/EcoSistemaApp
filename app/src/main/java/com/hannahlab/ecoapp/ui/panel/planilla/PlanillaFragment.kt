package com.hannahlab.ecoapp.ui.panel.planilla

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentPlanillaBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.planilla.PlanillaActivity
import com.hannahlab.ecoapp.util.CalculoPlanilla
import com.hannahlab.ecoapp.util.Fechas
import com.hannahlab.ecoapp.util.Formato
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Planilla del mes: el admin ve a todo el equipo, el developer solo su fila. */
class PlanillaFragment : Fragment(R.layout.fragment_planilla) {

    private var _binding: FragmentPlanillaBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlanillaBinding.bind(view)

        val app = requireContext().ecoApp
        val usuarioId = app.session.usuarioId ?: return
        val esAdmin = app.session.esAdmin

        binding.tvMes.text = Fechas.nombreMes()
        binding.tvLabelTotal.setText(if (esAdmin) R.string.planilla_total_admin else R.string.planilla_total_dev)

        val adapter = TrabajadorPlanillaAdapter { usuario ->
            startActivity(PlanillaActivity.intent(requireContext(), usuario.id))
        }
        binding.rvTrabajadores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrabajadores.adapter = adapter

        val db = app.database
        val filas = combine(
            db.usuarioDao().observeActivos(),
            db.registroHorasDao().observeHorasPorUsuario(Fechas.inicioMes(), Fechas.hoyEpoch()),
        ) { equipo, horas ->
            val horasPorId = horas.associate { it.usuarioId to it.horas }
            equipo
                .filter { if (esAdmin) !it.esAdmin else it.id == usuarioId }
                .map { FilaPlanilla(it, CalculoPlanilla(horasPorId[it.id] ?: 0.0, it.tarifaHora)) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                filas.collect { lista ->
                    adapter.submitList(lista)
                    binding.tvTotal.text = Formato.soles(lista.sumOf { it.calculo.neto })
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
