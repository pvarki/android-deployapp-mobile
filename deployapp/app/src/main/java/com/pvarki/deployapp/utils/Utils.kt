package com.pvarki.deployapp.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Debug
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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.EncodeHintType
import android.util.Base64
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder
import org.bouncycastle.pkcs.PKCS12SafeBag
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder
import org.bouncycastle.pkcs.jcajce.JcePKCS12MacCalculatorBuilder
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEOutputEncryptorBuilder
import java.net.URI
import java.security.cert.CertificateFactory

class Utils {


    fun firstHostLabel(url: String): String {
        val host = URI(url).host ?: throw IllegalArgumentException("Invalid URL: $url")
        return host.substringBefore('.')
    }

    /**
     * Loads a PFX/PKCS12 file and extracts the user certificate.
     *
     * @param folderPath The directory containing the PFX file.
     * @param fileName The name of the PFX file (e.g., "user.pfx").
     * @param password The password required to unlock the PFX archive.
     * @return The parsed X509Certificate object.
     */
    fun loadPfxCertificate(folderPath: String, fileName: String, password: CharArray): X509Certificate {
        val pfxFile = File(folderPath, fileName)

        if (!pfxFile.exists()) {
            throw IllegalArgumentException("PFX file not found at: ${pfxFile.absolutePath}")
        }

        return FileInputStream(pfxFile).use { inputStream ->
            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(inputStream, password)

            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                if (keyStore.isKeyEntry(alias)) {
                    val certificate = keyStore.getCertificate(alias) as? X509Certificate
                    if (certificate != null) return@use certificate
                }
            }
            throw IllegalStateException("No user certificate found in PFX file.")
        }
    }

    /**
     * Reads a CA certificate from a specific folder and returns it as an X509Certificate.
     *
     * @param folderPath The directory where the certificate is stored.
     * @param fileName The name of the certificate file (e.g., "isrg-root-x1-cross-signed.pem").
     * @return The parsed X509Certificate object.
     */
    fun loadCACertificate(folderPath: String, fileName: String): X509Certificate {
        val certFile = File(folderPath, fileName)

        // 1. Ensure the file actually exists
        if (!certFile.exists()) {
            throw IllegalArgumentException("Certificate file not found at: ${certFile.absolutePath}")
        }

        // 2. Open a file input stream
        return FileInputStream(certFile).use { inputStream ->
            // 3. Initialize the CertificateFactory for X.509
            val certificateFactory = CertificateFactory.getInstance("X.509")

            // 4. Generate and cast the certificate
            certificateFactory.generateCertificate(inputStream) as X509Certificate
        }
    }


    fun saveBase64File(base64Data: String, filename: String, directory: File): File? {
        return try {
            // Remove the data URL prefix if present
            val cleanBase64 = base64Data.substringAfter("base64,")

            // Decode Base64 to byte array
            val fileBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

            // Create target file
            val file = File(directory, filename)

            // Write bytes to file
            FileOutputStream(file).use { it.write(fileBytes) }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun createDecoratedQRImage(qrBitmap: Bitmap): Bitmap {
        val width = qrBitmap.width + 100
        val height = qrBitmap.height + 100
        val composedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composedBitmap)
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw QR code centered
        val left = (width - qrBitmap.width) / 2f
        val top = (height - qrBitmap.height) / 2f
        canvas.drawBitmap(qrBitmap, left, top, null)

        return composedBitmap
    }



    fun generateQRCode(text: String, size: Int = 512): Bitmap {
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }


    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected()
    }


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
            val keyStore = KeyStore.getInstance("PKCS12")
            FileInputStream(pfxPath).use { fis ->
                keyStore.load(fis, password.toCharArray())
            }

            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                if (keyStore.isKeyEntry(alias)) {
                    val certificate = keyStore.getCertificate(alias)
                    if (certificate != null) return certificate.publicKey
                }
            }
            throw Exception("No certificate with public key found in PFX file.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun loadKeyFromPfx(pfxPath: String, password: String): Pair<PrivateKey, X509Certificate> {
        val keyStore = KeyStore.getInstance("PKCS12")
        FileInputStream(pfxPath).use { fis ->
            keyStore.load(fis, password.toCharArray())
        }

        val aliases = keyStore.aliases()
        while (aliases.hasMoreElements()) {
            val alias = aliases.nextElement()
            if (keyStore.isKeyEntry(alias)) {
                val key = keyStore.getKey(alias, password.toCharArray()) as? PrivateKey
                val cert = keyStore.getCertificate(alias) as? X509Certificate
                if (key != null && cert != null) {
                    return Pair(key, cert)
                }
            }
        }
        throw Exception("No private key and certificate pair found in PFX file.")
    }

    @Throws(Exception::class)
    fun createLegacyPfx(
        pfxFilePath: String,
        password: String,
        privateKey: PrivateKey,
        certificateChain: Array<X509Certificate>,
        friendlyName: String
    ) {
        val bcProvider = BouncyCastleProvider()
        val passwordChars = password.toCharArray()
        val pfxPduBuilder = PKCS12PfxPduBuilder()

        // 1. Shrouded Keybag for Private Key: pbeWithSHA1And3-KeyTripleDES-CBC, Iteration 50000
        val keyEncryptor = JcePKCSPBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC)
            .setProvider(bcProvider)
            .setIterationCount(50000)
            .build(passwordChars)

        val leafCert = certificateChain[0]
        val skid = JcaX509ExtensionUtils().createSubjectKeyIdentifier(leafCert.publicKey)

        val keyBagBuilder = JcaPKCS12SafeBagBuilder(privateKey, keyEncryptor)
        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, DERBMPString(friendlyName))
        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, DEROctetString(skid.keyIdentifier))
        pfxPduBuilder.addData(keyBagBuilder.build())

        // 2. Certificate Bags as unencrypted data
        certificateChain.forEachIndexed { index, cert ->
            val bagBuilder = JcaPKCS12SafeBagBuilder(cert)
            val currentFriendlyName = if (index == 0) friendlyName else "$friendlyName-ca-$index"
            bagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, DERBMPString(currentFriendlyName))

            if (index == 0) { // Leaf certificate attributes
                bagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, DEROctetString(skid.keyIdentifier))
            }
            pfxPduBuilder.addData(bagBuilder.build())
        }

        // 3. MAC: sha1, Iteration 2048
        val macCalculatorBuilder = JcePKCS12MacCalculatorBuilder(OIWObjectIdentifiers.idSHA1)
            .setProvider(bcProvider)
            .setIterationCount(2048)

        val pfxPdu = pfxPduBuilder.build(macCalculatorBuilder, passwordChars)

        FileOutputStream(pfxFilePath).use { it.write(pfxPdu.encoded) }
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