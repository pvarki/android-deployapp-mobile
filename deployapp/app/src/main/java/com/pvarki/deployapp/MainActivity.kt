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
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        textViewInfo = findViewById(R.id.textViewInfo)
        val buttonCreateCertificate = findViewById<Button>(R.id.buttonCreateCertificate)
        buttonCreateCertificate.setOnClickListener {
            try {
                // Step 1: Generate KeyPair
                val utils = Utils()
                val keyPair = utils.generateKeyPair()

                // Step 2: Generate Self-Signed Certificate
                val certificate = utils.generateSelfSignedCertificate(keyPair, "TODO CN")
                val newGuid = UUID.randomUUID()
                // Step 3: Save PFX file with password
                val fileName = "cert_$newGuid.pfx"
                val pfxFilePath = utils.getCertDirectory(this) + "/" + fileName // Path to save the PFX
                val pfxPassword = "mySecurePassword" // Password for the PFX file

                utils.createPfxWithPassword(pfxFilePath, pfxPassword, certificate, keyPair.private)

                //    Log.d(
                //        MainActivity.TAG,
                //        "PFX file created successfully at: $pfxFilePath"
                //    )
                val file = File(utils.getCertDirectory(this), fileName)
                println("2. File absolute path: " + file.absolutePath)

                shareFile(
                    this@MainActivity,
                    file,
                    "com.pvarki.deployapp.fileprovider"
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
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


    fun shareFile(context: Context, file: File, authority: String) {
        println("3. File absolute path: " + file.absolutePath)

        // Create a Uri for the file using FileProvider
        val fileUri = FileProvider.getUriForFile(
            context,
            authority,  // Replace with your FileProvider authority
            file
        )

        // Create an Intent to share the file
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("application/x-pkcs12") // Adjust MIME type as needed
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "DeployApp")
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Cert file, pls install locally")
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Start the activity to share the file
        context.startActivity(shareIntent)
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