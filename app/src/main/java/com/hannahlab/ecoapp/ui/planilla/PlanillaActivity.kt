package com.hannahlab.ecoapp.ui.planilla

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.databinding.ActivityPlanillaBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.util.CalculoPlanilla
import com.hannahlab.ecoapp.util.Fechas
import com.hannahlab.ecoapp.util.Formato
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/** Formulario del salario de un trabajador. Solo el admin puede cambiar la tarifa. */
class PlanillaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanillaBinding
    private var usuario: Usuario? = null
    private var horas = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = ecoApp
        val usuarioId = intent.getLongExtra(EXTRA_USUARIO_ID, -1L)
        val esAdmin = app.session.esAdmin
        // Un developer solo puede ver su propia planilla
        if (!esAdmin && usuarioId != app.session.usuarioId) {
            finish()
            return
        }

        binding = ActivityPlanillaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.planilla_titulo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvMes.text = Fechas.nombreMes()
        binding.etTarifa.isEnabled = esAdmin
        binding.btnGuardar.isVisible = esAdmin

        lifecycleScope.launch {
            val encontrado = app.database.usuarioDao().findById(usuarioId) ?: run {
                finish()
                return@launch
            }
            usuario = encontrado
            horas = app.database.registroHorasDao()
                .observeHorasUsuario(usuarioId, Fechas.inicioMes(), Fechas.hoyEpoch())
                .first()

            binding.tvInicial.text = encontrado.nombre.take(1).uppercase()
            binding.tvNombre.text = encontrado.nombre
            binding.tvCargo.text = encontrado.cargo
            binding.etTarifa.setText(String.format(Locale.US, "%.2f", encontrado.tarifaHora))
            binding.etHoras.setText(Formato.horas(horas))
            calcular()
        }

        binding.btnCalcular.setOnClickListener { calcular() }
        binding.btnGuardar.setOnClickListener { guardarTarifa() }
    }

    private fun leerTarifa(): Double? {
        val tarifa = binding.etTarifa.text?.toString()?.replace(',', '.')?.toDoubleOrNull()
        val valida = tarifa != null && tarifa > 0
        binding.tilTarifa.error = if (valida) null else getString(R.string.error_tarifa)
        return tarifa.takeIf { valida }
    }

    private fun calcular() {
        val tarifa = leerTarifa() ?: return
        val calculo = CalculoPlanilla(horas, tarifa)
        binding.tvBruto.text = Formato.soles(calculo.bruto)
        binding.tvDescuento.text = "− ${Formato.soles(calculo.descuento)}"
        binding.tvNeto.text = Formato.soles(calculo.neto)
    }

    private fun guardarTarifa() {
        val tarifa = leerTarifa() ?: return
        val actual = usuario ?: return
        lifecycleScope.launch {
            val actualizado = actual.copy(tarifaHora = tarifa)
            ecoApp.database.usuarioDao().update(actualizado)
            usuario = actualizado
            calcular()
            Toast.makeText(this@PlanillaActivity, R.string.planilla_guardada, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val EXTRA_USUARIO_ID = "usuario_id"

        fun intent(context: Context, usuarioId: Long) =
            Intent(context, PlanillaActivity::class.java).putExtra(EXTRA_USUARIO_ID, usuarioId)
    }
}
