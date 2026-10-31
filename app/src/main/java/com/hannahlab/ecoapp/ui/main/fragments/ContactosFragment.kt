package com.hannahlab.ecoapp.ui.main.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.data.local.entity.MensajeContacto
import com.hannahlab.ecoapp.data.repository.LocalContactoRepository
import com.hannahlab.ecoapp.databinding.FragmentContactosBinding
import com.hannahlab.ecoapp.databinding.ItemDatoContactoBinding
import com.hannahlab.ecoapp.ecoApp
import kotlinx.coroutines.launch

/** Datos públicos de contacto y formulario que guarda el mensaje en la base local. */
class ContactosFragment : Fragment(R.layout.fragment_contactos) {

    private var _binding: FragmentContactosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentContactosBinding.bind(view)

        val correoEmpresa = getString(R.string.contacto_correo)
        configurarDato(binding.datoDireccion, R.drawable.ic_ubicacion, R.string.contacto_direccion_label, R.string.contacto_direccion) {
            abrir(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(getString(R.string.contacto_direccion)))))
        }
        configurarDato(binding.datoCorreo, R.drawable.ic_correo, R.string.contacto_correo_label, R.string.contacto_correo) {
            abrir(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correoEmpresa")))
        }
        configurarDato(binding.datoWeb, R.drawable.ic_web, R.string.contacto_web_label, R.string.contacto_web) {
            abrir(Intent(Intent.ACTION_VIEW, Uri.parse("https://" + getString(R.string.contacto_web))))
        }

        val app = requireContext().ecoApp
        val repositorio = LocalContactoRepository(app.database.mensajeContactoDao())

        binding.btnEnviar.setOnClickListener {
            val mensaje = leerFormulario() ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                repositorio.enviar(mensaje)
                Toast.makeText(requireContext(), R.string.contacto_guardado, Toast.LENGTH_SHORT).show()
                limpiarFormulario()
            }
        }
        binding.btnEnviarCorreo.setOnClickListener {
            val mensaje = leerFormulario() ?: return@setOnClickListener
            val cuerpo = buildString {
                appendLine(mensaje.mensaje)
                appendLine()
                appendLine("${mensaje.nombre} · ${mensaje.correo}")
                if (mensaje.telefono.isNotEmpty()) appendLine(mensaje.telefono)
            }
            abrir(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                    .putExtra(Intent.EXTRA_EMAIL, arrayOf(correoEmpresa))
                    .putExtra(Intent.EXTRA_SUBJECT, mensaje.asunto)
                    .putExtra(Intent.EXTRA_TEXT, cuerpo)
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.database.mensajeContactoDao().observeAll().collect { mensajes ->
                    binding.tvRegistrados.text = getString(R.string.contacto_registrados, mensajes.size)
                }
            }
        }
    }

    private fun configurarDato(
        dato: ItemDatoContactoBinding,
        @DrawableRes icono: Int,
        @StringRes label: Int,
        @StringRes valor: Int,
        onClick: () -> Unit,
    ) {
        dato.ivIcono.setImageResource(icono)
        dato.tvLabel.setText(label)
        dato.tvValor.setText(valor)
        dato.root.setOnClickListener { onClick() }
    }

    /** Valida el formulario; devuelve el mensaje listo o null si hay errores. */
    private fun leerFormulario(): MensajeContacto? {
        val nombre = texto(binding.etNombre)
        val correo = texto(binding.etCorreo)
        val telefono = texto(binding.etTelefono)
        val asunto = texto(binding.etAsunto)
        val mensaje = texto(binding.etMensaje)

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
                binding.tilTelefono,
                if (telefono.isNotEmpty() && !Patterns.PHONE.matcher(telefono).matches()) R.string.error_telefono else null,
            ),
            validar(binding.tilAsunto, if (asunto.isEmpty()) R.string.error_obligatorio else null),
            validar(binding.tilMensaje, if (mensaje.length < 10) R.string.error_mensaje_corto else null),
        ).all { it }

        return if (valido) MensajeContacto(nombre = nombre, correo = correo, telefono = telefono, asunto = asunto, mensaje = mensaje) else null
    }

    private fun limpiarFormulario() {
        listOf(binding.etNombre, binding.etCorreo, binding.etTelefono, binding.etAsunto, binding.etMensaje)
            .forEach { it.text = null }
    }

    private fun texto(campo: TextInputEditText) = campo.text?.toString()?.trim().orEmpty()

    private fun validar(campo: TextInputLayout, @StringRes error: Int?): Boolean {
        campo.error = error?.let(::getString)
        return error == null
    }

    private fun abrir(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.contacto_sin_app, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
