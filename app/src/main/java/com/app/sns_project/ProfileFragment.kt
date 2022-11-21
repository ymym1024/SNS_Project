package com.app.sns_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.DTO.UserDTO
import com.app.sns_project.databinding.FragmentPostAddBinding
import com.app.sns_project.databinding.FragmentProfileBinding
import com.app.sns_project.model.ContentDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userName = ""

    var profileListener : ListenerRegistration?=null
    var followingListener : ListenerRegistration?=null
    var followerListener : ListenerRegistration?=null
    var postListener : ListenerRegistration?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getProfileInfo()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getPostImage()
    }

    private fun getPostImage(){
        val contentImageDTO : ArrayList<String> = ArrayList()
        val contentDTO : ArrayList<PostDTO> = ArrayList()
        postListener = firestore?.collection("post").whereEqualTo("uid",auth.currentUser?.uid!!).addSnapshotListener { value, error ->
            contentImageDTO.clear()

            if (value == null) return@addSnapshotListener
            for (v in value?.documents!!) {
                val data = v.toObject(PostDTO::class.java)!!
                contentDTO.add(data)
                if(data.imageUrl!!.size>1){
                    contentImageDTO.add(data.imageUrl?.get(0).toString()) // 무조건 첫번째 화면만
                }else{
                    contentImageDTO.add("")
                }
            }
            binding.userPostTextview.text = contentDTO.size.toString()
            binding.imageRecylcerview.adapter = ItemPagerAdapter(requireActivity(),contentImageDTO)
            binding.imageRecylcerview.layoutManager = GridLayoutManager(context,3)
        }
    }

    private fun getProfileInfo(){
        val uid = auth.currentUser!!.uid
        //profile image
        profileListener = firestore.collection("user").document(uid).addSnapshotListener { value, error ->
            if(value?.data!=null){
                val imageUrl = value?.data!!["profileImage"]
                val userName = value?.data!!["userName"].toString()
                Glide.with(requireContext()).load(imageUrl).into(binding.userImageImageView)
                binding.userProfileName.text = userName
            }
        }

        followingListener = firestore?.collection("user")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val userDTO = documentSnapshot?.toObject(UserDTO::class.java)
            if (userDTO == null) return@addSnapshotListener
            val followingCount = userDTO?.followingCount.toString()
            binding.userProfileFollowing.text = followingCount
        }

        followerListener = firestore?.collection("user")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val userDTO = documentSnapshot?.toObject(UserDTO::class.java)
            if (userDTO == null) return@addSnapshotListener
            val followerCount = userDTO?.followerCount.toString()
            binding.userProfileFollower.text = followerCount
        }
    }

    override fun onStop() {
        super.onStop()
        followerListener!!.remove()
        followingListener!!.remove()
        profileListener!!.remove()
        postListener!!.remove()
    }
}