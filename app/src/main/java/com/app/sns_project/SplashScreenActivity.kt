package com.app.sns_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.sns_project.util.pushMessage
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