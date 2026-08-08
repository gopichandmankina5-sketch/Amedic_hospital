package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.NotificationAdapter
import com.amedick.hospitalapp.databinding.ActivityNotificationsBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { notification ->
            if (!notification.isRead) {
                markAsRead(notification.notificationId)
            }
            if (notification.type == "doctor_verification") {
                val intent = Intent(this, AdminVerificationActivity::class.java)
                intent.putExtra("EXTRA_DOCTOR_ID", notification.relatedId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Notification: ${notification.title}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        val userId = authRepository.getCurrentUserId() ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        binding.rvNotifications.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        
        lifecycleScope.launch {
            val result = firestoreRepository.getNotificationsForUser(userId)
            binding.progressBar.visibility = View.GONE
            
            result.onSuccess { notifications ->
                if (notifications.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.rvNotifications.visibility = View.VISIBLE
                    adapter.updateData(notifications)
                }
            }.onFailure {
                binding.emptyStateLayout.visibility = View.VISIBLE
                Toast.makeText(this@NotificationsActivity, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markAsRead(notificationId: String) {
        lifecycleScope.launch {
            firestoreRepository.markNotificationRead(notificationId)
            loadNotifications() // Refresh list
        }
    }
}
