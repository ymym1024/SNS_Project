package com.app.sns_project.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.app.sns_project.R
import com.app.sns_project.databinding.ActivityMainBinding
import com.app.sns_project.ui.fragment.*
import com.app.sns_project.util.pushMessage

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()


        pushMessage().saveToken()
    }

    private fun initNavigation(){
        val bottomNavi = binding.bottomNav
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupActionBarWithNavController(this,navController)
        bottomNavi.setupWithNavController(navController)

        //supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment,MainFragment()).commit()
//        binding.bottomNav.setOnItemSelectedListener {
//            when(it.itemId){
//                R.id.mainFragment -> replaceFragment(MainFragment())
//                R.id.FollowFragment -> replaceFragment(FollowFragment())
//                R.id.postAddFragment -> replaceFragment(PostAddFragment())
//                R.id.ChatFragment -> replaceFragment(ChatFragment())
//                R.id.profileFragment -> replaceFragment(ProfileFragment())
//            }
//            true
//        }
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}


