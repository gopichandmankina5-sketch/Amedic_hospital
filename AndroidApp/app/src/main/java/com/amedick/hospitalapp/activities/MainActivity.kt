package com.amedick.hospitalapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.amedick.hospitalapp.R
import com.amedick.hospitalapp.databinding.ActivityMainBinding
import com.amedick.hospitalapp.fragments.AppointmentHistoryFragment
import com.amedick.hospitalapp.fragments.DoctorListFragment
import com.amedick.hospitalapp.fragments.HomeFragment
import com.amedick.hospitalapp.fragments.ProfileFragment
import com.amedick.hospitalapp.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.mainFragmentContainer, HomeFragment())
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeMenu -> replaceFragment(HomeFragment())
                R.id.doctorsMenu -> replaceFragment(DoctorListFragment())
                R.id.appointmentsMenu -> replaceFragment(AppointmentHistoryFragment())
                R.id.profileMenu -> replaceFragment(ProfileFragment())
                R.id.settingsMenu -> replaceFragment(SettingsFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.commit {
            replace(R.id.mainFragmentContainer, fragment)
        }
    }
}
