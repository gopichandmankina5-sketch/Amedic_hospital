package com.amedick.hospitalapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.DoctorAdapter
import com.amedick.hospitalapp.databinding.FragmentHomeBinding
import com.amedick.hospitalapp.viewmodel.HomeState
import com.amedick.hospitalapp.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

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
        binding.doctorRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.doctorRecycler.adapter = DoctorAdapter(emptyList()) { doctor ->
            Toast.makeText(requireContext(), "Open ${doctor.name}", Toast.LENGTH_SHORT).show()
        }

        viewModel.loadDoctors()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.doctorsState.collect { state ->
                when (state) {
                    is HomeState.Idle -> binding.progressBar.visibility = View.GONE
                    is HomeState.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is HomeState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.emptyState.text = state.message
                        binding.emptyState.visibility = View.VISIBLE
                    }
                    is HomeState.DoctorsLoaded -> {
                        binding.progressBar.visibility = View.GONE
                        binding.emptyState.visibility = if (state.doctors.isEmpty()) View.VISIBLE else View.GONE
                        (binding.doctorRecycler.adapter as DoctorAdapter).updateData(state.doctors)
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
