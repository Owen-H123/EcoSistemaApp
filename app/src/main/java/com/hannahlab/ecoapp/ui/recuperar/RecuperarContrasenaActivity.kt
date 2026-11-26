package com.hannahlab.ecoapp.ui.recuperar

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.ActivityRecuperarContrasenaBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.acceso.mensajeDeError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Pide a Firebase que envíe el correo para restablecer la contraseña. */
class RecuperarContrasenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarContrasenaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSiguiente.setOnClickListener {
            val correo = binding.etCorreo.text?.toString()?.trim()?.lowercase().orEmpty()
            binding.tilCorreo.error = when {
                correo.isEmpty() -> getString(R.string.error_obligatorio)
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> getString(R.string.error_correo)
                else -> null
            }
            if (binding.tilCorreo.error == null) enviar(correo)
        }
        binding.tvVolver.setOnClickListener { finish() }
    }

    private fun enviar(correo: String) {
        binding.btnSiguiente.isEnabled = false
        lifecycleScope.launch {
            try {
                ecoApp.auth.enviarRecuperacion(correo)
                Toast.makeText(this@RecuperarContrasenaActivity, R.string.recuperar_enviado, Toast.LENGTH_LONG).show()
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.btnSiguiente.isEnabled = true
                Toast.makeText(this@RecuperarContrasenaActivity, mensajeDeError(e), Toast.LENGTH_LONG).show()
            }
        }
    }
}
