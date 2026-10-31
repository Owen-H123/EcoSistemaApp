package com.hannahlab.ecoapp.ui.panel.inicio

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.RegistroDetalle
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.databinding.FragmentPanelInicioBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.util.CalculoPlanilla
import com.hannahlab.ecoapp.util.Fechas
import com.hannahlab.ecoapp.util.Formato
import com.hannahlab.ecoapp.util.Periodo
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Resumen del mes: del propio trabajador (developer) o de todo el equipo (admin). */
class PanelInicioFragment : Fragment(R.layout.fragment_panel_inicio) {

    private var _binding: FragmentPanelInicioBinding? = null
    private val binding get() = _binding!!

    private data class Estado(val usuario: Usuario, val equipo: List<Usuario>, val registros: List<RegistroDetalle>)

    private var estado: Estado? = null
    private var periodo = Periodo.SEMANA
    private lateinit var adapter: RegistroAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPanelInicioBinding.bind(view)

        val app = requireContext().ecoApp
        val usuarioId = app.session.usuarioId ?: return
        val esAdmin = app.session.esAdmin

        adapter = RegistroAdapter(mostrarNombre = esAdmin)
        binding.rvRegistros.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRegistros.adapter = adapter

        binding.togglePeriodo.check(R.id.btn_semana)
        binding.togglePeriodo.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            periodo = when (checkedId) {
                R.id.btn_semana -> Periodo.SEMANA
                R.id.btn_mes -> Periodo.MES
                else -> Periodo.HOY
            }
            estado?.let(::renderLista)
        }

        // Un solo rango que cubre el mes y la semana pasada; el resto se filtra en memoria
        val desde = minOf(Fechas.inicioMes(), Fechas.semanaPasada().first)
        val db = app.database
        val flujo = combine(
            db.usuarioDao().observeById(usuarioId),
            db.usuarioDao().observeActivos(),
            db.registroHorasDao().observeDetalle(if (esAdmin) null else usuarioId, desde, Fechas.hoyEpoch()),
        ) { usuario, equipo, registros -> usuario?.let { Estado(it, equipo, registros) } }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flujo.collect { nuevo ->
                    if (nuevo != null) {
                        estado = nuevo
                        render(nuevo)
                    }
                }
            }
        }
    }

    private fun render(estado: Estado) {
        val usuario = estado.usuario
        val registrosMes = estado.registros.filter { it.registro.fecha >= Fechas.inicioMes() }
        val horasMes = registrosMes.sumOf { it.registro.horas }
        val tarifas = estado.equipo.associate { it.id to it.tarifaHora }

        binding.tvSaludo.setText(Fechas.saludo())
        binding.tvNombre.text = getString(R.string.panel_hola, usuario.nombre.substringBefore(' '))

        if (usuario.esAdmin) {
            val netoEquipo = registrosMes.groupBy { it.registro.usuarioId }.entries.sumOf { (id, registros) ->
                CalculoPlanilla(registros.sumOf { it.registro.horas }, tarifas[id] ?: 0.0).neto
            }
            binding.tvLabel1.setText(R.string.panel_horas_equipo)
            binding.tvValor1.text = Formato.horas(horasMes)
            binding.tvLabel2.setText(R.string.panel_planilla_mes)
            binding.tvValor2.text = Formato.soles(netoEquipo)
            renderMeta(horasMes, estado.equipo.filterNot { it.esAdmin }.sumOf { it.metaHoras }, R.string.panel_meta_equipo)
        } else {
            binding.tvLabel1.setText(R.string.panel_horas_mes)
            binding.tvValor1.text = Formato.horas(horasMes)
            binding.tvLabel2.setText(R.string.panel_neto_estimado)
            binding.tvValor2.text = Formato.soles(CalculoPlanilla(horasMes, usuario.tarifaHora).neto)
            renderMeta(horasMes, usuario.metaHoras, R.string.panel_meta_propia)
        }

        val semanaPasada = Fechas.semanaPasada()
        binding.tvSemanaPasada.text = Formato.horas(
            estado.registros.filter { it.registro.fecha in semanaPasada }.sumOf { it.registro.horas }
        )
        binding.tvCompletadas.text = registrosMes.count { it.registro.completada }.toString()

        adapter.tarifas = tarifas
        renderLista(estado)
    }

    private fun renderMeta(horas: Double, meta: Double, @StringRes texto: Int) {
        val porcentaje = if (meta > 0) (horas / meta * 100).roundToInt() else 0
        binding.progressMeta.setProgressCompat(porcentaje.coerceAtMost(100), true)
        binding.progressCirculo.setProgressCompat(porcentaje.coerceAtMost(100), true)
        binding.tvPorcentaje.text = "$porcentaje%"
        binding.tvMeta.text = getString(texto, porcentaje, Formato.horas(meta))
    }

    private fun renderLista(estado: Estado) {
        val rango = Fechas.rango(periodo)
        val lista = estado.registros.filter { it.registro.fecha in rango }
        adapter.submitList(lista)
        binding.tvVacio.isVisible = lista.isEmpty()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
