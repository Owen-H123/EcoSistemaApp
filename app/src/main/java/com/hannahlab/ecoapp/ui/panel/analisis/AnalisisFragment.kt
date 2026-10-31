package com.hannahlab.ecoapp.ui.panel.analisis

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.RegistroDetalle
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.databinding.FragmentAnalisisBinding
import com.hannahlab.ecoapp.databinding.ItemBarraBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.util.Fechas
import com.hannahlab.ecoapp.util.Formato
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/** Horas del mes por semana, proyecto y persona. Equivale al Reporte de horas del ecosistema web. */
class AnalisisFragment : Fragment(R.layout.fragment_analisis) {

    private var _binding: FragmentAnalisisBinding? = null
    private val binding get() = _binding!!

    /** Usuario filtrado; nulo = todo el equipo (solo admin). */
    private val filtro = MutableStateFlow<Long?>(null)
    private var equipo: List<Usuario> = emptyList()
    private var registros: List<RegistroDetalle> = emptyList()
    private var esAdmin = false

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnalisisBinding.bind(view)

        val app = requireContext().ecoApp
        val usuarioId = app.session.usuarioId ?: return
        esAdmin = app.session.esAdmin
        if (!esAdmin) filtro.value = usuarioId

        binding.tvMes.text = Fechas.nombreMes()
        binding.tilFiltro.isVisible = esAdmin
        configurarGrafico()

        val db = app.database
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    db.usuarioDao().observeActivos().collect {
                        equipo = it
                        if (esAdmin) configurarFiltro()
                        render()
                    }
                }
                launch {
                    filtro.flatMapLatest { id ->
                        db.registroHorasDao().observeDetalle(id, Fechas.inicioMes(), Fechas.hoyEpoch())
                    }.collect {
                        registros = it
                        render()
                    }
                }
            }
        }
    }

    private fun configurarFiltro() {
        val developers = equipo.filterNot { it.esAdmin }
        val opciones = listOf(getString(R.string.analisis_todo_equipo)) + developers.map { it.nombre }
        binding.actvFiltro.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, opciones))
        if (binding.actvFiltro.text.isNullOrEmpty()) binding.actvFiltro.setText(opciones.first(), false)
        binding.actvFiltro.setOnItemClickListener { _, _, posicion, _ ->
            filtro.value = if (posicion == 0) null else developers[posicion - 1].id
        }
    }

    private fun render() {
        if (_binding == null) return
        val id = filtro.value
        val meta = if (id == null) {
            equipo.filterNot { it.esAdmin }.sumOf { it.metaHoras }
        } else {
            equipo.find { it.id == id }?.metaHoras ?: 0.0
        }
        val total = registros.sumOf { it.registro.horas }
        binding.tvTotal.text = getString(R.string.analisis_total, Formato.horas(total), Formato.horas(meta))

        // Barras por semana del mes contra la meta repartida en semanas
        val semanas = (Fechas.diasDelMes() + 6) / 7
        val valores = FloatArray(semanas)
        registros.forEach { valores[Fechas.semanaDelMes(it.registro.fecha)] += it.registro.horas.toFloat() }
        val metaSemanal = (meta / semanas).toFloat()
        mostrarGrafico(
            etiquetas = (1..semanas).map { "S$it" },
            valores = valores.toList(),
            metas = List(semanas) { metaSemanal },
        )

        renderBarras(binding.llProyectos, agrupar { it.registro.proyecto })

        val verPersonas = esAdmin && id == null
        binding.tvTituloPersonas.isVisible = verPersonas
        binding.llPersonas.isVisible = verPersonas
        if (verPersonas) renderBarras(binding.llPersonas, agrupar { it.nombreUsuario })
    }

    /** Estilo del gráfico (MPAndroidChart) con la paleta y tipografía de la app. */
    private fun configurarGrafico() {
        val inter = ResourcesCompat.getFont(requireContext(), R.font.inter_400)
        val gris = requireContext().getColor(R.color.eco_texto_suave)
        with(binding.chartSemanas) {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setNoDataText(getString(R.string.analisis_sin_datos))
            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                textColor = gris
                typeface = inter
                gridColor = requireContext().getColor(R.color.eco_borde)
                setDrawAxisLine(false)
                enableGridDashedLine(12f, 12f, 0f)
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = gris
                typeface = inter
                setDrawGridLines(false)
                setCenterAxisLabels(true)
            }
        }
    }

    /** Barras agrupadas por semana: meta (gris) y horas registradas (lima). */
    private fun mostrarGrafico(etiquetas: List<String>, valores: List<Float>, metas: List<Float>) {
        val contexto = requireContext()
        val meta = BarDataSet(metas.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }, getString(R.string.analisis_leyenda_meta))
            .apply {
                color = contexto.getColor(R.color.eco_meta)
                setDrawValues(false)
            }
        val horas = BarDataSet(valores.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }, getString(R.string.analisis_leyenda_horas))
            .apply {
                color = contexto.getColor(R.color.eco_lima_oscuro)
                setDrawValues(false)
            }

        // (ancho + espacio) × 2 barras + espacio de grupo = 1 unidad por semana
        val espacioGrupo = 0.3f
        val espacioBarra = 0.05f
        val datos = BarData(meta, horas).apply { barWidth = 0.3f }

        with(binding.chartSemanas) {
            data = datos
            xAxis.valueFormatter = IndexAxisValueFormatter(etiquetas)
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = datos.getGroupWidth(espacioGrupo, espacioBarra) * etiquetas.size
            groupBars(0f, espacioGrupo, espacioBarra)
            animateY(600)
            invalidate()
        }
    }

    private fun agrupar(clave: (RegistroDetalle) -> String): List<Pair<String, Double>> =
        registros.groupBy(clave)
            .map { (nombre, lista) -> nombre to lista.sumOf { it.registro.horas } }
            .sortedByDescending { it.second }

    private fun renderBarras(contenedor: LinearLayout, datos: List<Pair<String, Double>>) {
        contenedor.removeAllViews()
        if (datos.isEmpty()) {
            contenedor.addView(TextView(requireContext()).apply {
                setText(R.string.analisis_sin_datos)
                setTextColor(requireContext().getColor(R.color.eco_texto_suave))
            })
            return
        }
        val maximo = datos.maxOf { it.second }
        datos.forEach { (nombre, horas) ->
            val fila = ItemBarraBinding.inflate(layoutInflater, contenedor, true)
            fila.tvNombre.text = nombre
            fila.tvValor.text = Formato.horas(horas)
            fila.progress.progress = (horas / maximo * 100).toInt()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
