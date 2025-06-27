package com.pvarki.deployapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.pvarki.deployapp.databinding.ActivityMainBinding
import com.pvarki.deployapp.ui.CreateCSRActivity
import com.pvarki.deployapp.ui.SettingsActivity
import com.pvarki.deployapp.utils.Utils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var textViewInfo: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home/*, R.id.nav_gallery, R.id.nav_slideshow*/
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        textViewInfo = findViewById(R.id.textViewInfo)
        val buttonCreateCertificate = findViewById<Button>(R.id.buttonCreateCertificate)
        buttonCreateCertificate.setOnClickListener {

        }

        // Add BouncyCastle provider
        Security.addProvider(BouncyCastleProvider())

        val buttonLoginTest = findViewById<Button>(R.id.buttonLoginTest)
        buttonLoginTest.setOnClickListener {
            startActivity(Intent(this, CreateCSRActivity::class.java))
        }
    }

    fun onSettingsMenuClick(item: android.view.MenuItem) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }







    @Throws(Exception::class)
    private fun createPfxWithPassword(
        pfxFilePath: String,
        pfxPassword: String,
        certificate: X509Certificate,
        privateKey: PrivateKey
    ) {
        // Create an empty KeyStore of type PKCS12 (PFX)
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null) // Initialize the keystore

        // Set the alias and store the certificate and private key in the keystore
        val alias = "my_certificate"
        keyStore.setCertificateEntry(alias, certificate)
        keyStore.setKeyEntry(
            alias,
            privateKey,
            pfxPassword.toCharArray(),
            arrayOf<Certificate>(certificate)
        )

        FileOutputStream(pfxFilePath).use { fos ->
            keyStore.store(fos, pfxPassword.toCharArray()) // Store the keystore with password
        }
        println("1. fFile absolute path pfxFilePath: $pfxFilePath")


        runOnUiThread { textViewInfo?.setText("Created pfxFilePath:$pfxFilePath") }
    }


}