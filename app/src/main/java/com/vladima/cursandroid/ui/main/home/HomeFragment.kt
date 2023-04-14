package com.vladima.cursandroid.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.FragmentHomeBinding
import com.vladima.cursandroid.models.User
import com.vladima.cursandroid.models.UserPost
import com.vladima.cursandroid.ui.MarginItemDecoration
import com.vladima.cursandroid.ui.authentication.AuthenticateActivity
import com.vladima.cursandroid.ui.main.FriendsFragment
import com.vladima.cursandroid.ui.main.new_post.CreatePostActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment(private val viewModel: HomeViewModel) : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var userPosts = listOf<UserPost>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).supportActionBar?.let {
            it.title = getString(R.string.your_recent_activity)
        }

        binding.rvPosts.addItemDecoration(MarginItemDecoration(100))

        lifecycleScope.launch {
            viewModel.userPosts.collect { list ->
                if (list.isNotEmpty()) {
                    with(binding) {
                        rvPosts.layoutManager = LinearLayoutManager(context)
                        userPosts = list
                        rvPosts.adapter = HomeAdapter(userPosts)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    withContext(Dispatchers.Main) {
                        with(binding) {
                            swipeRefresh.isRefreshing = true
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        with(binding) {
                            swipeRefresh.isRefreshing = false
                            if (userPosts.isEmpty()) {
                                noPosts.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

        binding.addPost.setOnClickListener {
            startActivity(Intent(context, CreatePostActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadCurrentUserPosts()
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}