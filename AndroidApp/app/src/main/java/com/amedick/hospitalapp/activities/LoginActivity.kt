package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amedick.hospitalapp.databinding.ActivityLoginBinding
import com.amedick.hospitalapp.viewmodel.ForgotPasswordState
import com.amedick.hospitalapp.viewmodel.LoginState
import com.amedick.hospitalapp.viewmodel.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            if (validateInputs()) {
                setLoading(true)
                viewModel.login(
                    binding.emailInput.text.toString().trim(),
                    binding.passwordInput.text.toString()
                )
            }
        }

        binding.registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        observeViewModel()
    }

    private fun validateInputs(): Boolean {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        var valid = true

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Enter a valid email address"
            valid = false
        } else {
            binding.emailInputLayout.error = null
        }

        if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            valid = false
        } else {
            binding.passwordInputLayout.error = null
        }

        return valid
    }

    private fun showForgotPasswordDialog() {
        val emailLayout = TextInputLayout(this).apply {
            hint = "Enter your email address"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setPadding(48, 16, 48, 8)
        }
        val emailField = TextInputEditText(emailLayout.context).apply {
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or android.text.InputType.TYPE_CLASS_TEXT
        }
        emailLayout.addView(emailField)

        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setMessage("We'll send a password reset link to your email address.")
            .setView(emailLayout)
            .setPositiveButton("Send Reset Link") { _, _ ->
                val email = emailField.text.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    viewModel.sendForgotPassword(email)
                } else {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is LoginState.Idle -> setLoading(false)
                            is LoginState.Loading -> setLoading(true)
                            is LoginState.Success -> {
                                setLoading(false)
                                val role = state.user?.role
                                val intent = when (role) {
                                    "admin" -> Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                                    "doctor" -> Intent(this@LoginActivity, DoctorDashboardActivity::class.java)
                                    else -> Intent(this@LoginActivity, MainActivity::class.java)
                                }
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            is LoginState.Error -> {
                                setLoading(false)
                                binding.passwordInputLayout.error = state.message
                                viewModel.resetState()
                            }
                        }
                    }
                }

                launch {
                    viewModel.forgotPasswordState.collect { state ->
                        when (state) {
                            is ForgotPasswordState.Idle -> Unit
                            is ForgotPasswordState.Loading ->
                                Toast.makeText(this@LoginActivity, "Sending reset link…", Toast.LENGTH_SHORT).show()
                            is ForgotPasswordState.Success -> {
                                Toast.makeText(this@LoginActivity, "Reset link sent! Check your email.", Toast.LENGTH_LONG).show()
                                viewModel.resetForgotPasswordState()
                            }
                            is ForgotPasswordState.Error -> {
                                Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                                viewModel.resetForgotPasswordState()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !loading
        binding.loginButton.alpha = if (loading) 0.7f else 1f
    }
}
