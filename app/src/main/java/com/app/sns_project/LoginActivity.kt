package com.app.sns_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = "로그인"

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val userEmail = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val userPassword = findViewById<EditText>(R.id.editTextPassword).text.toString()

            if(userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                doLogin(userEmail, userPassword)
            }
            else {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }

        }

        val signupButton = findViewById<Button>(R.id.signupButton)
        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

    }

    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}