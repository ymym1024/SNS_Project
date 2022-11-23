package com.app.sns_project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.*
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class SearchFragment() : Fragment() {
    private val viewModel by viewModels<MyViewModel>()

    val db = Firebase.firestore

    // 현재 로그인한 user의 uid
    val currentUid = Firebase.auth.currentUser?.uid.toString()

    // user Collection Ref
    val userColRef = db.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchResultTextView = view.findViewById<TextView>(R.id.searchResultTextView)
        val usernameTextView = view.findViewById<TextView>(R.id.userNametextView)
        val profileImage = view.findViewById<CircleImageView>(R.id.circleImageView)
        val isEmptyTextView = view.findViewById<TextView>(R.id.isEmptyTextView)
        val followButton = view.findViewById<ImageButton>(R.id.followButton2)
        val followingButton = view.findViewById<ImageButton>(R.id.followingButton)

        // bundle 이용
       // val searchName = arguments?.getString("searchName")
        val args:SearchFragmentArgs by navArgs()
        val searchName = args.searchName
        searchResultTextView.text = searchName

        if (searchName == "" || searchName == null){
            profileImage.visibility = View.INVISIBLE
            isEmptyTextView.text = "검색어가 입력되지 않았습니다"
        }
        else{
//            Log.e("searchName",searchName)
            userColRef.whereEqualTo("userName",searchName).get()
                .addOnSuccessListener {
                    if(it.isEmpty){
                        profileImage.visibility = View.INVISIBLE
                        isEmptyTextView.text = "존재하지 않는 사용자입니다"
                    }
                    for(doc in it){
                        // 프로필사진 세팅
                        Glide.with(view).load(doc["profileImage"].toString()).into(profileImage)
                        profileImage.visibility = View.VISIBLE

                        // username 세팅
                        usernameTextView.text = doc["userName"].toString()

                        // button 세팅
                        userColRef.document(currentUid).get()
                            .addOnSuccessListener {
                                val followingList = it["following"] as MutableMap<String,String> // 현재 로그인한 user의 팔로잉 리스트
                                if(followingList.containsKey(doc["userName"].toString())) {// 팔로잉중이면
                                    followButton.visibility = View.INVISIBLE
                                    followingButton.visibility = View.VISIBLE
                                }
                                else {
                                    followingButton.visibility = View.INVISIBLE
                                    followButton.visibility = View.VISIBLE
                                }

                                userColRef.document(currentUid).get()
                                    .addOnSuccessListener {
                                        if (it["userName"].toString() == searchName){
                                            followButton.visibility = View.INVISIBLE
                                            followingButton.visibility = View.INVISIBLE
                                        }
                                    }
                            }

                    }
                }
        }


        followButton.setOnClickListener {
            userColRef.whereEqualTo("userName",searchName).get()
                .addOnSuccessListener {
                    for (doc in it){
                        followUser(doc["userName"].toString(),doc["profileImage"].toString())
                        followButton.visibility = View.INVISIBLE
                        followingButton.visibility = View.VISIBLE
                    }
                }
        }

    }

    private fun followUser(username : String, profileImageUrl : String){
        userColRef.document(currentUid).get()
            .addOnSuccessListener {
                val followingList = it["following"] as MutableMap<String, String> // 현재 로그인한 user의 팔로잉 목록에
                followingList.put(username, profileImageUrl) // 해당 유저 추가

                userColRef.document(currentUid)
                    .update("following", followingList) // firestore 팔로잉 목록 update
                userColRef.document(currentUid)
                    .update("followingCount", followingList.size) // firestore 팔로잉 수 update



                val currentUsername = it["userName"].toString() // 현재 로그인한 user의 username 받아오기
                val currentUserProfileImage = it["profileImage"].toString() // 현재 로그인한 user의 profile image 받아오기

                userColRef.whereEqualTo("userName",username).get()
                    .addOnSuccessListener {
                        for(doc in it){
                            val followerList = doc["followers"] as MutableMap<String, String> // 팔로우당한 user의 팔로워 목록에
                            followerList.put(currentUsername,currentUserProfileImage) // 현재 로그인한 user 추가

                            userColRef.document(doc.id)
                                .update("followers",followerList) // firestore 팔로워 목록 update
                            userColRef.document(doc.id)
                                .update("followerCount",followerList.size) // firestore 팔로워 수 update
                        }
                    }
            }
    }
}