package com.hannahlab.ecoapp.ui.panel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.ActivityPanelBinding
import com.hannahlab.ecoapp.ecoApp
import com.hannahlab.ecoapp.ui.panel.analisis.AnalisisFragment
import com.hannahlab.ecoapp.ui.panel.inicio.PanelInicioFragment
import com.hannahlab.ecoapp.ui.panel.perfil.PerfilFragment
import com.hannahlab.ecoapp.ui.panel.planilla.PlanillaFragment
import com.hannahlab.ecoapp.ui.welcome.WelcomeActivity

/** Zona privada del trabajador: Inicio, Análisis, Planilla y Perfil. El contenido cambia según el rol. */
class PanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ecoApp.session.usuarioId == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
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
