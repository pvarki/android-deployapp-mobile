package com.pvarki.deployapp.ui.tests

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.security.KeyChain
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.pvarki.deployapp.App
import com.pvarki.deployapp.App.Companion.AppPrefs
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.repository.EndUserPfxRepository
import com.pvarki.deployapp.data.repository.EnrollmentRepository
import com.pvarki.deployapp.data.repository.HealthcheckRepository
import com.pvarki.deployapp.data.repository.InfoRepository
import com.pvarki.deployapp.data.repository.InstructionsRepository
import com.pvarki.deployapp.data.repository.UtilsRepository
import com.pvarki.deployapp.utils.PreferenceHelper.callSign
import com.pvarki.deployapp.utils.PreferenceHelper.jwt
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl
import com.pvarki.deployapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.UUID


class UiTestsActivity : AppCompatActivity() {

    private val callSign: String
        get() = App.AppPrefs.callSign.ifBlank { "Kolmospakki" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_tests)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Timber.d("UiTestsActivity onCreate. callSign: $callSign, baseUrl: ${App.AppPrefs.restApiBaseUrl}")

        findViewById<Button>(R.id.buttonTest23).setOnClickListener { test23() }
        findViewById<Button>(R.id.buttonTest22).setOnClickListener { test22() }
        findViewById<Button>(R.id.buttonTest21).setOnClickListener { test21() }
        findViewById<Button>(R.id.buttonTest20).setOnClickListener { test20() }
        findViewById<Button>(R.id.buttonTest19).setOnClickListener { test19() }
        findViewById<Button>(R.id.buttonTest18).setOnClickListener { test18() }
        findViewById<Button>(R.id.buttonTest17).setOnClickListener { test17() }
        findViewById<Button>(R.id.buttonTest16).setOnClickListener { test16() }
        findViewById<Button>(R.id.buttonTest15).setOnClickListener { test15() }
        findViewById<Button>(R.id.buttonTest14).setOnClickListener { test14() }
        findViewById<Button>(R.id.buttonTest13).setOnClickListener { test13() }
        findViewById<Button>(R.id.buttonTest12).setOnClickListener { test12() }
        findViewById<Button>(R.id.buttonTest11).setOnClickListener { test11() }
        findViewById<Button>(R.id.buttonTest10).setOnClickListener { test10() }
        findViewById<Button>(R.id.buttonTest9).setOnClickListener { test9() }
        findViewById<Button>(R.id.buttonTest8).setOnClickListener { test8() }
        findViewById<Button>(R.id.buttonTest7).setOnClickListener { test7() }
        findViewById<Button>(R.id.buttonTest6).setOnClickListener { test6() }
        findViewById<Button>(R.id.buttonTest5).setOnClickListener { test5() }
        findViewById<Button>(R.id.buttonTest4).setOnClickListener { test4() }
        findViewById<Button>(R.id.buttonTest3).setOnClickListener { test3() }
        findViewById<Button>(R.id.buttonTest2).setOnClickListener { test2() }
        findViewById<Button>(R.id.buttonTest1).setOnClickListener { test1() }
    }

    // Combine self signed cert and callsing.pem together and install it.
    // After that, it should be possible to call mtls REST APIs
    private fun test23() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val healthCheck =  HealthcheckRepository().requestHealthCheck()
                val serverName = Utils().firstHostLabel(AppPrefs.restApiBaseUrl)
                Timber.d("serverName: $serverName")

               // val validUserPayload = InfoRepository().returnValiduserPayload()

                val callsignPemResponse = EndUserPfxRepository().getUserPem(callSign)

                val outFilename = "${callSign}.pem"
                val certDir = Utils().getCertDirectory(this@UiTestsActivity)
                val outFile = File(certDir, outFilename)
                Timber.d("$outFile created")

                if (callsignPemResponse.isSuccessful) {
                    callsignPemResponse.body()?.let { body ->
                        // Stream the response into the file
                        outFile.outputStream().use { fileOut ->
                            body.byteStream().use { input ->
                                input.copyTo(fileOut)
                            }
                        }
                    }
                } else {
                    throw IOException("Failed to create PEM: ${callsignPemResponse.code()} ${callsignPemResponse.message()}")
                }

                installCertificate(this@UiTestsActivity, outFilename, callSign,   File(certDir))
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }


    // Combine self signed cert and one from pvarki server together and install it.
    // After that, it should be possible to call mtls REST APIs
    private fun test22() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d("test22 " + AppPrefs.jwt)
                val healthCheck =  HealthcheckRepository().requestHealthCheck()
                val serverName = Utils().firstHostLabel(AppPrefs.restApiBaseUrl)
                Timber.d("serverName: $serverName")

                val validUserPayload = InfoRepository().returnValiduserPayload()

                val pfxResponse = EndUserPfxRepository().signedChainPFXFile(callSign, serverName)

                val response = EndUserPfxRepository().signedChainPEMFile(callSign, serverName)

                val outFilename = "${callSign}_${serverName}.pem"
                val certDir = Utils().getCertDirectory(this@UiTestsActivity)
                val outFile = File(certDir, outFilename)
                Timber.d("$outFile created")

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        // Stream the response into the file
                        outFile.outputStream().use { fileOut ->
                            body.byteStream().use { input ->
                                input.copyTo(fileOut)
                            }
                        }
                    }
                } else {
                    throw IOException("Failed to create PEM: ${response.code()} ${response.message()}")
                }

               // installCertificate(this@UiTestsActivity, outFilename, callSign)
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test21() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val utils = Utils()
                val certDir = utils.getCertDirectory(this@UiTestsActivity)


