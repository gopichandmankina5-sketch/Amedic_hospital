package com.amedick.hospitalapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.activities.MainActivity
import com.amedick.hospitalapp.adapters.AppointmentAdapter
import com.amedick.hospitalapp.databinding.FragmentAppointmentHistoryBinding
import com.amedick.hospitalapp.models.AppointmentStatus
import com.amedick.hospitalapp.viewmodel.AppointmentListState
import com.amedick.hospitalapp.viewmodel.AppointmentViewModel
import com.amedick.hospitalapp.viewmodel.CancelState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppointmentHistoryFragment : Fragment() {

    private var _binding: FragmentAppointmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppointmentViewModel by viewModels()
    private lateinit var appointmentAdapter: AppointmentAdapter

    @javax.inject.Inject lateinit var firestoreRepository: com.amedick.hospitalapp.firebase.FirestoreRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = firestoreRepository.getCurrentUserId() ?: ""

        appointmentAdapter = AppointmentAdapter(
            appointments = emptyList(),
            onCancelClick = { appointment ->
                if (appointment.status == AppointmentStatus.PENDING || appointment.status == AppointmentStatus.ACCEPTED) {
                    showCancelDialog(appointment.appointmentId)
                }
            },
            onOpenChatClick = { appointment ->
                val intent = android.content.Intent(requireContext(), com.amedick.hospitalapp.activities.ChatActivity::class.java).apply {
                    putExtra(com.amedick.hospitalapp.activities.ChatActivity.EXTRA_APPOINTMENT_ID, appointment.appointmentId)
                    putExtra(com.amedick.hospitalapp.activities.ChatActivity.EXTRA_OTHER_USER_ID, appointment.doctorId)
                    putExtra(com.amedick.hospitalapp.activities.ChatActivity.EXTRA_OTHER_USER_NAME, "Dr. ${appointment.doctorName}")
                }
                startActivity(intent)
            },
            onRateClick = { appointment ->
                val intent = android.content.Intent(requireContext(), com.amedick.hospitalapp.activities.ReviewActivity::class.java).apply {
                    putExtra("appointmentId", appointment.appointmentId)
                    putExtra("doctorId", appointment.doctorId)
                }
                startActivity(intent)
            },
            onJoinMeetClick = { appointment ->
                joinGoogleMeet(appointment.doctorId)
            },
            onRescheduleClick = { appointment ->
                val intent = android.content.Intent(requireContext(), com.amedick.hospitalapp.activities.BookAppointmentActivity::class.java).apply {
                    putExtra(com.amedick.hospitalapp.activities.BookAppointmentActivity.EXTRA_DOCTOR_ID, appointment.doctorId)
                    putExtra(com.amedick.hospitalapp.activities.BookAppointmentActivity.EXTRA_DOCTOR_NAME, appointment.doctorName)
                    putExtra("EXTRA_RESCHEDULE_APPOINTMENT_ID", appointment.appointmentId)
                }
                startActivity(intent)
            },
            onVerifyCompletionClick = { appointment, isConfirmed ->
                if (isConfirmed) {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Completion")
                        .setMessage("Are you sure this consultation was completed?")
                        .setPositiveButton("Yes") { _, _ ->
                            viewModel.respondToCompletionVerification(appointment.appointmentId, true) { success, msg ->
                                if (success) {
                                    Toast.makeText(requireContext(), "Appointment completed successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), msg ?: "Action failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Consultation Not Completed")
                        .setMessage("Are you sure you want to state that the consultation was not completed? Your doctor will be notified.")
                        .setPositiveButton("Yes") { _, _ ->
                            viewModel.respondToCompletionVerification(appointment.appointmentId, false) { success, msg ->
                                if (success) {
                                    Toast.makeText(requireContext(), "Completion not confirmed", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), msg ?: "Action failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            },
            onPaymentSubmitClick = { appointment ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirm Payment")
                    .setMessage("Are you sure you have paid ₹${appointment.consultationFee} via UPI ID: ${appointment.upiId}?")
                    .setPositiveButton("Yes, I Have Paid") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            val result = firestoreRepository.updateAppointmentPaymentStatus(appointment.appointmentId, "submitted")
                            if (result.isSuccess) {
                                Toast.makeText(requireContext(), "Payment status updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to update payment status", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.appointmentRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
        }

        binding.bookButton.setOnClickListener {
            (activity as? MainActivity)?.navigateToDoctors()
        }

        viewModel.loadMyAppointments()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyAppointments()
    }

    private fun joinGoogleMeet(doctorId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = firestoreRepository.getUserProfile(doctorId)
            result.onSuccess { doctorProfile ->
                val link = doctorProfile.googleMeetLink
                if (link.isNotEmpty() && link.startsWith("https://meet.google.com/")) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link))
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "The doctor has not set up their meeting link yet.", Toast.LENGTH_LONG).show()
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to load doctor profile. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCancelDialog(appointmentId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                viewModel.cancelAppointment(appointmentId)
            }
            .setNegativeButton("Keep Appointment", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.listState.collect { state ->
                        when (state) {
                            is AppointmentListState.Idle -> binding.progressBar.visibility = View.GONE
                            is AppointmentListState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.emptyStateLayout.visibility = View.GONE
                            }
                            is AppointmentListState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.emptyState.text = state.message
                                binding.emptyStateLayout.visibility = View.VISIBLE
                            }
                            is AppointmentListState.Loaded -> {
                                binding.progressBar.visibility = View.GONE
                                if (state.appointments.isEmpty()) {
                                    binding.emptyStateLayout.visibility = View.VISIBLE
                                    binding.appointmentRecycler.visibility = View.GONE
                                } else {
                                    binding.emptyStateLayout.visibility = View.GONE
                                    binding.appointmentRecycler.visibility = View.VISIBLE
                                    appointmentAdapter.updateData(state.appointments)
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.cancelState.collect { state ->
                        when (state) {
                            is CancelState.Success -> {
                                Toast.makeText(requireContext(), "Appointment cancelled.", Toast.LENGTH_SHORT).show()
                                viewModel.resetCancelState()
                                viewModel.loadMyAppointments()
                            }
                            is CancelState.Error -> {
                                Toast.makeText(requireContext(), "Failed to cancel appointment", Toast.LENGTH_SHORT).show()
                                viewModel.resetCancelState()
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
