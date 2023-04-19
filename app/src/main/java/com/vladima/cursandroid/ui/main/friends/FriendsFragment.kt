package com.vladima.cursandroid.ui.main.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.firebase.auth.FirebaseAuth
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val currentUser = FirebaseAuth.getInstance().currentUser!!

        binding = FragmentFriendsBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).supportActionBar?.let {
            it.show()
            it.title = getString(R.string.friends)
        }

        val viewModel = ViewModelProvider(activity as ViewModelStoreOwner)[FriendsViewModel::class.java]

        binding.addFriends.setOnClickListener {
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "https://www.social-sharing.com/new-friend/${currentUser.uid}")
                    },
                    "Share link"
                )
            )
        }

        return binding.root
    }
}