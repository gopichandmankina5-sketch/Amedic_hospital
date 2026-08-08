package com.amedick.hospitalapp.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amedick.hospitalapp.databinding.ItemReviewBinding
import com.amedick.hospitalapp.models.Review
import java.util.Date

class DoctorReviewAdapter(
    private var reviews: List<Review>
) : RecyclerView.Adapter<DoctorReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.ratingBar.rating = review.rating
            binding.tvFeedback.text = review.feedback
            binding.tvFeedback.visibility = if (review.feedback.isNotEmpty()) View.VISIBLE else View.GONE
            
            // Format time ago
            if (review.createdAt != null) {
                val now = System.currentTimeMillis()
                val timeAgo = DateUtils.getRelativeTimeSpanString(
                    review.createdAt.time,
                    now,
                    DateUtils.MINUTE_IN_MILLIS
                )
                binding.tvDate.text = timeAgo
            } else {
                binding.tvDate.text = "Just now"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
