package com.amedick.hospitalapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.activities.DoctorDetailsActivity
import com.amedick.hospitalapp.activities.MainActivity
import com.amedick.hospitalapp.adapters.DoctorAdapter
import com.amedick.hospitalapp.databinding.FragmentHomeBinding
import com.amedick.hospitalapp.models.Appointment
import com.amedick.hospitalapp.models.AppointmentStatus
import com.amedick.hospitalapp.viewmodel.HomeState
import com.amedick.hospitalapp.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var doctorAdapter: DoctorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting()
        setupRecyclerView()
        setupQuickActions()
        setupSearch()
        observeViewModel()
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.greetingText.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    private fun setupRecyclerView() {
        doctorAdapter = DoctorAdapter(emptyList()) { doctor ->
            startActivity(DoctorDetailsActivity.newIntent(requireContext(), doctor))
        }
        binding.doctorRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupQuickActions() {
        binding.findDoctorCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToDoctors()
        }
        binding.bookAppointmentCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToDoctors()
        }
        binding.myAppointmentsCard.setOnClickListener {
            (activity as? MainActivity)?.navigateToAppointments()
        }
        binding.seeAllDoctors.setOnClickListener {
            (activity as? MainActivity)?.navigateToDoctors()
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener { text ->
            val query = text.toString().trim()
            val currentDoctors = (viewModel.doctorsState.value as? HomeState.DoctorsLoaded)?.doctors ?: emptyList()
            val filtered = if (query.isEmpty()) currentDoctors
            else currentDoctors.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.specialization.contains(query, ignoreCase = true)
            }
            doctorAdapter.updateData(filtered)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userState.collect { user ->
                        user?.let {
                            val firstName = it.name.split(" ").firstOrNull() ?: "there"
                            binding.userNameText.text = firstName
                        }
                    }
                }

                launch {
                    viewModel.doctorsState.collect { state ->
                        when (state) {
                            is HomeState.Idle -> binding.progressBar.visibility = View.GONE
                            is HomeState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.emptyState.visibility = View.GONE
                            }
                            is HomeState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.emptyState.text = "Unable to load doctors"
                                binding.emptyState.visibility = View.VISIBLE
                            }
                            is HomeState.DoctorsLoaded -> {
                                binding.progressBar.visibility = View.GONE
                                if (state.doctors.isEmpty()) {
                                    binding.emptyState.visibility = View.VISIBLE
                                    binding.emptyState.text = "No doctors found"
                                } else {
                                    binding.emptyState.visibility = View.GONE
                                }
                                doctorAdapter.updateData(state.doctors)
                            }
                        }
                    }
                }

                launch {
                    viewModel.upcomingAppointment.collect { appointment ->
                        if (appointment != null) {
                            showUpcomingAppointment(appointment)
                        } else {
                            binding.upcomingSection.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showUpcomingAppointment(appt: Appointment) {
        binding.upcomingSection.visibility = View.VISIBLE
        binding.upcomingDoctorName.text = if (appt.doctorName.isNotEmpty()) "Dr. ${appt.doctorName}" else "Appointment"
        binding.upcomingDate.text = "📅 ${appt.date}"
        binding.upcomingTime.text = "  🕐 ${appt.time}"
        binding.upcomingStatus.text = appt.status.lowercase().replaceFirstChar { it.uppercase() }

        val (chipBgRes, chipTextRes) = when (appt.status) {
            AppointmentStatus.CONFIRMED -> Pair(
                com.amedick.hospitalapp.R.color.status_confirmed_bg,
                com.amedick.hospitalapp.R.color.status_confirmed
            )
            AppointmentStatus.PENDING -> Pair(
                com.amedick.hospitalapp.R.color.status_pending_bg,
                com.amedick.hospitalapp.R.color.status_pending
            )
            else -> Pair(
                com.amedick.hospitalapp.R.color.status_pending_bg,
                com.amedick.hospitalapp.R.color.status_pending
            )
        }
        binding.upcomingStatus.setChipBackgroundColorResource(chipBgRes)
        binding.upcomingStatus.setTextColor(requireContext().getColor(chipTextRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
