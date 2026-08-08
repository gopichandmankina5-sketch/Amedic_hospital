package com.amedick.hospitalapp.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ActivityChatBinding
import com.amedick.hospitalapp.databinding.ItemChatMessageBinding
import com.amedick.hospitalapp.firebase.AuthRepository
import com.amedick.hospitalapp.firebase.FirestoreRepository
import com.amedick.hospitalapp.models.ChatMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_APPOINTMENT_ID = "appointmentId"
        const val EXTRA_OTHER_USER_ID = "otherUserId"
        const val EXTRA_OTHER_USER_NAME = "otherUserName"
    }

    private lateinit var binding: ActivityChatBinding
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private var appointmentId: String = ""
    private var otherUserId: String = ""
    private var currentUserId: String = ""
    
    private val messagesList = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = authRepository.getCurrentUserId() ?: return finish()

        appointmentId = intent.getStringExtra(EXTRA_APPOINTMENT_ID) ?: ""
        if (appointmentId.isEmpty()) {
            Toast.makeText(this, "Unable to open this conversation.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: ""
        val otherUserName = intent.getStringExtra(EXTRA_OTHER_USER_NAME) ?: "Chat"

        binding.backButton.setOnClickListener { finish() }
        binding.tvOtherUserName.text = otherUserName
        binding.tvAppointmentStatus.text = "Accepted" // By definition, only accepted apps get chat

        setupRecyclerView()
        observeMessages()

        binding.btnSend.setOnClickListener {
            val text = binding.etMessageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messagesList, currentUserId)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = adapter
    }

    private fun observeMessages() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            firestoreRepository.getChatMessages(appointmentId).collectLatest { result ->
                binding.progressBar.visibility = View.GONE
                result.onSuccess { messages ->
                    messagesList.clear()
                    messagesList.addAll(messages)
                    adapter.notifyDataSetChanged()
                    
                    if (messagesList.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvMessages.scrollToPosition(messagesList.size - 1)
                    }
                }.onFailure {
                    Toast.makeText(this@ChatActivity, "Unable to load messages. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        binding.btnSend.isEnabled = false
        val message = ChatMessage(
            appointmentId = appointmentId,
            senderId = currentUserId,
            message = text
        )

        lifecycleScope.launch {
            val result = firestoreRepository.sendMessage(message)
            binding.btnSend.isEnabled = true
            result.onSuccess {
                binding.etMessageInput.text.clear()
                // Also send a notification
                firestoreRepository.createNotification(
                    userId = otherUserId,
                    title = "New Message",
                    message = "You have a new message regarding your appointment.",
                    type = "chat"
                )
            }.onFailure {
                Toast.makeText(this@ChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ChatAdapter(
        private val messages: List<ChatMessage>,
        private val currentUserId: String
    ) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val message = messages[position]
            val isSentByMe = message.senderId == currentUserId
            
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeString = if (message.timestamp != null) timeFormat.format(message.timestamp) else timeFormat.format(Date())

            if (isSentByMe) {
                holder.binding.layoutSent.visibility = View.VISIBLE
                holder.binding.layoutReceived.visibility = View.GONE
                holder.binding.tvSentMessage.text = message.message
                holder.binding.tvSentTime.text = timeString
            } else {
                holder.binding.layoutSent.visibility = View.GONE
                holder.binding.layoutReceived.visibility = View.VISIBLE
                holder.binding.tvReceivedMessage.text = message.message
                holder.binding.tvReceivedTime.text = timeString
            }
        }

        override fun getItemCount() = messages.size
    }
}
