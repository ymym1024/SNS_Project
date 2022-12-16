package com.app.sns_project.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.sns_project.data.model.Post
import com.app.sns_project.data.model.User
import com.app.sns_project.ui.activity.LoginActivity
import com.app.sns_project.adapter.PostAdapter
import com.app.sns_project.adapter.onListMoveInterface
import com.app.sns_project.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ProfileFragment : Fragment(),onListMoveInterface {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var postList : ArrayList<Post> = arrayListOf()
    private val postIdList : ArrayList<String> = ArrayList()
    private var userFollowingList : HashMap<String,String> = HashMap()

    private var uid = ""
    private var userName = ""
    private var imageUrl = ""

    var profileListener : ListenerRegistration?=null
    var followingListener : ListenerRegistration?=null
    var followerListener : ListenerRegistration?=null
    var postListener : ListenerRegistration?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val bundle = arguments

        if (bundle != null) {
            uid = bundle.getString("uid", "")
        }else{
            uid = auth.currentUser!!.uid
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        getProfileInfo()
        logOut()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileUpdateButton = binding.profileUpdateBtn
        profileUpdateButton.setOnClickListener {
            val uid = auth.currentUser?.uid.toString()
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToProfileUpdateFragment(uid))
        }
    }

    override fun onResume() {
        super.onResume()
        getPostImage()
    }

    private fun logOut() {
        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(activity, LoginActivity::class.java))
        }
    }

    private fun getPostImage(){
        postListener = firestore?.collection("post").whereEqualTo("uid",uid).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            postList.clear()

            if (value == null) return@addSnapshotListener
            for (v in value?.documents!!) {
                val data = v.toObject(Post::class.java)!!
                postList.add(data)
                postIdList.add(v.id)
            }
            binding.userPostTextview.text = postList.size.toString()
            binding.imageRecylcerview.adapter = PostAdapter(postList,postIdList,userFollowingList,this)
            binding.imageRecylcerview.layoutManager = LinearLayoutManager(context)
            binding.imageRecylcerview.setHasFixedSize(true)
        }
    }

    private fun getProfileInfo(){
        profileListener = firestore.collection("user").document(uid).addSnapshotListener { value, error ->
            userName = value?.data!!["userName"] as String
            imageUrl = value?.data!!["profileImage"] as String

            Glide.with(this).load(imageUrl).apply(RequestOptions().centerCrop()).into(binding.userImageImageView)
            binding.userProfileName.text = value?.data!!["userName"].toString()
        }

        followingListener = firestore?.collection("user")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val user = documentSnapshot?.toObject(User::class.java)
            if (user == null) return@addSnapshotListener
            val followingCount = user?.followingCount.toString()
            userFollowingList = user?.following
            binding.userProfileFollowing.text = followingCount
        }

        followerListener = firestore?.collection("user")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val user = documentSnapshot?.toObject(User::class.java)
            if (user == null) return@addSnapshotListener
            val followerCount = user?.followerCount.toString()
            binding.userProfileFollower.text = followerCount
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("profileFragment","onStop")
        if(followerListener!=null){
            followerListener!!.remove()
        }
        if(followingListener!=null){
            followingListener!!.remove()
        }
        if(profileListener!=null){
            profileListener!!.remove()
        }
        if(postListener!=null){
            postListener!!.remove()
        }
    }

    override fun movePostToProfile(position: Int) {
        return
    }

    override fun movePostToComment(position: Int) {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToDetailFragment(postIdList[position],postList[position].uid.toString()))
    }
}