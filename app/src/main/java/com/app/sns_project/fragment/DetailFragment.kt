package com.app.sns_project.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.R
import com.app.sns_project.databinding.FragmentDetailBinding
import com.app.sns_project.util.pushMessage
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    var postListener : ListenerRegistration?=null
    var profile = PostDTO()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val args:DetailFragmentArgs by navArgs()
        val postId = args.postId
        val postUser = args.postUser

        getProfile(postId,postUser)

        return binding.root
    }

    private fun getProfile(postId : String,postUser:String){
        firestore.collection("user").document(postUser).get().addOnSuccessListener {
            val image = it.data!!["profileImage"]
            Glide.with(binding.userImageImageView.context).load(image).into(binding.userImageImageView)

            postListener = firestore?.collection("post").document(postId).addSnapshotListener { value, error ->
                if (value == null) return@addSnapshotListener
                val data = value?.data!!

                profile.userName =  data["userName"] as String
                profile.content = data["content"] as String
                profile.timestamp = data["timestamp"] as Long
                profile.imageUrl = data["imageUrl"] as ArrayList<String>
                profile.favoriteCount = Integer.parseInt(data["favoriteCount"].toString())
                profile.uid = data["uid"] as String
                profile.favorites = data["favorites"] as HashMap<String,Boolean>

                initView(postId)

            }
        }
    }

    fun initView(postId : String){
        binding.userName.text = profile.userName
        binding.postContent.text = profile.content
        binding.postUser.text = profile.userName
        binding.postTime.text = convertTimestampToDate(profile.timestamp)

        if(profile.imageUrl?.isEmpty()!!){
            binding.postImage.visibility = View.GONE
            binding.postImageIndicator.visibility = View.GONE
        }else{
            binding.postImage.adapter = ImageViewPager2(profile.imageUrl)

            if(profile.imageUrl!!.size == 1){
                binding.postImageIndicator.visibility = View.GONE
            }else{
                binding.postImageIndicator.setViewPager2(binding.postImage)
            }
        }

        val favoriteCount = profile.favoriteCount
        if(favoriteCount>0){
            binding.postFavoriteCnt.text = "${favoriteCount}명이 좋아합니다."
        }else{
            binding.postFavoriteCnt.text = ""
        }
        if(!profile.uid.equals(auth.currentUser?.uid)) {
            binding.postMenu.visibility = View.INVISIBLE
        }

        binding.postMenu.setOnClickListener {
            //수정, 삭제 보여주기
            //BottomSheetFragment(postId).show(parentFragmentManager,"PostMenu")
            findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToBottomSheetFragment(postId))
        }

        //좋아요 버튼 상태값 변경
        if(profile.favorites.containsKey(auth.currentUser!!.uid)){
            binding.postFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
        }else{
            binding.postFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }

        //좋아요 버튼 클릭 이벤트
        binding.postFavorite.setOnClickListener {
            val doc = firestore?.collection("post").document(postId)

            firestore?.runTransaction { transaction ->
                val post = transaction.get(doc).toObject(PostDTO::class.java)

                if (post!!.favorites.containsKey(auth.currentUser!!.uid)) {
                    post.favoriteCount = post?.favoriteCount - 1
                    post.favorites.remove(auth.currentUser!!.uid) // 사용자 remove
                } else {
                    post.favoriteCount = post?.favoriteCount + 1
                    post.favorites[auth.currentUser!!.uid] = true //사용자 추가
                    alarmFavorite(post.uid!!)
                }
                transaction.set(doc, post)
            }
        }
    }

    private fun alarmFavorite(postUseruid:String){
        firestore.collection("user").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
            val userName = it["userName"] as String

            Log.d("userName",userName)
            var message = String.format("%s 님이 좋아요를 눌렀습니다.",userName)
            pushMessage()?.sendMessage(postUseruid, "알림 메세지 입니다.", message)
        }
    }

    fun convertTimestampToDate(time: Long?): String {
        Log.d("time", time.toString())
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val date = sdf.format(time).toString()
        return date
    }

    override fun onStop() {
        super.onStop()
        if(postListener!=null){
            postListener?.remove()
        }

    }
}