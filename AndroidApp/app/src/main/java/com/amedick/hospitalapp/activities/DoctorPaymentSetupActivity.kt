package com.amedick.hospitalapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amedick.hospitalapp.config.CloudinaryConfig
import com.amedick.hospitalapp.databinding.ActivityDoctorPaymentSetupBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.DoctorPaymentInfo
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class DoctorPaymentSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorPaymentSetupBinding
    private var doctorId: String = ""
    private var currentQrUrl: String = ""
    private var currentFee: Double = 0.0

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var firestoreRepository: FirestoreRepository

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                Log.w("PaymentSetup", "Could not take persistable permission", e)
            }
            uploadQrPhoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorPaymentSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doctorId = authRepository.getCurrentUserId() ?: run {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.cardUploadQr.setOnClickListener {
            pickImage()
        }

        binding.btnSave.setOnClickListener {
            savePaymentDetails()
        }

        loadPaymentDetails()
    }

    private fun loadPaymentDetails() {
        binding.progressBarLayout.visibility = View.VISIBLE
        lifecycleScope.launch {
            val userResult = firestoreRepository.getUserProfile(doctorId)
            if (userResult.isSuccess) {
                currentFee = userResult.getOrNull()?.consultationFee ?: 0.0
                binding.etFee.setText(if (currentFee > 0) currentFee.toString() else "")
            }
            
            val infoResult = firestoreRepository.getDoctorPaymentInfo(doctorId)
            if (infoResult.isSuccess) {
                val info = infoResult.getOrNull()
                if (info != null) {
                    binding.etUpiId.setText(info.upiId)
                    currentQrUrl = info.paymentQrUrl
                    
                    if (currentQrUrl.isNotEmpty()) {
                        binding.ivQrPreview.visibility = View.VISIBLE
                        binding.layoutUploadPlaceholder.visibility = View.GONE
                        Glide.with(this@DoctorPaymentSetupActivity)
                            .load(currentQrUrl)
                            .into(binding.ivQrPreview)
                    }
                }
            }
            binding.progressBarLayout.visibility = View.GONE
        }
    }

    private fun savePaymentDetails() {
        val upiId = binding.etUpiId.text.toString().trim()
        val feeStr = binding.etFee.text.toString().trim()
        
        if (upiId.isEmpty()) {
            Toast.makeText(this, "Please enter your UPI ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        val fee = feeStr.toDoubleOrNull() ?: 0.0

        binding.progressBarLayout.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val paymentInfo = DoctorPaymentInfo(
                id = "details",
                upiId = upiId,
                paymentQrUrl = currentQrUrl
            )
            
            val result = firestoreRepository.saveDoctorPaymentInfo(doctorId, paymentInfo)
            
            // Also update fee in main profile
            val resultFee = runCatching {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Users").document(doctorId).update("consultationFee", fee)
            }
            
            binding.progressBarLayout.visibility = View.GONE
            if (result.isSuccess && resultFee.isSuccess) {
                Toast.makeText(this@DoctorPaymentSetupActivity, "Payment details saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@DoctorPaymentSetupActivity, "Failed to save details.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    private fun uploadQrPhoto(uri: Uri) {
        binding.progressBarLayout.visibility = View.VISIBLE
        
        var fileName = "payment_qr.jpg"
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) fileName = cursor.getString(nameIdx)
                }
            }
        } catch (_: Exception) {}

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) throw Exception("Failed to read file bytes")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                    .addFormDataPart("file", fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/auto/upload")
                    .post(requestBody)
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("Cloudinary server error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response")
                val secureUrl = JSONObject(responseBody).getString("secure_url")

                withContext(Dispatchers.Main) {
                    currentQrUrl = secureUrl
                    binding.ivQrPreview.visibility = View.VISIBLE
                    binding.layoutUploadPlaceholder.visibility = View.GONE
                    Glide.with(this@DoctorPaymentSetupActivity)
                        .load(currentQrUrl)
                        .into(binding.ivQrPreview)
                        
                    binding.progressBarLayout.visibility = View.GONE
                    Toast.makeText(this@DoctorPaymentSetupActivity, "QR Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PaymentSetup", "Failed to upload photo", e)
                withContext(Dispatchers.Main) {
                    binding.progressBarLayout.visibility = View.GONE
                    Toast.makeText(this@DoctorPaymentSetupActivity, "Failed to upload QR image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