// 1. Load the REAL private key from the local placeholder PFX
                val (privateKey, _) = utils.loadKeyFromPfx(
                    "$certDir/cert_$callSign.pfx",
                    callSign  // this PFX's password, per your test18 pattern
                )

// 2. Load the server-signed leaf + intermediate CA from the downloaded chain
//    (Ykkospakki.pem has both certs concatenated - leaf first, then intermediate)
                val certFactory = CertificateFactory.getInstance("X.509")
                val certs = FileInputStream("$certDir/$callSign.pem").use { stream ->
                    certFactory.generateCertificates(stream)
                }.map { it as X509Certificate }

                val leafCert = certs[0]        // CN=Ykkospakki, real signed
                val intermediateCert = certs[1] // working-ewe-node1 Intermediate CA

// 3. Build the final PFX with the REAL private key + REAL signed chain
                val fileName = "final_${callSign}.pfx"
                val pfxFilePath = "$certDir/$fileName"

                utils.createLegacyPfx(
                    pfxFilePath,
                    callSign,  // or whatever password you want for the final file
                    privateKey,
                    arrayOf(leafCert, intermediateCert),
                    callSign
                )

                showToast("File created to $pfxFilePath")
                installCertificate(this@UiTestsActivity, fileName, callSign, File(certDir))
            } catch (e: Exception) {
                Timber.e(e, "Error in test21")
                showToast("Error: ${e.message}")
            }
        }
    }


    private fun test20() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val utils = Utils()
                val certDir = utils.getCertDirectory(this@UiTestsActivity)

                val serverName = Utils().firstHostLabel(AppPrefs.restApiBaseUrl)
                Timber.d("serverName: $serverName")

              //  val outFilename = "${callSign}_${serverName}.pem"
                val outFilename = "final_${callSign}.pfx"

                val pfxFile = File(certDir, outFilename)
                val pfxPassword = callSign // Password for the PFX file
                
                val result = pfxFile.inputStream().use { inputStream ->
                    HealthcheckRepository().requestHealthCheckMtls(
                        inputStream,
                        pfxPassword,
                        this@UiTestsActivity
                    )
                }
                showToast("Result: ${result}")

                val haveIBeenAccepted = pfxFile.inputStream().use { inputStream ->
                    EnrollmentRepository().requestHaveIBeenAccepted(
                        inputStream,
                        pfxPassword,
                        this@UiTestsActivity
                    )
                }
                showToast("Result: ${haveIBeenAccepted}")

            } catch (e: Exception) {
                Timber.e(e, "Error in test20")
                showToast("Error: ${e.message}")
            }
        }
    }


    private fun test19() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

            } catch (e: Exception) {
                Timber.e(e, "Error in test19")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test18() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

            } catch (e: Exception) {
                Timber.e(e, "Error in test18")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test17() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

            } catch (e: Exception) {
                Timber.e(e, "Error in test17")
                showToast("Error: ${e.message}")
            }
        }
    }


    private fun test16() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val utils = Utils()
                // Step 1: Generate KeyPair for the user
                val keyPair = utils.generateKeyPair()

                // Step 2: Generate a dummy CA and a user certificate signed by it (for demonstration)
                // In reality, you'd get these from your CA server.
                val userCert = utils.generateSelfSignedCertificate(keyPair, callSign)
                // We'll just use the same cert twice or another self-signed one to simulate a chain
                val caCert =
                    utils.generateSelfSignedCertificate(utils.generateKeyPair(), "wanted-lamb")

                // Step 3: Create the PFX with the requested legacy encryption parameters
                val fileName = "legacy_${callSign}_${UUID.randomUUID()}.pfx"
                val pfxFilePath = utils.getCertDirectory(this@UiTestsActivity) + "/" + fileName
                val pfxPassword = callSign

                utils.createLegacyPfx(
                    pfxFilePath,
                    pfxPassword,
                    keyPair.private,
                    arrayOf(userCert, caCert),
                    "IEUser"
                )

                withContext(Dispatchers.Main) {
                    showToast("Legacy PFX created: $fileName")
                    val file = File(pfxFilePath)
                    shareFile(this@UiTestsActivity, file, "com.pvarki.deployapp.fileprovider")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in test16")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test15() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = UtilsRepository().getJwtPubkey()
                showToast("JWT pubkey: ${response}")
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test14() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response = EndUserPfxRepository().getUserPfx(callSign)
                val formatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss_SSS")
                val timestamp = formatter.format(java.util.Date())
                val outFilename = "${callSign}_$timestamp.pfx"

                val outFile = File(this@UiTestsActivity.filesDir, outFilename)
                Timber.d("$outFile created")

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        // Stream the response into the file
                        outFile.outputStream().use { fileOut ->
                            body.byteStream().use { input ->
                                input.copyTo(fileOut)
                            }
                        }
                    }
                } else {
                    throw IOException("Failed to create PEM: ${response.code()} ${response.message()}")
                }

                val uri = FileProvider.getUriForFile(
                    this@UiTestsActivity,
                    "com.pvarki.deployapp.fileprovider",
                    outFile
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/x-pkcs12")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Install certificate"))
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }


    private fun test13() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EndUserPfxRepository().getUserPfx(callSign)
                val formatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss_SSS")
                val timestamp = formatter.format(java.util.Date())
                val outFilename = "${callSign}_$timestamp.pfx"

                val outFile = File(this@UiTestsActivity.filesDir, outFilename)
                Timber.d("$outFile created")

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        // Stream the response into the file
                        outFile.outputStream().use { fileOut ->
                            body.byteStream().use { input ->
                                input.copyTo(fileOut)
                            }
                        }
                    }
                } else {
                    throw IOException("Failed to create PEM: ${response.code()} ${response.message()}")
                }

                installCertificate(this@UiTestsActivity, outFilename, callSign, this@UiTestsActivity.filesDir)
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }


    private fun test12() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EndUserPfxRepository().getUserPem(callSign)

                val outFile = File(this@UiTestsActivity.filesDir, "${callSign}.pem")
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        // Stream the response into the file
                        outFile.outputStream().use { fileOut ->
                            body.byteStream().use { input ->
                                input.copyTo(fileOut)
                            }
                        }
                    }
                } else {
                    throw IOException("Failed to create PEM: ${response.code()} ${response.message()}")
                }

                installPemCertificate(this@UiTestsActivity, "${callSign}.pem", callSign)
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test11() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = EndUserPfxRepository().getUserPfx(callSign)

                val outFile = File(this@UiTestsActivity.filesDir, "user_${callSign}.pfx")
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        // Stream the response into the file
                        outFile.outputStream().use { fileOut ->
                            body.byteStream().use { input ->
                                input.copyTo(fileOut)
                            }
                        }
                    }
                } else {
                    throw IOException("Failed to create PFX: ${response.code()} ${response.message()}")
                }


            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test10() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileName = "cert_$callSign.pfx"
                val pfxFilePath = Utils().getCertDirectory(this@UiTestsActivity) + "/" + fileName
                val inputStream: InputStream = File(pfxFilePath).inputStream()
                val pfxPassword = callSign // Password for the PFX file
                val result = HealthcheckRepository().requestHealthCheckMtls(
                    inputStream,
                    pfxPassword,
                    this@UiTestsActivity
                )
                showToast("Result: ${result}")
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }


    private fun test9() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = InfoRepository().returnMtlsPayload()
                showToast("Result: ${result}")
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test8() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Get the full response
                val response = EndUserPfxRepository().getUserPfx(App.AppPrefs.callSign)
                if (response.isSuccessful && response.body() != null) {
                    // 2. Extract filename from Content-Disposition header
                    val contentDisposition = response.headers()["Content-Disposition"]
                    val fileName = contentDisposition
                        ?.substringAfter("filename=")
                        ?.replace("\"", "")
                        ?: "certificate.pfx"

                    // 3. Save file to disk
                    val fileDir = Utils().getCertDirectory(this@UiTestsActivity)
                    val file = File(fileDir, fileName)
                    response.body()!!.byteStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    showToast("Result saved to: ${file.absolutePath}")

                    // 4. Prompt user to install
                    val uri = FileProvider.getUriForFile(
                        this@UiTestsActivity,
                        "com.pvarki.deployapp.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/x-pkcs12")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Install certificate"))
                } else {
                    showToast("Failed to download certificate")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }


    private fun test7() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = EnrollmentRepository().requestEnrollmentStatus(App.AppPrefs.callSign)
                showToast("test1 result: $result")
            } catch (e: Exception) {
                Timber.e(e, "Error")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun test6() {
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
                this@UiTestsActivity,
                file,
                "com.pvarki.deployapp.fileprovider"
            )
        } catch (e: Exception) {
            Timber.e(e, "Error")
            Toast.makeText(
                this@UiTestsActivity,
                "Error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
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


    private fun test5() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val qrBitmap = Utils().generateQRCode(App.AppPrefs.restApiBaseUrl)
            withContext(Dispatchers.Main) {
                showImageDialog(this@UiTestsActivity, qrBitmap)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error")
            showToast("Error: ${e.message}")
        }
    }

    private fun test4() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = InstructionsRepository().userInstructionFragment()
            showToast("test1 result: ${result.files.keys}")

            // Get external files directory (private to your app)
            val fileDir = this@UiTestsActivity.getExternalFilesDir(null)

            // Assume you got these from API
            if (fileDir != null) {
                Timber.d("File directory: ${fileDir.absolutePath}")
            } else {
                Timber.e("Failed to get external files directory")
                return@launch
            }

            result.files.forEach { (key, value) ->

                Timber.d("Key: $key, Value: $value")
                value.forEach { fileItem ->

                    Timber.d("File Item Title: ${fileItem.title}, Filename: ${fileItem.filename}")
                    val base64String = fileItem.data
                    val filename = fileItem.filename

                    val savedFile = Utils().saveBase64File(base64String, filename, fileDir)

                    if (savedFile != null) {
                        Timber.d("File saved to: ${savedFile.absolutePath}")
                    } else {
                        Timber.e("Failed to save file")
                    }
                    runOnUiThread {
                        Toast.makeText(
                            this@UiTestsActivity,
                            "Filename: ${fileItem.filename} saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error")
            showToast("Error: ${e.message}")
        }
    }

    private fun test3() {
        runOnUiThread {
            Toast.makeText(this, "test3", Toast.LENGTH_SHORT).show()
        }
    }

    private fun test2() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = InfoRepository().exchangeToken()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UiTestsActivity, "test2 result: $result", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in test2")
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@UiTestsActivity,
                    "test2 error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun test1() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = InfoRepository().exchangeToken()
            showToast("test1 result: $result")
        } catch (e: Exception) {
            Timber.e(e, "Error in test1")
            showToast("test1 error: ${e.message}")
        }
    }

    private suspend fun showToast(message: String) = withContext(Dispatchers.Main) {
        Toast.makeText(this@UiTestsActivity, message, Toast.LENGTH_SHORT).show()
    }


    fun showQRCodeDialog(context: Context, qrBitmap: Bitmap) {
        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(500, 500)
            setImageBitmap(qrBitmap)
        }

        AlertDialog.Builder(context)
            .setTitle("Scan QR Code")
            .setView(imageView)
            .setPositiveButton("Close", null)
            .show()
    }


    fun showImageDialog(context: Context, imageBitmap: Bitmap) {
        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(600, 600)
            setImageBitmap(imageBitmap)
        }

        AlertDialog.Builder(context)
            .setTitle("Here's your QR code")
            .setView(imageView)
            .setPositiveButton("Close", null)
            .show()
    }


    fun installCertificate(context: Context, fileName: String, callSign: String,directory:File) {
        val certFile = File(directory, fileName)

        if (certFile.exists()) {
            val certBytes = certFile.readBytes()

            val installIntent = KeyChain.createInstallIntent().apply {
                putExtra(KeyChain.EXTRA_PKCS12, certBytes)
                putExtra(KeyChain.EXTRA_NAME, callSign) // optional alias name
            }

            context.startActivity(installIntent)
        } else {
            throw IllegalArgumentException("Certificate file not found: ${certFile.absolutePath}")
        }
    }


    fun installPemCertificate(context: Context, fileName: String, callSign: String) {
        val certFile = File(context.filesDir, fileName)

        if (certFile.exists()) {
            val certBytes = certFile.readBytes()

            val installIntent = KeyChain.createInstallIntent().apply {
                putExtra(KeyChain.EXTRA_CERTIFICATE, certBytes)
                putExtra(KeyChain.EXTRA_NAME, callSign) // optional alias name
            }

            context.startActivity(installIntent)
        } else {
            throw IllegalArgumentException("Certificate file not found: ${certFile.absolutePath}")
        }
    }


}