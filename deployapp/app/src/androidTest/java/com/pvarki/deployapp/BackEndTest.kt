package com.pvarki.deployapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pvarki.deployapp.data.model.EnrollRequest
import com.pvarki.deployapp.data.repository.EnrollmentRepository
import com.pvarki.deployapp.data.repository.HealthcheckRepository
import com.pvarki.deployapp.data.repository.TokenRepository
import com.pvarki.deployapp.data.repository.UtilsRepository
import com.pvarki.deployapp.utils.PreferenceHelper.restApiBaseUrl
import com.pvarki.deployapp.utils.Utils
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import javax.security.auth.x500.X500Principal


@RunWith(AndroidJUnit4::class)
class BackEndTest {
    @Before
    fun init() {
        val appContext = InstrumentationRegistry.getInstrumentation().context
        Assert.assertNotNull(appContext)
        App.AppPrefs.restApiBaseUrl = "https://casual-halibut.solution.dev.pvarki.fi"
    }

    @Test
    fun testHealthCheck() = runBlocking {
        val repository = HealthcheckRepository()
        val result = repository.requestHealthCheck()
        Assert.assertNotNull(result)
        Assert.assertTrue(result.healthcheck.isNotEmpty())
        Assert.assertEquals("success", result.healthcheck)
    }

    @Test
    fun testHealthCheckServices() = runBlocking {
        val repository = HealthcheckRepository()
        val result = repository.requestHealthCheckServices()
        Assert.assertNotNull(result)
    }

    @Test
    fun testUtilsRepository() = runBlocking {
        val repository = UtilsRepository()
        val result = repository.getJwtPubkey()
        Assert.assertNotNull(result)
        Assert.assertTrue(result.isNotEmpty())
    }


    @Test
    fun testEnrollment() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val repository = EnrollmentRepository()
        val inviteCode = "VPOSOQ9J"
        val callSign = "EIOOTOTTA2"

        // Step 1: Generate KeyPair
        val utils = Utils()
        val keyPair = utils.generateKeyPair()

        // Step 2: Generate Self-Signed Certificate
        val certificate = utils.generateSelfSignedCertificate(keyPair, callSign)
        val newGuid = UUID.randomUUID()

        // Step 3: Save PFX file with password
        val fileName = "cert_$callSign.pfx"
        val pfxFilePath =
            utils.getCertDirectory(appContext) + "/" + fileName // Path to save the PFX
        val pfxPassword = callSign // Password for the PFX file

        val fileContent =
            utils.createPfxWithPassword(pfxFilePath, pfxPassword, certificate, keyPair.private)

        val (privateKey, cert) = utils.loadKeyFromPfx(pfxFilePath, pfxPassword)

        val publicKey = utils.getPublicKeyFromPfx(pfxFilePath, pfxPassword)

        val subject = X500Principal("CN=$callSign, O=MyOrg, C=FI")
        val csrPem = utils.generateCSR(privateKey, publicKey, subject)
        Assert.assertNotNull(csrPem)

        val er = EnrollRequest(inviteCode, callSign, csrPem)

        val result = repository.postEnEnrollResponse(er)
        Assert.assertNotNull(result)
        Assert.assertTrue(result.jwt.isNotEmpty())
    }


    @Test
    fun testToken() = runBlocking {
        val repository = TokenRepository()
        val result = repository.refreshToken()
        Assert.assertNotNull(result)
        Assert.assertTrue(result.isNotEmpty())
    }


    // https://casual-halibut.solution.dev.pvarki.fi

}