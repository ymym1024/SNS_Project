package com.app.sns_project

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.databinding.FragmentMainBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var firestore: FirebaseFirestore

    private var postList : ArrayList<PostDTO> = arrayListOf()
    private lateinit var mAdapter : RecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("post").get().addOnSuccessListener { result ->
            postList.clear()
            for (item in result) {
                val post = item.toObject(PostDTO::class.java)
                postList.add(post)
            }
            setAdapter()
            mAdapter.notifyDataSetChanged()
        }

        return binding.root
    }

    private fun setAdapter(){
        mAdapter = RecyclerViewAdapter(postList)
        binding.postRecyclerview.adapter = mAdapter
        binding.postRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.postRecyclerview.setHasFixedSize(true)
    }

    inner class RecyclerViewAdapter(val itemList: ArrayList<PostDTO>) : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_post_item, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val postUser : TextView = itemView.findViewById(R.id.post_user)
            val userName : TextView = itemView.findViewById(R.id.user_name)
            val postContent : TextView = itemView.findViewById(R.id.post_content)
            val postTime : TextView = itemView.findViewById(R.id.post_time)
            val postImage : ImageView = itemView.findViewById(R.id.post_image)
            val postFavoriteCnt : TextView = itemView.findViewById(R.id.post_favorite_cnt)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.postUser.text = itemList[position].userId
            holder.userName.text = itemList[position].userId
            holder.postContent.text = itemList[position].content
           // holder.postTime.text = convertTimestampToDate(itemList[position].timestamp)
            if(itemList[position].imageUrl?.isNotEmpty() == true){
                Glide.with(context!!).load(itemList[position].imageUrl?.get(0)).into(holder.postImage) //첫번째 이미지만 불러오기(임시)
            }
            if(itemList[position].favoriteCount>0){
                holder.postFavoriteCnt.text = "${itemList[position].favoriteCount}명이 좋아합니다."
            }
        }

        override fun getItemCount(): Int {
            return postList.size
        }

        private fun convertTimestampToDate(time: Long?): String {
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val date = sdf.format(time).toString()
            return date
        }
    }
}