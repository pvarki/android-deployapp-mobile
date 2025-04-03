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
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Date
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
                val keyPair = generateKeyPair()

                // Step 2: Generate Self-Signed Certificate
                val certificate = generateSelfSignedCertificate(keyPair)
                val newGuid = UUID.randomUUID()
                // Step 3: Save PFX file with password
                val fileName = "cert_$newGuid.pfx"
                val pfxFilePath = getCertDirectory() + "/" + fileName // Path to save the PFX
                val pfxPassword = "mySecurePassword" // Password for the PFX file

                createPfxWithPassword(pfxFilePath, pfxPassword, certificate, keyPair.private)

                //    Log.d(
                //        MainActivity.TAG,
                //        "PFX file created successfully at: $pfxFilePath"
                //    )
                val file = File(getCertDirectory(), fileName)
                println("2. File absolute path: " + file.absolutePath)

                val certFile = File(getCertDirectory(), fileName)
                if (certFile.exists()) {
                    println("File fileName exists at: " + certFile.absolutePath)
                } else {
                    println("File fileName not found!")
                }


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


    fun getCertDirectory(): String {
        val dir = ("$filesDir/certfiles").toString()
        // Create a subdirectory under the files directory
        val targetDir = File(dir)
        if (!targetDir.exists()) {
            val wasCreated =
                targetDir.mkdirs() // Creates the directory and any missing parent directories
            if (wasCreated) {
                println("Directory created at: " + targetDir.absolutePath)
            } else {
                println("Failed to create directory at: " + targetDir.absolutePath)
            }
        } else {
            println("Directory already exists at: " + targetDir.absolutePath)
        }
        return dir
    }

    fun shareFile(context: Context, file: File, authority: String?) {
        println("3. File absolute path: " + file.absolutePath)
        val u = FileProvider.getUriForFile(
            context,
            authority!!, file
        )
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


    @Throws(java.lang.Exception::class)
    private fun generateKeyPair(): KeyPair {
        // Generate RSA KeyPair
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    @Throws(java.lang.Exception::class)
    private fun generateSelfSignedCertificate(keyPair: KeyPair): X509Certificate {
        // Set up the certificate's issuer and subject (both are the same for self-signed certificates)
        val issuer: org.bouncycastle.asn1.x500.X500Name =
            org.bouncycastle.asn1.x500.X500Name("CN=Self Signed")
        val subject: org.bouncycastle.asn1.x500.X500Name = issuer

        // Generate serial number and set validity dates
        val serial = BigInteger(128, SecureRandom())

        // Use DERUTCTime (or ASN1GeneralizedTime) for Date conversion
        val notBeforeDate = Date(System.currentTimeMillis())
        val notAfterDate =
            Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000) // 1 year validity

        val notBefore: org.bouncycastle.asn1.x509.Time =
            org.bouncycastle.asn1.x509.Time(notBeforeDate)
        val notAfter: org.bouncycastle.asn1.x509.Time =
            org.bouncycastle.asn1.x509.Time(notAfterDate)

        val publicKey = keyPair.public
        val privateKey = keyPair.private

        // Convert PublicKey to SubjectPublicKeyInfo
        val publicKeyInfo: org.bouncycastle.asn1.x509.SubjectPublicKeyInfo =
            org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(publicKey.encoded)

        // Build the certificate
        val certificateBuilder: org.bouncycastle.cert.X509v3CertificateBuilder =
            org.bouncycastle.cert.X509v3CertificateBuilder(
                issuer, serial, notBefore, notAfter, subject, publicKeyInfo
            )

        // Sign the certificate with the private key
        val contentSigner: org.bouncycastle.operator.ContentSigner =
            org.bouncycastle.operator.jcajce.JcaContentSignerBuilder("SHA256withRSA")
                .build(privateKey)

        // Convert the certificate to X509Certificate
        val certificate: X509Certificate =
            org.bouncycastle.cert.jcajce.JcaX509CertificateConverter()
                .getCertificate(certificateBuilder.build(contentSigner))

        return certificate
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