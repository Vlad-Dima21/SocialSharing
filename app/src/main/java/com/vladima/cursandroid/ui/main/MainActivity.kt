package com.vladima.cursandroid.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.ActivityMainBinding
import com.vladima.cursandroid.ui.main.home.HomeFragment
import com.vladima.cursandroid.ui.main.home.HomeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.materialToolbar2)

        setContentView(binding.root)
        replaceFragment(HomeFragment(homeViewModel))

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home -> replaceFragment(HomeFragment(homeViewModel))
                R.id.friends -> replaceFragment(FriendsFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}