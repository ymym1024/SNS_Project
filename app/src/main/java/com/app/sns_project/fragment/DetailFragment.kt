package com.app.sns_project.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.app.sns_project.BottomSheetFragment
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.R
import com.app.sns_project.databinding.FragmentDetailBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    var postListener : ListenerRegistration?=null

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
            postListener = firestore?.collection("post").document(postId).addSnapshotListener { value, error ->
                if (value == null) return@addSnapshotListener
                val data = value?.data!!

                binding.userName.text = data["userName"] as String
                binding.postContent.text = data["content"] as String
                binding.postUser.text = data["userName"] as String
                binding.postTime.text = convertTimestampToDate(data["timestamp"] as Long)
                //user 이미지
                Glide.with(binding.userImageImageView.context).load(image).into(binding.userImageImageView)

                val imageArray = data["imageUrl"] as ArrayList<String>

                if(imageArray?.isEmpty()!!){
                    binding.postImage.visibility = View.GONE
                    binding.postImageIndicator.visibility = View.GONE
                }else{
                    binding.postImage.adapter = ImageViewPager2(imageArray)

                    if(imageArray.size == 1){
                        binding.postImageIndicator.visibility = View.GONE
                    }else{
                        binding.postImageIndicator.setViewPager2(binding.postImage)
                    }
                }

                val favoriteCount = Integer.parseInt(data["favoriteCount"].toString())
                if(favoriteCount>0){
                    binding.postFavoriteCnt.text = "${favoriteCount}명이 좋아합니다."
                }else{
                    binding.postFavoriteCnt.text = ""
                }
                if(!data["uid"]!!.equals(auth.currentUser?.uid)) {
                    binding.postMenu.visibility = View.INVISIBLE
                }

                binding.postMenu.setOnClickListener {
                    //수정, 삭제 보여주기
                    BottomSheetFragment(postId).show(parentFragmentManager,"PostMenu")
                }

                //좋아요 버튼 상태값 변경
                val favorites = data["favorites"] as HashMap<String,Boolean>
                if(favorites.containsKey(auth.currentUser!!.uid)){
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
                        }
                        transaction.set(doc, post)
                    }
                }
            }
        }

    }
    fun convertTimestampToDate(time: Long?): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val date = sdf.format(time).toString()
        return date
    }

    override fun onStop() {
        super.onStop()
        postListener?.remove()
    }
}