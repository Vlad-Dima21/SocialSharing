package com.vladima.cursandroid.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.vladima.cursandroid.databinding.FragmentHomeBinding
import com.vladima.cursandroid.models.RVUserPost
import com.vladima.cursandroid.ui.MarginItemDecoration
import com.vladima.cursandroid.ui.main.new_post.CreatePostActivity
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var posts = listOf<RVUserPost>()
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
                posts = list
                postsAdapter.setNewPosts(posts)
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
                            if (posts.isEmpty()) {
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
                posts.filter {
                    it.imageDescription.lowercase().contains(filter.lowercase())
                }
            )
        }
    }
}