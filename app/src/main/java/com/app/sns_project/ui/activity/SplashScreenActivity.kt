package com.app.sns_project.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashScreenActivity : AppCompatActivity() {
    val currentUid = Firebase.auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)

            if(currentUid==null){
                startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
            }else{
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            }
            finish()
        }

    }
}