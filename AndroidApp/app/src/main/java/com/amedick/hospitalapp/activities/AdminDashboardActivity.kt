package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.NotificationAdapter
import com.amedick.hospitalapp.databinding.ActivityAdminDashboardBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var notificationAdapter: NotificationAdapter

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupRecyclerView()
        observeRealtimeData()
    }

    override fun onResume() {
        super.onResume()
        loadStaticPlatformStats()
    }

    private fun setupListeners() {
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            com.amedick.hospitalapp.utils.LogoutHelper.showLogoutConfirmation(this)
        }

        binding.cardDoctorVerification.setOnClickListener {
            startActivity(Intent(this, AdminVerificationActivity::class.java))
        }

        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.tvViewAllNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.cardPatients.setOnClickListener {
            startActivity(Intent(this, AdminPatientListActivity::class.java))
        }

        binding.cardDoctors.setOnClickListener {
            val intent = Intent(this, AdminDoctorListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "ALL")
            startActivity(intent)
        }

        binding.cardVerified.setOnClickListener {
            val intent = Intent(this, AdminDoctorListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "VERIFIED")
            startActivity(intent)
        }

        binding.cardPending.setOnClickListener {
            startActivity(Intent(this, AdminVerificationActivity::class.java))
        }

        binding.cardAppointments.setOnClickListener {
            val intent = Intent(this, AdminAppointmentListActivity::class.java)
            intent.putExtra("FILTER_TYPE", "ALL")
            startActivity(intent)
        }

        binding.cardCompleted.setOnClickListener {
            startActivity(Intent(this, AdminCompletedAppointmentsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            notifications = emptyList(),
            onClick = { notification ->
                if (!notification.isRead) {
                    lifecycleScope.launch {
                        firestoreRepository.markNotificationRead(notification.notificationId)
                    }
                }
                if (notification.type == "doctor_verification") {
                    val intent = Intent(this, AdminVerificationActivity::class.java)
                    intent.putExtra("EXTRA_DOCTOR_ID", notification.relatedId)
                    startActivity(intent)
                }
            }
        )
        binding.rvRecentNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvRecentNotifications.adapter = notificationAdapter
    }

    private fun observeRealtimeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Listen to pending doctors count
                launch {
                    firestoreRepository.getPendingDoctorsCountRealtime().collect { result ->
                        result.onSuccess { count ->
                            binding.tvPendingDoctors.text = count.toString()
                            binding.tvActionPendingCount.text = if (count > 0) {
                                "$count pending request${if (count > 1) "s" else ""}"
                            } else {
                                "No pending requests"
                            }
                        }
                    }
                }

                // Listen to admin notifications
                launch {
                    val adminId = authRepository.getCurrentUserId() ?: return@launch
                    firestoreRepository.getAdminNotificationsRealtime(adminId).collect { result ->
                        result.onSuccess { notifications ->
                            val unreadCount = notifications.count { !it.isRead }
                            
                            // Update Badge
                            if (unreadCount > 0) {
                                binding.badgeNotification.visibility = View.VISIBLE
                                binding.tvBadgeCount.text = if (unreadCount > 99) "99+" else unreadCount.toString()
                            } else {
                                binding.badgeNotification.visibility = View.GONE
                            }

                            // Update Recent List
                            if (notifications.isEmpty()) {
                                binding.tvNoNotifications.visibility = View.VISIBLE
                                binding.rvRecentNotifications.visibility = View.GONE
                            } else {
                                binding.tvNoNotifications.visibility = View.GONE
                                binding.rvRecentNotifications.visibility = View.VISIBLE
                                notificationAdapter.updateData(notifications.take(5))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadStaticPlatformStats() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = firestoreRepository.getPlatformStats()
            binding.progressBar.visibility = View.GONE
            
            result.onSuccess { stats ->
                binding.tvTotalPatients.text = stats["patients"].toString()
                binding.tvTotalDoctors.text = stats["doctors"].toString()
                binding.tvVerifiedDoctors.text = stats["verifiedDoctors"].toString()
                binding.tvTotalAppointments.text = stats["totalAppointments"].toString()
                binding.tvCompletedAppointments.text = stats["completedAppointments"].toString()
            }.onFailure {
                Toast.makeText(this@AdminDashboardActivity, "Failed to load stats", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
