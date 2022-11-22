package com.app.sns_project.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.DTO.UserDTO
import com.app.sns_project.ItemPagerAdapter
import com.app.sns_project.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val postIdList : ArrayList<String> = ArrayList()

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
        val contentDTO : ArrayList<PostDTO> = ArrayList()

        postListener = firestore?.collection("post").whereEqualTo("uid",auth.currentUser?.uid!!).addSnapshotListener { value, error ->
            contentDTO.clear()

            if (value == null) return@addSnapshotListener
            for (v in value?.documents!!) {
                val data = v.toObject(PostDTO::class.java)!!
                contentDTO.add(data)
                postIdList.add(v.id)
            }
            binding.userPostTextview.text = contentDTO.size.toString()
            binding.imageRecylcerview.adapter = GridImageRecyclerViewAdatper(requireActivity(),contentDTO)
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


    inner class GridImageRecyclerViewAdatper(val context: Context,val content:ArrayList<PostDTO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            //현재 사이즈 뷰 화면 크기의 가로 크기의 1/3값을 가지고 오기
            val width = resources.displayMetrics.widthPixels / 3

            val imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)

            return CustomViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var imageView = (holder as CustomViewHolder).imageView
            var image :String

            if(content[position].imageUrl!!.isEmpty()){
                image = ""
            }else{
                image = content[position].imageUrl!!.get(0)
            }
            Glide.with(holder.itemView.context)
                .load(image)
                .apply(RequestOptions().centerCrop())
                .into(imageView)

            imageView.setOnClickListener {

                findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToDetailFragment(postIdList[position],content[position].uid.toString()))
            }
        }

        override fun getItemCount(): Int {
            return content.size
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    }
}