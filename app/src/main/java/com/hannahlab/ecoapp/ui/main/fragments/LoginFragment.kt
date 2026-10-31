package com.hannahlab.ecoapp.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentLoginBinding
import com.hannahlab.ecoapp.ui.planilla.PlanillaActivity
import com.hannahlab.ecoapp.ui.recuperar.RecuperarContrasenaActivity

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.btnIngresar.setOnClickListener {
            if (isFormValid()) {
                // TODO: validar credenciales contra Room cuando se defina el login
                startActivity(Intent(requireContext(), PlanillaActivity::class.java))
            }
        }
        binding.tvOlvide.setOnClickListener {
            startActivity(Intent(requireContext(), RecuperarContrasenaActivity::class.java))
        }
    }

    private fun isFormValid(): Boolean {
        val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()
        val contrasena = binding.etContrasena.text?.toString().orEmpty()

        binding.tilCorreo.error = when {
            correo.isEmpty() -> getString(R.string.error_obligatorio)
            correo.contains('@') && !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                getString(R.string.error_correo)
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
