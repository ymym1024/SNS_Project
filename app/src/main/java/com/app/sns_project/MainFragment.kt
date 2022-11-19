package com.app.sns_project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.databinding.FragmentMainBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.text.SimpleDateFormat


class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var uid = ""
    private var userName = ""

    private var postList : ArrayList<PostDTO> = arrayListOf()
    private var postIdList : ArrayList<String> = arrayListOf()
    private var userFollowingList = HashMap<String,String>()

    private lateinit var mAdapter : RecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!

        userName = "jeong" //TODO :: 합쳤을때 수정해야함 -> 전역변수로 username을 저장
        Log.d("user:",FirebaseAuth.getInstance().currentUser?.email!!)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dataRefresh()
    }

    private fun dataRefresh(){
        setAdapter()

        firestore.collection("user").document(userName).get().addOnSuccessListener {
            userFollowingList = it["following"] as HashMap<String,String>
            firestore.collection("post").orderBy("timestamp")?.addSnapshotListener { value, error ->
                postList.clear()
                postIdList.clear()
                if(value == null) return@addSnapshotListener
                for(post in value!!.documents){
                    Log.d("item",post.toString())
                    var item = post.toObject(PostDTO::class.java)!!
                    if(userFollowingList.keys?.contains(item.userName)!!) {
                        postList.add(item)
                        postIdList.add(post.id)
                    }
                }
                mAdapter.notifyDataSetChanged()
            }

        }

    }

    private fun setAdapter(){
        mAdapter = RecyclerViewAdapter(postList)
        binding.postRecyclerview.adapter = mAdapter
        binding.postRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.postRecyclerview.setHasFixedSize(true)
    }

    inner class RecyclerViewAdapter(var itemList: ArrayList<PostDTO>) : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_post_item, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val postUser : TextView = itemView.findViewById(R.id.post_user)
            val userName : TextView = itemView.findViewById(R.id.user_name)
            val userImage : ImageView = itemView.findViewById(R.id.user_image)
            val postContent : TextView = itemView.findViewById(R.id.post_content)
            val postTime : TextView = itemView.findViewById(R.id.post_time)
            val postImageList : ViewPager2 = itemView.findViewById(R.id.post_image)
            val postIndicator :DotsIndicator = itemView.findViewById(R.id.post_image_indicator)
            val postFavoriteCnt : TextView = itemView.findViewById(R.id.post_favorite_cnt)
            val postFavorite : ImageView = itemView.findViewById(R.id.post_favorite)
            val postMenu : Button = itemView.findViewById(R.id.post_menu)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.postUser.text = itemList[position].userName
            holder.userName.text = itemList[position].userName
            holder.postContent.text = itemList[position].content
            holder.postTime.text = convertTimestampToDate(itemList[position].timestamp!!)

            //user 이미지
            Glide.with(holder.userImage.context).load(userFollowingList.get(itemList[position].userName)).into(holder.userImage)

            if(itemList[position].imageUrl?.isEmpty()!!){
                holder.postImageList.visibility = View.GONE
                holder.postIndicator.visibility = View.GONE
            }else{
                holder.postImageList.adapter = ImageViewPager2(itemList[position]?.imageUrl)

                if(itemList[position].imageUrl?.size == 1){
                    holder.postIndicator.visibility = View.GONE
                }else{
                    holder.postIndicator.setViewPager2(holder.postImageList)
                }
            }

            if(itemList[position].favoriteCount>0){
                holder.postFavoriteCnt.text = "${itemList[position].favoriteCount}명이 좋아합니다."
            }else{
                holder.postFavoriteCnt.text = ""
            }
            if(!holder.postUser.text.equals(auth.currentUser?.email)) {
                holder.postMenu.visibility = View.INVISIBLE
            }

            holder.postMenu.setOnClickListener {
                //수정, 삭제 보여주기
                BottomSheetFragment(postIdList[position]).show(parentFragmentManager,"PostMenu")
            }

            //좋아요 버튼 상태값 변경
            if(itemList[position].favorites.containsKey(uid)){
                holder.postFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
            }else{
                holder.postFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }

            //좋아요 버튼 클릭 이벤트
            holder.postFavorite.setOnClickListener {
                val doc = firestore?.collection("post").document(postIdList[position])

                firestore?.runTransaction { transaction ->
                    val post = transaction.get(doc).toObject(PostDTO::class.java)

                    if (post!!.favorites.containsKey(uid)) {
                        post.favoriteCount = post?.favoriteCount - 1
                        post.favorites.remove(uid) // 사용자 remove
                    } else {
                        post.favoriteCount = post?.favoriteCount + 1
                        post.favorites[uid] = true //사용자 추가
                    }
                    transaction.set(doc, post)
                }
            }
        }
        override fun getItemCount(): Int {
            return itemList.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        private fun convertTimestampToDate(time: Long?): String {
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val date = sdf.format(time).toString()
            return date
        }
    }


    inner class ImageViewPager2(private var list: List<String>?): RecyclerView.Adapter<ImageViewPager2.ViewHolder>(){

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val Image : ImageView = view.findViewById(R.id.pagerImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_pager_item, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.Image.context).load(list?.get(position)!!).into(holder.Image)
        }

        override fun getItemCount(): Int = list!!.size
    }
}

