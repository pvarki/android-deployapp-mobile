package com.pvarki.deployapp.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.pvarki.deployapp.R
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringWriter
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

class Utils {


    fun showProgressDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        builder.setView(view)
        builder.setCancelable(false) // Optional: make it not cancelable
        val dialog = builder.create()
        dialog.show()
        return dialog
    }


    fun generateCSR(privateKey: PrivateKey, publicKey: PublicKey, subject: X500Principal): String {
        val signer = JcaContentSignerBuilder("SHA256withRSA").build(privateKey)
        val csrBuilder = JcaPKCS10CertificationRequestBuilder(subject, publicKey)
        val csr = csrBuilder.build(signer)

        val stringWriter = StringWriter()
        JcaPEMWriter(stringWriter).use {
            it.writeObject(csr)
        }

        return stringWriter.toString()
    }


    fun getPublicKeyFromPfx(pfxPath: String, password: String): PublicKey {
        try {
            // Add Bouncy Castle provider if not already added
            if (Security.getProvider("BC") == null) {
                Security.addProvider(BouncyCastleProvider())
            }

            val keyStore = KeyStore.getInstance("PKCS12", "BC")
            FileInputStream(pfxPath).use { fis ->
                keyStore.load(fis, password.toCharArray())
            }

            val alias = keyStore.aliases().toList().firstOrNull()
                ?: throw Exception("No aliases found in keystore")

            val certificate = keyStore.getCertificate(alias)
                ?: throw Exception("Certificate not found for alias: $alias")

            return certificate.publicKey
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun loadKeyFromPfx(pfxPath: String, password: String): Pair<PrivateKey, X509Certificate> {
        val keyStore = KeyStore.getInstance("PKCS12", "BC")
        keyStore.load(FileInputStream(pfxPath), password.toCharArray())

        val alias = keyStore.aliases().toList().first()
        val key = keyStore.getKey(alias, password.toCharArray()) as PrivateKey
        val cert = keyStore.getCertificate(alias) as X509Certificate

        return Pair(key, cert)
    }


    @Throws(Exception::class)
    fun createPfxWithPassword(
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
    }


    fun getCertDirectory(context: Context): String {
        val fileDir = context.filesDir
        val dir = ("$fileDir/certfiles").toString()
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

    @Throws(java.lang.Exception::class)
    fun generateSelfSignedCertificate(keyPair: KeyPair, cn: String): X509Certificate {
        // Set up the certificate's issuer and subject (both are the same for self-signed certificates)
        val issuer: org.bouncycastle.asn1.x500.X500Name =
            org.bouncycastle.asn1.x500.X500Name("CN=$cn")
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

    @Throws(java.lang.Exception::class)
    fun generateKeyPair(): KeyPair {
        // Generate RSA KeyPair
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

}