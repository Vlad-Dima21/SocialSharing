package com.vladima.cursandroid.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vladima.cursandroid.databinding.ImageViewholderBinding
import com.vladima.cursandroid.models.UserPost

class HomeAdapter(
    private val userPosts: List<UserPost>
): RecyclerView.Adapter<HomeAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ImageViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = userPosts.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder) {
            with(userPosts[position]) {
                binding.imageView.setImageBitmap(imageBitmap)
            }
        }
    }
}