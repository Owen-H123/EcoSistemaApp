package com.hannahlab.ecoapp.ui.horas

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.RegistroHoras
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.databinding.ActivityRegistrarHorasBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.acceso.mensajeDeDatos
import com.hannahlab.ecoapp.util.Fechas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Formulario de horas trabajadas. El admin elige al trabajador; el resto registra las suyas. */
class RegistrarHorasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarHorasBinding
    private var trabajador: Usuario? = null
    private var fecha: LocalDate = Fechas.hoy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val usuarioId = ecoApp.session.usuarioId ?: run {
            finish()
            return
        }
        binding = ActivityRegistrarHorasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.horas_titulo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        savedInstanceState?.let { fecha = LocalDate.ofEpochDay(it.getLong(ESTADO_FECHA, fecha.toEpochDay())) }
        mostrarFecha()
        binding.etFecha.setOnClickListener { elegirFecha() }
        binding.btnGuardar.setOnClickListener { guardar() }
        cargarTrabajador(usuarioId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ESTADO_FECHA, fecha.toEpochDay())
    }

    private fun cargarTrabajador(usuarioId: Long) {
        val esAdmin = ecoApp.session.esAdmin
        binding.grupoTrabajador.isVisible = esAdmin
        val dao = ecoApp.database.usuarioDao()
        lifecycleScope.launch {
            if (!esAdmin) {
                trabajador = dao.findById(usuarioId)
                return@launch
            }
            val equipo = dao.observeActivos().first().filterNot { it.esAdmin }
            binding.actvTrabajador.setAdapter(
                ArrayAdapter(this@RegistrarHorasActivity, android.R.layout.simple_list_item_1, equipo.map { it.nombre }),
            )
            binding.actvTrabajador.setOnItemClickListener { _, _, posicion, _ ->
                trabajador = equipo[posicion]
                binding.tilTrabajador.error = null
            }
        }
    }

    private fun elegirFecha() {
        // El selector de Material trabaja con la medianoche en UTC
        val selector = MaterialDatePicker.Builder.datePicker()
            .setSelection(fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .setCalendarConstraints(
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build(),
            )
            .build()
        selector.addOnPositiveButtonClickListener { milisegundos ->
            fecha = LocalDate.ofEpochDay(Math.floorDiv(milisegundos, MILISEGUNDOS_POR_DIA))
            mostrarFecha()
        }
        selector.show(supportFragmentManager, "fecha")
    }

    private fun mostrarFecha() {
        binding.etFecha.setText(fecha.format(FORMATO_FECHA))
    }

    private fun guardar() {
        val proyecto = texto(binding.etProyecto)
        val actividad = texto(binding.etActividad)
        val horas = texto(binding.etHoras).replace(',', '.').toDoubleOrNull()
        val elegido = trabajador

        val valido = listOf(
            validar(binding.tilTrabajador, if (elegido == null) R.string.error_obligatorio else null),
            validar(binding.tilProyecto, if (proyecto.isEmpty()) R.string.error_obligatorio else null),
            validar(binding.tilActividad, if (actividad.isEmpty()) R.string.error_obligatorio else null),
            validar(binding.tilHoras, if (horas == null || horas <= 0 || horas > 24) R.string.error_horas else null),
        ).all { it }
        if (!valido || elegido == null || horas == null) return

        val registro = RegistroHoras(
            usuarioId = elegido.id,
            fecha = fecha.toEpochDay(),
            proyecto = proyecto,
            actividad = actividad,
            horas = horas,
            completada = binding.swCompletada.isChecked,
        )
        binding.btnGuardar.isEnabled = false
        lifecycleScope.launch {
            try {
                val confirmado = ecoApp.registros.registrar(elegido, registro)
                val aviso = if (confirmado) R.string.horas_guardadas else R.string.aviso_pendiente
                Toast.makeText(this@RegistrarHorasActivity, aviso, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.btnGuardar.isEnabled = true
                Toast.makeText(this@RegistrarHorasActivity, mensajeDeDatos(e), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun texto(campo: TextInputEditText) = campo.text?.toString()?.trim().orEmpty()

    private fun validar(campo: TextInputLayout, @StringRes error: Int?): Boolean {
        campo.error = error?.let(::getString)
        return error == null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private companion object {
        const val ESTADO_FECHA = "fecha"
        const val MILISEGUNDOS_POR_DIA = 86_400_000L
        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Fechas.LOCALE)
    }
}
