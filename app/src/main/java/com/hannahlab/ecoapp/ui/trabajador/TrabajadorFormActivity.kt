package com.hannahlab.ecoapp.ui.trabajador

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.Usuario
import com.hannahlab.ecoapp.databinding.ActivityTrabajadorFormBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.util.Seguridad
import kotlinx.coroutines.launch

/** Alta de un developer. Solo el admin entra aquí (las cuentas no se crean desde el login). */
class TrabajadorFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrabajadorFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ecoApp.session.esAdmin) {
            finish()
            return
        }
        binding = ActivityTrabajadorFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.trabajador_titulo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnGuardar.setOnClickListener { guardar() }
    }

    private fun guardar() {
        val nombre = texto(binding.etNombre)
        val correo = texto(binding.etCorreo).lowercase()
        val contrasena = binding.etContrasena.text?.toString().orEmpty()
        val cargo = texto(binding.etCargo)
        val tarifa = numero(binding.etTarifa)
        val meta = numero(binding.etMeta)

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
            validar(binding.tilContrasena, if (contrasena.length < 6) R.string.error_contrasena_corta else null),
            validar(binding.tilCargo, if (cargo.isEmpty()) R.string.error_obligatorio else null),
            validar(binding.tilTarifa, if (tarifa == null || tarifa <= 0) R.string.error_tarifa else null),
            validar(binding.tilMeta, if (meta == null || meta <= 0) R.string.error_numero else null),
        ).all { it }
        if (!valido) return

        lifecycleScope.launch {
            try {
                ecoApp.database.usuarioDao().insert(
                    Usuario(
                        nombre = nombre,
                        correo = correo,
                        contrasenaHash = Seguridad.hash(contrasena),
                        rol = Usuario.ROL_DEVELOPER,
                        cargo = cargo,
                        tarifaHora = tarifa!!,
                        metaHoras = meta!!,
                    )
                )
                Toast.makeText(this@TrabajadorFormActivity, R.string.trabajador_creado, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: SQLiteConstraintException) {
                binding.tilCorreo.error = getString(R.string.error_correo_duplicado)
            }
        }
    }

    private fun texto(campo: TextInputEditText) = campo.text?.toString()?.trim().orEmpty()

    private fun numero(campo: TextInputEditText) = texto(campo).replace(',', '.').toDoubleOrNull()

    private fun validar(campo: TextInputLayout, error: Int?): Boolean {
        campo.error = error?.let(::getString)
        return error == null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
