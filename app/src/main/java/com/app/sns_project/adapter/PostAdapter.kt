package com.app.sns_project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.R
import com.app.sns_project.data.model.Post
import com.app.sns_project.databinding.LayoutPostItemBinding
import com.app.sns_project.util.CommonService
import com.app.sns_project.data.FirebaseDbService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.util.HashMap

class PostAdapter(var postList:ArrayList<Post>, var postIdList :ArrayList<String>,val followList : HashMap<String,String>, listener: onListMoveInterface) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>(){

    private var dbService : FirebaseDbService = FirebaseDbService()
    private var onListMoveInterface : onListMoveInterface = listener

    inner class PostViewHolder(private val binding:LayoutPostItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(postItem:Post,position: Int){
            val postId = postIdList[position]

            binding.postUser.text = postItem.userName
            binding.userName.text = postItem.userName
            binding.postContent.text = postItem.content
            binding.postTime.text = CommonService().convertTimestampToDate(postItem.timestamp)

            val userProfileImage = followList[postItem.userName]

            //user 이미지 불러오기
            Glide.with(binding.userImageImageView.context).load(userProfileImage).apply(
                RequestOptions().centerCrop()).into(binding.userImageImageView)

            binding.postFavoriteCnt.text = if(postItem.favoriteCount > 0) "좋아요 ${postItem.favoriteCount}개" else ""

            if(postItem.imageUrl?.isEmpty()!!){
                binding.postImage.visibility = View.GONE
                binding.postImageIndicator.visibility = View.GONE
            }else{
                binding.postImage.adapter = PostImageViewPager(postItem?.imageUrl)

                if(postItem?.imageUrl?.size == 1){
                    binding.postImageIndicator.visibility = View.GONE
                }else{
                    binding.postImageIndicator.setViewPager2(binding.postImage)
                }
            }

            //좋아요 버튼 상태값 변경
            if(postItem.favorites.containsKey(dbService.uid)){
                binding.postFavorite.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
            }else{
                binding.postFavorite.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
            }

            //좋아요 버튼 클릭
            binding.postFavorite.setOnClickListener {
                dbService.transactionFavorite(postId)
            }

            //프로필 페이지 이동
            binding.userImageImageView.setOnClickListener {
                onListMoveInterface.movePostToProfile(position)
            }

            //댓글 상세화면 이동
            binding.commentButton.setOnClickListener {
                onListMoveInterface.movePostToComment(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = LayoutPostItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        Log.d("userFollowList",followList.toString())
        holder.bind(postList[position],position)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}

interface onListMoveInterface{
    fun movePostToProfile(position:Int)
    fun movePostToComment(position:Int)
}