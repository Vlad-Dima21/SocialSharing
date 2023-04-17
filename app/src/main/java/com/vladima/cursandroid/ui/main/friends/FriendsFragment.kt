package com.vladima.cursandroid.ui.main.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFriendsBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).supportActionBar?.let {
            it.show()
            it.title = getString(R.string.friends)
        }

        val viewModel = ViewModelProvider(activity as ViewModelStoreOwner)[FriendsViewModel::class.java]

        return binding.root
    }
}