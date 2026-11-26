package com.hannahlab.ecoapp.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentLoginBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.acceso.abrirComoInicio
import com.hannahlab.ecoapp.ui.acceso.mensajeDeError
import com.hannahlab.ecoapp.ui.main.MainActivity
import com.hannahlab.ecoapp.ui.recuperar.RecuperarContrasenaActivity
import com.hannahlab.ecoapp.ui.registro.RegistroActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.btnIngresar.setOnClickListener { if (isFormValid()) iniciarSesion() }
        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(requireContext(), RegistroActivity::class.java))
        }
        binding.tvOlvide.setOnClickListener {
            startActivity(Intent(requireContext(), RecuperarContrasenaActivity::class.java))
        }
    }

    private fun iniciarSesion() {
        val auth = requireContext().ecoApp.auth
        val correo = binding.etCorreo.text.toString().trim().lowercase()
        val contrasena = binding.etContrasena.text.toString()

        binding.btnIngresar.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                auth.iniciarSesion(correo, contrasena)
                requireContext().abrirComoInicio(MainActivity::class.java)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                binding.btnIngresar.isEnabled = true
                Toast.makeText(requireContext(), mensajeDeError(e), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isFormValid(): Boolean {
        val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()
        val contrasena = binding.etContrasena.text?.toString().orEmpty()

        binding.tilCorreo.error = when {
            correo.isEmpty() -> getString(R.string.error_obligatorio)
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> getString(R.string.error_correo)
            else -> null
        }
        binding.tilContrasena.error =
            if (contrasena.isEmpty()) getString(R.string.error_obligatorio) else null

        return binding.tilCorreo.error == null && binding.tilContrasena.error == null
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
