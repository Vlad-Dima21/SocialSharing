package com.vladima.cursandroid.ui.main

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.ActivityMainBinding
import com.vladima.cursandroid.ui.main.friends.FriendsFragment
import com.vladima.cursandroid.ui.main.friends.FriendsViewModel
import com.vladima.cursandroid.ui.main.home.HomeFragment
import com.vladima.cursandroid.ui.main.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var friendsViewModel: FriendsViewModel
    private var selectedBottomId = R.id.home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        friendsViewModel = ViewModelProvider(this)[FriendsViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.materialToolbar2)
        setContentView(binding.root)

        intent.getBooleanExtra("friend_added", false).let {
            if (it) {
                replaceFragment(FriendsFragment())
                binding.bottomNavigationView.selectedItemId = R.id.friends
            } else {
                replaceFragment(HomeFragment())
            }
        }

        savedInstanceState?.getInt("selectedBottomItem").let {
            when (it) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.friends -> replaceFragment(FriendsFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
            }
            if (it != null) {
                selectedBottomId = it
            }
        }

        intent.data?.let { uri ->
            val parameters = uri.pathSegments
            with(parameters) {
                if (size == 2 && get(size - 2) == "new-friend") {
                    replaceFragment(FriendsFragment())
                    binding.bottomNavigationView.selectedItemId = R.id.friends
                    friendsViewModel.alertNewFriend(get(size - 1))
                }
            }
        }

        lifecycleScope.launch {
            friendsViewModel.potentialFriendName.collect { name ->
                if (name.isNotEmpty()) {
                    val dialog = Dialog(this@MainActivity)
                    dialog.setContentView(R.layout.new_friend_dialog)
                    dialog.findViewById<TextView>(R.id.dialog_title).text = getString(R.string.dialog_title, name)
                    val okBtn = dialog.findViewById<Button>(R.id.ok_btn)
                    val cancelBtn = dialog.findViewById<Button>(R.id.cancel_btn)

                    okBtn.setOnClickListener {
                        friendsViewModel.addNewFriend()
                        dialog.dismiss()
                    }
                    cancelBtn.setOnClickListener {
                        dialog.dismiss()
                    }
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.friends -> replaceFragment(FriendsFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
            }
            selectedBottomId = item.itemId
            true
        }

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedBottomItem", selectedBottomId)
    }
}