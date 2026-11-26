package com.hannahlab.ecoapp.ui.panel

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.ActivityPanelBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.acceso.LoginActivity
import com.hannahlab.ecoapp.ui.acceso.abrirComoInicio
import com.hannahlab.ecoapp.ui.acceso.mensajeDeDatos
import com.hannahlab.ecoapp.ui.panel.analisis.AnalisisFragment
import com.hannahlab.ecoapp.ui.panel.inicio.PanelInicioFragment
import com.hannahlab.ecoapp.ui.panel.perfil.PerfilFragment
import com.hannahlab.ecoapp.ui.panel.planilla.PlanillaFragment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Zona privada del trabajador: Inicio, Análisis, Planilla y Perfil. El contenido cambia según el rol. */
class PanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ecoApp.auth.haySesion) {
            abrirComoInicio(LoginActivity::class.java)
            finish()
            return
        }
        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            showSection(item.itemId)
            true
        }
        binding.bottomNav.setOnItemReselectedListener { }

        if (savedInstanceState == null) showSection(R.id.panel_inicio)
        mantenerDatosAlDia()
    }

    /** Mientras el panel está a la vista, trae de Firestore los cambios en fichas y horas. */
    private fun mantenerDatosAlDia() {
        val usuarioId = ecoApp.session.usuarioId ?: return
        lifecycleScope.launch {
            ecoApp.registros.preparar(usuarioId)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    ecoApp.registros.sincronizar(usuarioId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Las pantallas siguen con la última copia local
                    Toast.makeText(this@PanelActivity, mensajeDeDatos(e), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSection(itemId: Int) {
        val fragment = when (itemId) {
            R.id.panel_analisis -> AnalisisFragment()
            R.id.panel_planilla -> PlanillaFragment()
            R.id.panel_perfil -> PerfilFragment()
            else -> PanelInicioFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.panel_container, fragment)
            .commit()
    }
}
