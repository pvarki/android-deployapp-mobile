package com.pvarki.deployapp.ui.tests

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.repository.InfoRepository
import com.pvarki.deployapp.data.repository.InstructionsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UiTestsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_tests)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Button>(R.id.buttonTest4).setOnClickListener { test4() }
        findViewById<Button>(R.id.buttonTest3).setOnClickListener { test3() }
        findViewById<Button>(R.id.buttonTest2).setOnClickListener { test2() }
        findViewById<Button>(R.id.buttonTest1).setOnClickListener { test1() }
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
}