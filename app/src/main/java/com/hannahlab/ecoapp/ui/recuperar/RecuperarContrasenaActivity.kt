package com.hannahlab.ecoapp.ui.recuperar

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.ActivityRecuperarContrasenaBinding

class RecuperarContrasenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarContrasenaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSiguiente.setOnClickListener {
            val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()
            binding.tilCorreo.error = when {
                correo.isEmpty() -> getString(R.string.error_obligatorio)
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> getString(R.string.error_correo)
                else -> null
            }
            if (binding.tilCorreo.error == null) {
                Toast.makeText(this, R.string.recuperar_enviado, Toast.LENGTH_LONG).show()
                finish()
            }
        }
        binding.tvVolver.setOnClickListener { finish() }
    }
}
