package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amedick.hospitalapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @javax.inject.Inject
    lateinit var authRepository: com.amedick.hospitalapp.firebase.AuthRepository

    @javax.inject.Inject
    lateinit var firestoreRepository: com.amedick.hospitalapp.firebase.FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fade-in animation for the content
        lifecycleScope.launch {
            delay(300)
            binding.splashContent.animate()
                .alpha(1f)
                .setDuration(800)
                .start()

            delay(1800)

            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                // User is logged in, fetch role
                val result = firestoreRepository.getUserProfile(uid)
                result.onSuccess { user ->
                    val intent = when (user.role?.lowercase()) {
                        "admin" -> Intent(this@SplashActivity, AdminDashboardActivity::class.java)
                        "doctor" -> Intent(this@SplashActivity, DoctorDashboardActivity::class.java)
                        else -> Intent(this@SplashActivity, MainActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }.onFailure {
                    // Fallback to login if profile fetch fails
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }
}
