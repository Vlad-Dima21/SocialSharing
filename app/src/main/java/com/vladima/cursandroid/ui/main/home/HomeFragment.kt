package com.vladima.cursandroid.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toLowerCase
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
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
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var userPosts = listOf<UserPost>()
    private var postsAdapter = HomeAdapter(listOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewModel =
            ViewModelProvider(activity as ViewModelStoreOwner)[HomeViewModel::class.java]

        binding = FragmentHomeBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).supportActionBar?.hide()

        with(binding.rvPosts) {
            addItemDecoration(MarginItemDecoration(80))
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }

        lifecycleScope.launch {
            viewModel.userPosts.collect { list ->
                userPosts = list
                postsAdapter.setNewPosts(userPosts)
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
                            } else {
                                noPosts.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        binding.search.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterPosts(newText ?: "")
                    return true
                }
            }
        )

        binding.addPost.setOnClickListener {
            startActivity(Intent(context, CreatePostActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.search.setQuery("", false)
            viewModel.loadCurrentUserPosts()
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private var job: Job? = null
    private fun filterPosts(filter: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            if (filter.isNotEmpty()) {
                delay(500)
            }
            postsAdapter.setNewPosts(
                userPosts.filter {
                    it.imageDescription.lowercase().contains(filter.lowercase())
                }
            )
        }
    }
}