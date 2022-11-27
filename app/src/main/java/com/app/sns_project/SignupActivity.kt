package com.app.sns_project

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.sns_project.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.title = "회원가입"

        val signupButton = findViewById<Button>(R.id.signupButton2)
        signupButton.setOnClickListener {
            val userName = findViewById<EditText>(R.id.editTextUsername).text.toString()
            val userEmail = findViewById<EditText>(R.id.editTextUserEmail).text.toString()
            val userPassword = findViewById<EditText>(R.id.editTextUserPassword).text.toString()
            val userPasswordConfirm = findViewById<EditText>(R.id.editTextUserPasswordConfirm).text.toString()

            if(userEmail.isNotEmpty() && userPassword.isNotEmpty() && userName.isNotEmpty() &&
                userPasswordConfirm.isNotEmpty()) {
                if(userPassword.length < 6 || userPassword.length > 12) {
                    Toast.makeText(this, "비밀번호는 6자리 이상 12자리 이하로 입력해 주세요.", Toast.LENGTH_SHORT).show()
                }

                if((userPassword == userPasswordConfirm) && userPassword.length < 13 && userPassword.length >= 6) {
                    doSignup(userEmail, userPassword, userName)
                    //updateProfile(userName)
                }
                else if(userPassword != userPasswordConfirm) {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                //updateProfile(userName)
//                val user = Firebase.auth.currentUser
//                if (user != null) {
//                    println("###########${user.displayName}")
//                }
            }
            else {
                Toast.makeText(this, "모든 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun doSignup(userEmail: String, password: String, userName: String) {
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공!!", Toast.LENGTH_SHORT).show()
//                    val itemMap = hashMapOf(
//                        "userName" to userName,
//                        "followerCount" to 0,
//                        "followingCount" to 0,
//                        "profileImage" to "gs://snsproject-638d2.appspot.com/images/profile_images/jeong1.jpeg"
//                    )
                    var userInfo = ContentDTO.UserInfo(
                        followerCount = 0,
                        followingCount = 0,
                        userName = userName,
                        profileImage = "https://firebasestorage.googleapis.com/v0/b/snsproject-638d2.appspot.com/o/images%2Fprofile_images%2Fjeong.png?alt=media&token=11f05012-c31e-46b5-8d17-b21677cf7c67",
                        followers = HashMap(),
                        following = HashMap(),
                        chat = HashMap()
                    )

                    FirebaseFirestore.getInstance().collection("user")
                        .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        .set(userInfo) // 입력받은 유저의 이름을 등록.
                        .addOnSuccessListener {
                            println("add user")
                        }
                    //updateProfile()
                    startActivity(
                        Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Creation failed.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                val errorMessage = it.message.toString()
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }

    }

    private fun updateProfile(userName: String) {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = userName
            //photoUri = Uri.parse("https://example.com/jane-q-user/profile.jpg")
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                }
            }
            .addOnFailureListener {
                println("##########user profile not updated.")
            }
    }
}