package com.amedick.hospitalapp.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.amedick.hospitalapp.activities.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

/**
 * Reusable logout confirmation helper.
 * Shows a Material dialog and only logs out on explicit confirmation.
 * Use from any Activity across Admin, Doctor, and Patient flows.
 */
object LogoutHelper {

    fun showLogoutConfirmation(activity: AppCompatActivity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                android.widget.Toast.makeText(activity, "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
                activity.startActivity(
                    Intent(activity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                activity.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
