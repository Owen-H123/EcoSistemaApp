package com.hannahlab.ecoapp.ui.main

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.hannahlab.ecoapp.R
import com.hannahlab.ecoapp.databinding.ActivityMainBinding
import com.hannahlab.ecoapp.ui.main.fragments.ContactosFragment
import com.hannahlab.ecoapp.ui.main.fragments.InicioFragment
import com.hannahlab.ecoapp.ui.main.fragments.LoginFragment
import com.hannahlab.ecoapp.ui.main.fragments.NosotrosFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.drawer_open, R.string.drawer_close,
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { item ->
            showSection(item.itemId)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        if (savedInstanceState == null) {
            binding.navView.setCheckedItem(R.id.nav_inicio)
            showSection(R.id.nav_inicio)
        }
    }

    private fun showSection(itemId: Int) {
        val (fragment, title) = when (itemId) {
            R.id.nav_nosotros -> NosotrosFragment() to R.string.menu_nosotros
            R.id.nav_contactos -> ContactosFragment() to R.string.menu_contactos
            R.id.nav_login -> LoginFragment() to R.string.menu_login
            else -> InicioFragment() to R.string.menu_inicio
        }
        replaceFragment(fragment)
        supportActionBar?.title = getString(title)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
