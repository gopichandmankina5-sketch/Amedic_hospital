package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amedick.hospitalapp.databinding.ActivityRegisterBinding
import com.amedick.hospitalapp.viewmodel.RegisterViewModel
import com.amedick.hospitalapp.viewmodel.RegistrationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            if (validateInputs()) {
                setLoading(true)
                viewModel.register(
                    name = binding.nameInput.text.toString().trim(),
                    email = binding.emailInput.text.toString().trim(),
                    phone = binding.phoneInput.text.toString().trim(),
                    password = binding.passwordInput.text.toString()
                )
            }
        }

        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RegistrationState.Idle -> setLoading(false)
                        is RegistrationState.Loading -> setLoading(true)
                        is RegistrationState.Success -> {
                            setLoading(false)
                            Toast.makeText(
                                this@RegisterActivity,
                                "Account created! Please verify your email before logging in.",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            })
                            finish()
                        }
                        is RegistrationState.Error -> {
                            setLoading(false)
                            Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()
        val termsAccepted = binding.termsCheckbox.isChecked
        var valid = true

        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Full name is required"
            valid = false
        } else {
            binding.nameInputLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Enter a valid email address"
            valid = false
        } else {
            binding.emailInputLayout.error = null
        }

        if (phone.isEmpty()) {
            binding.phoneInputLayout.error = "Phone number is required"
            valid = false
        } else {
            binding.phoneInputLayout.error = null
        }

        if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            valid = false
        } else {
            binding.passwordInputLayout.error = null
        }

        if (confirmPassword != password) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            valid = false
        } else {
            binding.confirmPasswordInputLayout.error = null
        }

        if (!termsAccepted) {
            Toast.makeText(this, "Please accept the Terms and Conditions to continue.", Toast.LENGTH_SHORT).show()
            valid = false
        }

        return valid
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !loading
        binding.registerButton.alpha = if (loading) 0.7f else 1f
    }
}
