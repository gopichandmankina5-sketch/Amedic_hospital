package com.amedick.hospitalapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedick.hospitalapp.adapters.DoctorAdapter
import com.amedick.hospitalapp.databinding.ActivityAdminDoctorListBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.Doctor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminDoctorListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDoctorListBinding
    private lateinit var adapter: DoctorAdapter
    private var allDoctors = listOf<Doctor>()
    private var filterType = "ALL" // ALL or VERIFIED

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDoctorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filterType = intent.getStringExtra("FILTER_TYPE") ?: "ALL"

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeDoctors()
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (filterType == "VERIFIED") "Verified Doctors" else "All Doctors"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = DoctorAdapter(emptyList()) { doctor ->
            val intent = Intent(this, DoctorDetailsActivity::class.java)
            intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR, doctor)
            intent.putExtra(DoctorDetailsActivity.EXTRA_IS_ADMIN, true)
            startActivity(intent)
        }
        binding.rvDoctors.layoutManager = LinearLayoutManager(this)
        binding.rvDoctors.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDoctors(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterDoctors(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allDoctors)
        } else {
            val lowerCaseQuery = query.lowercase()
            val filtered = allDoctors.filter {
                it.name.lowercase().contains(lowerCaseQuery) ||
                it.specialization.lowercase().contains(lowerCaseQuery) ||
                it.email.lowercase().contains(lowerCaseQuery)
            }
            adapter.updateData(filtered)
        }
    }

    private fun observeDoctors() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvDoctors.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                firestoreRepository.getDoctorsRealtime(filterType).collect { result ->
                    binding.progressBar.visibility = View.GONE
                    
                    result.onSuccess { doctors ->
                        allDoctors = doctors
                        filterDoctors(binding.etSearch.text.toString())
                        
                        if (doctors.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvDoctors.visibility = View.GONE
                            binding.tvEmptyMessage.text = if (filterType == "VERIFIED") "No verified doctors yet." else "No doctors registered yet."
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvDoctors.visibility = View.VISIBLE
                        }
                    }.onFailure {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.tvEmptyMessage.text = "Unable to load information. Please try again."
                        Toast.makeText(this@AdminDoctorListActivity, "Failed to load doctors", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
