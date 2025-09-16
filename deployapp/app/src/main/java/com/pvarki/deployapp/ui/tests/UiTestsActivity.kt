package com.pvarki.deployapp.ui.tests

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.pvarki.deployapp.App
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.repository.EndUserPfxRepository
import com.pvarki.deployapp.data.repository.EnrollmentRepository
import com.pvarki.deployapp.data.repository.HealthcheckRepository
import com.pvarki.deployapp.data.repository.InfoRepository
import com.pvarki.deployapp.data.repository.InstructionsRepository
import com.pvarki.deployapp.utils.PreferenceHelper.callSign
import com.pvarki.deployapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.UUID


class UiTestsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_tests)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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


    private fun test10() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val callSign = "Qqwweerr"
                val fileName = "$callSign.pfx"
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
            val qrBitmap = Utils().generateQRCode("https://busy-leopard.solution.dev.pvarki.fi")
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
}