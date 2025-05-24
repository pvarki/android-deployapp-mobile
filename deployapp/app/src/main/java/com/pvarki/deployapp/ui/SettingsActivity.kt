package com.pvarki.deployapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pvarki.deployapp.App
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.api.ApiClient
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        // Show the up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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