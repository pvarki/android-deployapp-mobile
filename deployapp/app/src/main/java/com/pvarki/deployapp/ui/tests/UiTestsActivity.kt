package com.pvarki.deployapp.ui.tests

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.repository.InfoRepository
import com.pvarki.deployapp.data.repository.InstructionsRepository
import com.pvarki.deployapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UiTestsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_tests)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

    private fun test5() = CoroutineScope(Dispatchers.IO).launch {
        val qrBitmap = Utils().generateQRCode("https://casual-halibut.solution.dev.pvarki.fi")
        withContext(Dispatchers.Main) {
            showImageDialog(this@UiTestsActivity, qrBitmap)
        }
    }

    private fun test4() = CoroutineScope(Dispatchers.IO).launch {
        val result = InstructionsRepository().userInstructionFragment()
        showToast("test1 result: ${result.files.keys}")
    }


    private fun test3() {
        runOnUiThread {
            Toast.makeText(this, "test3", Toast.LENGTH_SHORT).show()
        }
    }

    private fun test2() = CoroutineScope(Dispatchers.IO).launch {
        val result = InfoRepository().returnValiduserPayload()
        withContext(Dispatchers.Main) {
            Toast.makeText(this@UiTestsActivity, "test2 result: $result", Toast.LENGTH_SHORT).show()
        }
    }

    private fun test1() = CoroutineScope(Dispatchers.IO).launch {
        val result = InfoRepository().exchangeToken()
        showToast("test1 result: $result")
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