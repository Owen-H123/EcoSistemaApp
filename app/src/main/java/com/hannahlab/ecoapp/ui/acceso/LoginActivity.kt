package com.hannahlab.ecoapp.ui.acceso

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hannahlab.ecoapp.R

/** Primera pantalla sin sesión: aloja el formulario de [com.hannahlab.ecoapp.ui.main.fragments.LoginFragment]. */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}
