package com.vladima.cursandroid.ui.main.friends

import android.animation.ObjectAnimator
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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.FragmentFriendsBinding
import com.vladima.cursandroid.models.RVUserPost
import com.vladima.cursandroid.ui.MarginItemDecoration
import com.vladima.cursandroid.ui.main.home.HomeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding
    private var posts = listOf<RVUserPost>()
    private var postsAdapter = HomeAdapter(listOf())

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

        with(binding.rvPosts) {
            addItemDecoration(MarginItemDecoration(80))
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }

        lifecycleScope.launch {
            viewModel.friendsPosts.collect { list ->
                posts = list
                postsAdapter.setNewPosts(posts)
                withContext(Dispatchers.Main) {
                    if (posts.isEmpty()) {
                        binding.noFriends.visibility = View.VISIBLE
                    } else {
                        binding.noFriends.visibility = View.GONE
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
                        }
                    }
                }
            }
        }

        val btnAnimatorOffScreen = ObjectAnimator.ofFloat(binding.addFriends, "translationY", 300f)
            .apply {
                duration = 1000
            }
        val btnAnimatorOnScreen = ObjectAnimator.ofFloat(binding.addFriends, "translationY", 0f)
            .apply {
                duration = 1000
            }

        var scrollDown = false
        binding.rvPosts.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && !scrollDown) {
                    btnAnimatorOffScreen.start()
                } else if (dy < 0 && scrollDown) {
                    btnAnimatorOnScreen.start()
                }
                scrollDown = dy > 0
            }
        })

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

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getFriendsPosts()
        }

        return binding.root
    }
}