package com.vladima.cursandroid.ui.splash_screen

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.ActivitySplashScreenBinding
import com.vladima.cursandroid.ui.authentication.AuthenticateActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        window?.setDecorFitsSystemWindows(false)

        lifecycleScope.launch {
            delay(2000)
            startActivity(Intent(this@SplashScreenActivity, AuthenticateActivity::class.java))
            finish()
        }
    }
}