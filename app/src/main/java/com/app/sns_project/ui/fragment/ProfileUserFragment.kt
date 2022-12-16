package com.app.sns_project.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.sns_project.R
import com.app.sns_project.adapter.PostImageViewPager
import com.app.sns_project.data.model.Post
import com.app.sns_project.data.model.User
import com.app.sns_project.databinding.FragmentProfileUserBinding
import com.app.sns_project.util.pushMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.text.SimpleDateFormat


class ProfileUserFragment : Fragment() {
    private lateinit var binding: FragmentProfileUserBinding

    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val postIdList : ArrayList<String> = ArrayList()

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
        binding = FragmentProfileUserBinding.inflate(inflater, container, false)

        getProfileInfo()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chattingButton = binding.userChattingBtn
        chattingButton.setOnClickListener {
            findNavController().navigate(ProfileUserFragmentDirections.actionUserProfileFragmentToChatRoomFragment(binding.userProfileName.text.toString()))

        }
    }

    override fun onResume() {
        super.onResume()
        getPostImage()
    }

    private fun getPostImage(){
        val contentDTO : ArrayList<Post> = ArrayList()

        postListener = firestore?.collection("post").whereEqualTo("uid",uid).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            contentDTO.clear()

            if (value == null) return@addSnapshotListener
            for (v in value?.documents!!) {
                val data = v.toObject(Post::class.java)!!
                contentDTO.add(data)
                postIdList.add(v.id)
            }
            binding.userPostTextview.text = contentDTO.size.toString()
            binding.imageRecylcerview.adapter = RecyclerViewAdapter(contentDTO)
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
            binding.userProfileFollowing.text = followingCount
        }

        followerListener = firestore?.collection("user")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val user = documentSnapshot?.toObject(User::class.java)
            if (user == null) return@addSnapshotListener
            val followerCount = user?.followerCount.toString()
            binding.userProfileFollower.text = followerCount
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("profileFragment","onDetach")
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

    inner class RecyclerViewAdapter(var itemList: ArrayList<Post>) : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_post_item, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val postUser : TextView = itemView.findViewById(R.id.post_user)
            val userName : TextView = itemView.findViewById(R.id.user_name)
            val userImage : ImageView = itemView.findViewById(R.id.user_image_imageView)
            val postContent : TextView = itemView.findViewById(R.id.post_content)
            val postTime : TextView = itemView.findViewById(R.id.post_time)
            val postImageList : ViewPager2 = itemView.findViewById(R.id.post_image)
            val postIndicator : DotsIndicator = itemView.findViewById(R.id.post_image_indicator)
            val postFavoriteCnt : TextView = itemView.findViewById(R.id.post_favorite_cnt)
            val postFavorite : ImageView = itemView.findViewById(R.id.post_favorite)
            val postComment : ImageView = itemView.findViewById(R.id.comment_button)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.postUser.text = itemList[position].userName
            holder.userName.text = itemList[position].userName
            holder.postContent.text = itemList[position].content
            holder.postTime.text = convertTimestampToDate(itemList[position].timestamp!!)

            //user 이미지 -> 내계정조회
            Glide.with(holder.userImage.context).load(imageUrl).apply(RequestOptions().centerCrop()).into(holder.userImage)

            if(itemList[position].imageUrl?.isEmpty()!!){
                holder.postImageList.visibility = View.GONE
                holder.postIndicator.visibility = View.GONE
            }else{
                holder.postImageList.adapter = PostImageViewPager(itemList[position]?.imageUrl)

                if(itemList[position].imageUrl?.size == 1){
                    holder.postIndicator.visibility = View.GONE
                }else{
                    holder.postIndicator.setViewPager2(holder.postImageList)
                }
            }

            if(itemList[position].favoriteCount>0){
                holder.postFavoriteCnt.text = "좋아요 ${itemList[position].favoriteCount}개"
            }else{
                holder.postFavoriteCnt.text = ""
            }

            //좋아요 버튼 상태값 변경
            if(itemList[position].favorites.containsKey(uid)){
                holder.postFavorite.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
            }else{
                holder.postFavorite.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
            }

            //좋아요 버튼 클릭 이벤트
            holder.postFavorite.setOnClickListener {
                val doc = firestore?.collection("post").document(postIdList[position])

                firestore?.runTransaction { transaction ->
                    val post = transaction.get(doc).toObject(Post::class.java)

                    if (post!!.favorites.containsKey(uid)) {
                        post.favoriteCount = post?.favoriteCount - 1
                        post.favorites.remove(uid) // 사용자 remove
                    } else {
                        post.favoriteCount = post?.favoriteCount + 1
                        post.favorites[uid] = true //사용자 추가
                        alarmFavorite(post.uid!!)
                    }
                    transaction.set(doc, post)
                }
            }

            //사용자 프로필 화면으로 이동

            //댓글 상세화면으로 이동
            holder.postComment.setOnClickListener {
                findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToDetailFragment(postIdList[position],itemList[position].uid.toString()))
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

        private fun alarmFavorite(postUseruid:String){
            firestore.collection("user").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
                val userName = it["userName"] as String

                Log.d("userName",userName)
                if(!postUseruid.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
                    var message = String.format("%s 님이 좋아요를 눌렀습니다.",userName)
                    pushMessage()?.sendMessage(postUseruid, "알림 메세지 입니다.", message)
                }
            }
        }
    }
}