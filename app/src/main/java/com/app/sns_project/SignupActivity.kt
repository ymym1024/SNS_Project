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

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.title = "회원가입"

        val signupButton = findViewById<Button>(R.id.signupButton2)
        signupButton.setOnClickListener {
            val userName = findViewById<EditText>(R.id.editTextUsername)
            val userEmail = findViewById<EditText>(R.id.editTextUserEmail).text.toString()
            val userPassword = findViewById<EditText>(R.id.editTextUserPassword).text.toString()
            val userPasswordConfirm = findViewById<EditText>(R.id.editTextUserPasswordComfirm).text.toString()

            if(userPassword.equals(userPasswordConfirm)) {
                doSignup(userEmail, userPassword)
            }
            else {
                Toast.makeText(this, "비밀번호를 다시 확인해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doSignup(userEmail: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공!!", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Creation failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}