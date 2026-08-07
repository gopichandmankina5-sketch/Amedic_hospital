package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amedick.hospitalapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            if (validateInputs()) {
                Toast.makeText(this, "Signup flow not implemented yet", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        var valid = true
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Name is required"
            valid = false
        } else {
            binding.nameInputLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email"
            valid = false
        } else {
            binding.emailInputLayout.error = null
        }

        if (password.length < 6) {
            binding.passwordInputLayout.error = "Password should be at least 6 characters"
            valid = false
        } else {
            binding.passwordInputLayout.error = null
        }

        return valid
    }
}
