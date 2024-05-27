package com.dicoding.capstone.cocodiag.features.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.capstone.cocodiag.R
import com.dicoding.capstone.cocodiag.common.setBottomNavBar
import com.dicoding.capstone.cocodiag.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setBottomNavBar(this@SettingsActivity, binding.bottomNavigation, R.id.nav_setting)
    }
}