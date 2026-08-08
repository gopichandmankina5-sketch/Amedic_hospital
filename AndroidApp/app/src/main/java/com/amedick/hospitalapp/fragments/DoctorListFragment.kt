package com.amedick.hospitalapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.activities.DoctorDetailsActivity
import com.amedick.hospitalapp.adapters.DoctorAdapter
import com.amedick.hospitalapp.databinding.FragmentDoctorListBinding
import com.amedick.hospitalapp.viewmodel.HomeState
import com.amedick.hospitalapp.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DoctorListFragment : Fragment() {

    private var _binding: FragmentDoctorListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var doctorAdapter: DoctorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doctorAdapter = DoctorAdapter(emptyList()) { doctor ->
            startActivity(DoctorDetailsActivity.newIntent(requireContext(), doctor))
        }
        binding.doctorRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorAdapter
        }

        binding.searchInput.addTextChangedListener { applyFilters() }
        binding.chipVerified.setOnCheckedChangeListener { _, _ -> applyFilters() }

        viewModel.loadDoctors()
        observeViewModel()
    }

    private fun applyFilters() {
        val query = binding.searchInput.text.toString().trim()
        val isVerifiedOnly = binding.chipVerified.isChecked
        val allDoctors = (viewModel.doctorsState.value as? HomeState.DoctorsLoaded)?.doctors ?: emptyList()
        
        val filtered = allDoctors.filter { doctor ->
            val matchesQuery = if (query.isEmpty()) true else {
                doctor.name.contains(query, ignoreCase = true) ||
                doctor.specialization.contains(query, ignoreCase = true)
            }
            val matchesVerified = if (isVerifiedOnly) doctor.isVerified else true
            matchesQuery && matchesVerified
        }
        
        doctorAdapter.updateData(filtered)
        binding.emptyStateLayout.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.doctorRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.doctorsState.collect { state ->
                    when (state) {
                        is HomeState.Idle -> binding.progressBar.visibility = View.GONE
                        is HomeState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.doctorRecycler.visibility = View.GONE
                        }
                        is HomeState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.emptyState.text = state.message
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.doctorRecycler.visibility = View.GONE
                        }
                        is HomeState.DoctorsLoaded -> {
                            binding.progressBar.visibility = View.GONE
                            applyFilters()
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
