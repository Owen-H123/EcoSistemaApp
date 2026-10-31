package com.hannahlab.ecoapp.ui.planilla

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.ActivityPlanillaBinding

class PlanillaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanillaBinding
    private val adapter = TrabajadorAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanillaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.planilla_titulo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvTrabajadores.layoutManager = LinearLayoutManager(this)
        binding.rvTrabajadores.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
