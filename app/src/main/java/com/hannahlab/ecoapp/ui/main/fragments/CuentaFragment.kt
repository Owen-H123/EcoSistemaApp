package com.hannahlab.ecoapp.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentCuentaBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.panel.PanelActivity
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/** Opción "Login" del menú con la sesión iniciada: muestra la cuenta y lleva al panel. */
class CuentaFragment : Fragment(R.layout.fragment_cuenta) {

    private var _binding: FragmentCuentaBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCuentaBinding.bind(view)

        val app = requireContext().ecoApp
        val usuarioId = app.session.usuarioId ?: return

        binding.btnPanel.setOnClickListener {
            startActivity(Intent(requireContext(), PanelActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.database.usuarioDao().observeById(usuarioId).filterNotNull().collect { usuario ->
                    binding.tvInicial.text = usuario.nombre.take(1).uppercase()
                    binding.tvNombre.text = usuario.nombre
                    binding.tvRol.setText(if (usuario.esAdmin) R.string.rol_admin else R.string.rol_developer)
                    binding.tvCorreo.text = usuario.correo
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
