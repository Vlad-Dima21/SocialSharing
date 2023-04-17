package com.vladima.cursandroid.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vladima.cursandroid.databinding.ImageViewholderBinding
import com.vladima.cursandroid.models.RVUserPost

class HomeAdapter(
    private var posts: List<RVUserPost>
): RecyclerView.Adapter<HomeAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ImageViewholderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder) {
            with(posts[position]) {
                binding.imageView.setImageBitmap(imageBitmap)
                binding.imageDescription.text = imageDescription
            }
        }
    }

    fun setNewPosts(newList: List<RVUserPost>) {
        posts = newList
        notifyDataSetChanged()
    }
}