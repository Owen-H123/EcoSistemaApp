package com.hannahlab.ecoapp.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentLoginBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.panel.PanelActivity
import com.hannahlab.ecoapp.ui.recuperar.RecuperarContrasenaActivity
import com.hannahlab.ecoapp.util.Seguridad
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.btnIngresar.setOnClickListener { if (isFormValid()) iniciarSesion() }
        binding.tvOlvide.setOnClickListener {
            startActivity(Intent(requireContext(), RecuperarContrasenaActivity::class.java))
        }
    }

    private fun iniciarSesion() {
        val app = requireContext().ecoApp
        val correo = binding.etCorreo.text.toString().trim().lowercase()
        val contrasena = binding.etContrasena.text.toString()

        viewLifecycleOwner.lifecycleScope.launch {
            val usuario = app.database.usuarioDao().findByCorreo(correo)
            if (usuario == null || !usuario.activo || usuario.contrasenaHash != Seguridad.hash(contrasena)) {
                binding.tilContrasena.error = getString(R.string.login_error)
                return@launch
            }
            app.session.iniciar(usuario)
            startActivity(
                Intent(requireContext(), PanelActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
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
