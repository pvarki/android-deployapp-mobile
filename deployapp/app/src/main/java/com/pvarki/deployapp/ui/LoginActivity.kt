package com.pvarki.deployapp.ui


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pvarki.deployapp.R
import com.pvarki.deployapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        // Initialize AuthRepository
        authRepository = AuthRepository(applicationContext)

        // Set login button click listener
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                performLogin(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
          /*  val result = authRepository.login(username, password)
            result.onSuccess {
                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                // Navigate to the next screen or perform other actions
            }.onFailure { error ->
                Toast.makeText(this@LoginActivity, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }*/
        }
    }
}