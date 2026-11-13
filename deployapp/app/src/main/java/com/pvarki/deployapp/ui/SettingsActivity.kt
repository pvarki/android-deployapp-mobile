package com.pvarki.deployapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pvarki.deployapp.App
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            findViewById<android.view.View>(R.id.app_bar_layout).setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val editServerAddress = findViewById<EditText>(R.id.edit_text_server_address)
        val btnSave = findViewById<Button>(R.id.button_save_settings)

        // Load saved or default value
        val savedAddress = App.AppPrefs.restApiBaseUrl
        editServerAddress.setText(savedAddress)

        // Save on button click
        btnSave.setOnClickListener {
            val newAddress = editServerAddress.text.toString()
            App.AppPrefs.restApiBaseUrl = newAddress
            ApiClient.reset()
            Toast.makeText(
                this,
                "Server address saved: $newAddress",
                Toast.LENGTH_SHORT
            ).show()
            finish() // Close activity
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // or simply finish()
        return true
    }
}