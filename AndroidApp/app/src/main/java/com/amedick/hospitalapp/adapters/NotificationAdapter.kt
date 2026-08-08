package com.amedick.hospitalapp.adapters

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemNotificationBinding
import com.amedick.hospitalapp.models.Notification

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.tvTitle.text = notification.title
            binding.tvMessage.text = notification.message
            
            val timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.tvTime.text = timeAgo
            
            if (notification.isRead) {
                binding.unreadIndicator.visibility = View.INVISIBLE
                binding.notificationContainer.setBackgroundColor(Color.TRANSPARENT)
            } else {
                binding.unreadIndicator.visibility = View.VISIBLE
                binding.notificationContainer.setBackgroundResource(com.amedick.hospitalapp.R.color.color_surface_variant)
            }
            
            binding.root.setOnClickListener {
                onClick(notification)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    fun updateData(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
