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
    private var selectedRole = "patient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRoleSelector()

        binding.registerButton.setOnClickListener {
            if (validateInputs()) {
                setLoading(true)
                val exp = binding.experienceInput.text.toString().toIntOrNull() ?: 0
                val fee = binding.feeInput.text.toString().toDoubleOrNull() ?: 0.0
                
                viewModel.register(
                    name = binding.nameInput.text.toString().trim(),
                    email = binding.emailInput.text.toString().trim(),
                    phone = binding.phoneInput.text.toString().trim(),
                    password = binding.passwordInput.text.toString(),
                    role = selectedRole,
                    specialization = binding.specializationInput.text.toString().trim(),
                    qualification = binding.qualificationInput.text.toString().trim(),
                    experience = exp,
                    consultationFee = fee,
                    hospital = binding.hospitalInput.text.toString().trim(),
                    location = binding.locationInput.text.toString().trim()
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
    
    private fun setupRoleSelector() {
        binding.cardPatient.setOnClickListener {
            selectedRole = "patient"
            updateRoleUI()
        }
        binding.cardDoctor.setOnClickListener {
            selectedRole = "doctor"
            updateRoleUI()
        }
        updateRoleUI()
    }
    
    private fun updateRoleUI() {
        val primaryColor = getColor(com.amedick.hospitalapp.R.color.color_primary)
        val outlineColor = getColor(com.amedick.hospitalapp.R.color.color_outline)
        val surfaceColor = getColor(com.amedick.hospitalapp.R.color.color_surface)
        val primaryLightColor = getColor(com.amedick.hospitalapp.R.color.color_primary_light)

        if (selectedRole == "patient") {
            binding.cardPatient.strokeWidth = 4
            binding.cardPatient.strokeColor = primaryColor
            binding.cardPatient.setCardBackgroundColor(primaryLightColor)
            
            binding.cardDoctor.strokeWidth = 2
            binding.cardDoctor.strokeColor = outlineColor
            binding.cardDoctor.setCardBackgroundColor(surfaceColor)
            
            binding.doctorFieldsContainer.visibility = View.GONE
        } else {
            binding.cardDoctor.strokeWidth = 4
            binding.cardDoctor.strokeColor = primaryColor
            binding.cardDoctor.setCardBackgroundColor(primaryLightColor)
            
            binding.cardPatient.strokeWidth = 2
            binding.cardPatient.strokeColor = outlineColor
            binding.cardPatient.setCardBackgroundColor(surfaceColor)
            
            binding.doctorFieldsContainer.visibility = View.VISIBLE
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
        
        if (selectedRole == "doctor") {
            if (binding.specializationInput.text.toString().trim().isEmpty()) {
                binding.specializationInputLayout.error = "Required"
                valid = false
            } else {
                binding.specializationInputLayout.error = null
            }
            if (binding.qualificationInput.text.toString().trim().isEmpty()) {
                binding.qualificationInputLayout.error = "Required"
                valid = false
            } else {
                binding.qualificationInputLayout.error = null
            }
            if (binding.experienceInput.text.toString().trim().isEmpty()) {
                binding.experienceInputLayout.error = "Required"
                valid = false
            } else {
                binding.experienceInputLayout.error = null
            }
            if (binding.hospitalInput.text.toString().trim().isEmpty()) {
                binding.hospitalInputLayout.error = "Required"
                valid = false
            } else {
                binding.hospitalInputLayout.error = null
            }
            if (binding.locationInput.text.toString().trim().isEmpty()) {
                binding.locationInputLayout.error = "Required"
                valid = false
            } else {
                binding.locationInputLayout.error = null
            }
            if (binding.feeInput.text.toString().trim().isEmpty()) {
                binding.feeInputLayout.error = "Required"
                valid = false
            } else {
                binding.feeInputLayout.error = null
            }
        }

        return valid
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !loading
        binding.registerButton.alpha = if (loading) 0.7f else 1f
    }
}
