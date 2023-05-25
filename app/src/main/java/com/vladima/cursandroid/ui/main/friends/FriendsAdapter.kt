package com.vladima.cursandroid.ui.main.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vladima.cursandroid.databinding.FriendsImageViewholderBinding
import com.vladima.cursandroid.models.RVFriendPost
import com.vladima.cursandroid.models.RVUserPost

class FriendsAdapter(private var posts: List<RVFriendPost>):
    RecyclerView.Adapter<FriendsAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: FriendsImageViewholderBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = FriendsImageViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder) {
            with(posts[position]) {
                binding.imageView.setImageBitmap(imageBitmap)
                binding.authorName.text = authorName
                binding.imageDescription.text = imageDescription
            }
        }
    }

    fun setNewPosts(newList: List<RVFriendPost>) {
        posts = newList
        notifyDataSetChanged()
    }
}