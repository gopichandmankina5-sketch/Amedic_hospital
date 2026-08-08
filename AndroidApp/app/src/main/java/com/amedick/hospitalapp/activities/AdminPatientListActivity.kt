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
import com.amedick.hospitalapp.adapters.AdminPatientAdapter
import com.amedick.hospitalapp.databinding.ActivityAdminPatientListBinding
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminPatientListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPatientListBinding
    private lateinit var adapter: AdminPatientAdapter
    private var allPatients = listOf<User>()

    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdminPatientAdapter(emptyList()) { patient ->
            val intent = Intent(this, AdminPatientDetailsActivity::class.java)
            intent.putExtra("PATIENT_ID", patient.uid)
            startActivity(intent)
        }
        binding.rvPatients.layoutManager = LinearLayoutManager(this)
        binding.rvPatients.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPatients(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterPatients(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allPatients)
        } else {
            val lowerCaseQuery = query.lowercase()
            val filtered = allPatients.filter {
                it.name.lowercase().contains(lowerCaseQuery) ||
                it.email.lowercase().contains(lowerCaseQuery) ||
                it.phone.contains(query)
            }
            adapter.updateData(filtered)
        }
    }

    private fun observePatients() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvPatients.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                firestoreRepository.getPatientsRealtime().collect { result ->
                    binding.progressBar.visibility = View.GONE
                    
                    result.onSuccess { patients ->
                        allPatients = patients
                        filterPatients(binding.etSearch.text.toString())
                        
                        if (patients.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvPatients.visibility = View.GONE
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvPatients.visibility = View.VISIBLE
                        }
                    }.onFailure {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.tvEmptyMessage.text = "Unable to load information. Please try again."
                        Toast.makeText(this@AdminPatientListActivity, "Failed to load patients", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
