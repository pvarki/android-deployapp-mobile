package com.pvarki.deployapp.ui


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.pvarki.deployapp.App.Companion.AppPrefs
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.model.EnrollRequest
import com.pvarki.deployapp.data.repository.EnrollmentRepository
import com.pvarki.deployapp.utils.PreferenceHelper.approveCode
import com.pvarki.deployapp.utils.PreferenceHelper.callSign
import com.pvarki.deployapp.utils.PreferenceHelper.jwt
import com.pvarki.deployapp.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.security.auth.x500.X500Principal

class CreateCSRActivity : AppCompatActivity() {

    private lateinit var editTextCallSign: EditText
    private lateinit var editTextInviteCode: EditText
    private lateinit var buttonCreateCsr: Button
    private lateinit var textViewApproveCode: TextView
    private lateinit var textViewErrorText: TextView
    private lateinit var imageButtonCopyApproveCode: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_csr)

       // setSupportActionBar(findViewById(R.id.toolbar))
      //  supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            findViewById<android.view.View>(R.id.app_bar_layout).setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Initialize views
        editTextCallSign = findViewById(R.id.edit_text_call_sign)
        editTextCallSign.setText(AppPrefs.callSign)

        editTextInviteCode = findViewById(R.id.edit_text_invite_code)

        buttonCreateCsr = findViewById(R.id.button_create_csr)
        textViewApproveCode = findViewById(R.id.text_view_approve_code)
        textViewApproveCode.text = AppPrefs.approveCode

        textViewErrorText = findViewById(R.id.text_view_error_text)

        imageButtonCopyApproveCode = findViewById(R.id.image_button_copy)
        imageButtonCopyApproveCode.setOnClickListener {
            val approveCode = textViewApproveCode.text.toString()
            if (approveCode.isNotEmpty()) {
                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Approve Code", approveCode)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    this@CreateCSRActivity, "Approve code copied to clipboard: $approveCode",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Timber.w("No approve code to copy")
            }
        }
        // Set login button click listener
        buttonCreateCsr.setOnClickListener {
            hideKeyboard()

            editTextInviteCode.error = null // Clear any previous error
            val inviteCode = editTextInviteCode.text.toString().trim()
            if (inviteCode.isEmpty()) {
                editTextInviteCode.error = "Please enter your invite code"
                return@setOnClickListener
            }

            editTextCallSign.error = null // Clear any previous error
            val callSign = editTextCallSign.text.toString().trim()
            if (callSign.isEmpty()) {
                editTextCallSign.error = "Please enter your callsign"
                return@setOnClickListener
            }

            AppPrefs.callSign = callSign
            createCSR(callSign, inviteCode)
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun setErrorText(error: String) {
        textViewErrorText.text = error
        textViewErrorText.visibility = if (error.isEmpty()) View.GONE else View.VISIBLE
        Timber.e(error)
    }

    fun onSettingsMenuClick(item: android.view.MenuItem) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    private fun createCSR(callSign: String, inviteCode: String) {
        lifecycleScope.launch {
            val progressDialog = Utils().showProgressDialog(this@CreateCSRActivity)
            try {
                val context = this@CreateCSRActivity
                setErrorText("")

                withContext(Dispatchers.IO) {
                    val repository = EnrollmentRepository()
                    val utils = Utils()
                    val keyPair = utils.generateKeyPair()
                    val fileName = "cert_$callSign.pfx"
                    val pfxFilePath =
                        utils.getCertDirectory(context) + "/" + fileName // Path to save the PFX
                    val pfxPassword = callSign // Password for the PFX file

                    val certificate = utils.generateSelfSignedCertificate(keyPair, callSign)

                    utils.createPfxWithPassword(
                        pfxFilePath,
                        pfxPassword,
                        certificate,
                        keyPair.private
                    )

                    val (privateKey, cert) = utils.loadKeyFromPfx(pfxFilePath, pfxPassword)
                    val publicKey = utils.getPublicKeyFromPfx(pfxFilePath, pfxPassword)
                    val subject = X500Principal("CN=$callSign")
                    val csrPem = utils.generateCSR(privateKey, publicKey, subject)

                    val er = EnrollRequest(inviteCode, callSign, csrPem)
                    val result = repository.postEnEnrollResponse(er)
                    textViewApproveCode.text = result.approvecode
                    AppPrefs.approveCode = result.approvecode
                    AppPrefs.jwt = result.jwt
                    Timber.d("JWT: ${AppPrefs.jwt}")
                    Timber.d("Approve code: ${AppPrefs.approveCode}")
                }
            } catch (e: Exception) {
                setErrorText("Failed to create CSR. Please try again. ${e.message}")
            } finally {
                progressDialog.dismiss()
            }
        }
    }
}