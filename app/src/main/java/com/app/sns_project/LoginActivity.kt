package com.app.sns_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = "로그인"

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val signupButton = findViewById<Button>(R.id.signupButton)
        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

    }
}