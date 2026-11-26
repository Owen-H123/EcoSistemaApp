package com.hannahlab.ecoapp.ui.registro

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.ActivityRegistroBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.acceso.abrirComoInicio
import com.hannahlab.ecoapp.ui.acceso.mensajeDeError
import com.hannahlab.ecoapp.ui.main.MainActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Crea una cuenta nueva y entra a la app. */
class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCrearCuenta.setOnClickListener { registrar() }
        binding.tvYaTengoCuenta.setOnClickListener { finish() }
    }

    private fun registrar() {
        val nombre = binding.etNombre.text?.toString()?.trim().orEmpty()
        val correo = binding.etCorreo.text?.toString()?.trim()?.lowercase().orEmpty()
        val contrasena = binding.etContrasena.text?.toString().orEmpty()
        val repetida = binding.etRepetir.text?.toString().orEmpty()

        val valido = listOf(
            validar(binding.tilNombre, if (nombre.isEmpty()) R.string.error_obligatorio else null),
            validar(
                binding.tilCorreo,
                when {
                    correo.isEmpty() -> R.string.error_obligatorio
                    !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> R.string.error_correo
                    else -> null
                },
            ),
            validar(
                binding.tilContrasena,
                if (contrasena.length < LARGO_MINIMO) R.string.error_contrasena_corta else null,
            ),
            validar(
                binding.tilRepetir,
                if (repetida != contrasena) R.string.error_contrasena_distinta else null,
            ),
        ).all { it }
        if (!valido) return

        binding.btnCrearCuenta.isEnabled = false
        lifecycleScope.launch {
            try {
                ecoApp.auth.registrar(nombre, correo, contrasena)
                Toast.makeText(this@RegistroActivity, R.string.registro_creado, Toast.LENGTH_SHORT).show()
                abrirComoInicio(MainActivity::class.java)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.btnCrearCuenta.isEnabled = true
                Toast.makeText(this@RegistroActivity, mensajeDeError(e), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validar(campo: TextInputLayout, @StringRes error: Int?): Boolean {
        campo.error = error?.let(::getString)
        return error == null
    }

    private companion object {
        const val LARGO_MINIMO = 6
    }
}
