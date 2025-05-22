package com.pvarki.deployapp

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pvarki.deployapp.utils.Utils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.Security
import java.util.UUID
import javax.security.auth.x500.X500Principal


@RunWith(AndroidJUnit4::class)
class CertificateTest {

    lateinit var appContext: Context
    val callSign="TestCallSign"

    @Before
    fun init() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertNotNull(appContext)
        // Add BouncyCastle provider
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun createCertificate() {
        val utils = Utils()
        val keyPair = utils.generateKeyPair()

        // Step 2: Generate Self-Signed Certificate
        val certificate = utils.generateSelfSignedCertificate(keyPair, callSign)
        val newGuid = UUID.randomUUID()
        // Step 3: Save PFX file with password
        val fileName = "$callSign.pfx"
        val pfxFilePath = utils.getCertDirectory(appContext) + "/" + fileName // Path to save the PFX
        val pfxPassword = callSign// Password for the PFX file

        utils.createPfxWithPassword(pfxFilePath, pfxPassword, certificate, keyPair.private)

        //    Log.d(
        //        MainActivity.TAG,
        //        "PFX file created successfully at: $pfxFilePath"
        //    )
        val file = File(utils.getCertDirectory(appContext), fileName)
        Assert.assertNotNull(file)


        val (privateKey, cert) = utils.loadKeyFromPfx(pfxFilePath, pfxPassword)

        val publicKey = utils.getPublicKeyFromPfx(pfxFilePath, pfxPassword)

        val subject = X500Principal("CN=$callSign, O=MyOrg, C=FI")
        val csrPem =  utils.generateCSR(privateKey, publicKey, subject)
        Assert.assertNotNull(csrPem)


    }
}