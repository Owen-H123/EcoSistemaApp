package com.hannahlab.ecoapp.ui.panel.perfil

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.FragmentPerfilBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.trabajador.TrabajadorFormActivity
import com.hannahlab.ecoapp.ui.welcome.WelcomeActivity
import com.hannahlab.ecoapp.util.Formato
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPerfilBinding.bind(view)

        val app = requireContext().ecoApp
        val usuarioId = app.session.usuarioId ?: return

        binding.btnNuevoTrabajador.isVisible = app.session.esAdmin
        binding.btnNuevoTrabajador.setOnClickListener {
            startActivity(Intent(requireContext(), TrabajadorFormActivity::class.java))
        }
        binding.btnCerrarSesion.setOnClickListener {
            app.session.cerrar()
            startActivity(
                Intent(requireContext(), WelcomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.database.usuarioDao().observeById(usuarioId).filterNotNull().collect { usuario ->
                    binding.tvInicial.text = usuario.nombre.take(1).uppercase()
                    binding.tvNombre.text = usuario.nombre
                    binding.tvRol.setText(if (usuario.esAdmin) R.string.rol_admin else R.string.rol_developer)
                    binding.tvCorreo.text = usuario.correo
                    binding.tvCargo.text = usuario.cargo
                    binding.tvMeta.text = Formato.horas(usuario.metaHoras)
                    binding.tvTarifa.text = Formato.soles(usuario.tarifaHora)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
