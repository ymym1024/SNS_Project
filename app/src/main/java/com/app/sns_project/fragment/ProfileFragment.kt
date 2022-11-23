package com.app.sns_project.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.DTO.UserDTO
import com.app.sns_project.ItemPagerAdapter
import com.app.sns_project.R
import com.app.sns_project.databinding.FragmentProfileBinding
import com.app.sns_project.util.pushMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.text.SimpleDateFormat

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val postIdList : ArrayList<String> = ArrayList()

    private var uid = ""
    private var imageUrl = ""

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
        uid = auth.currentUser!!.uid

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
            //binding.imageRecylcerview.adapter = GridImageRecyclerViewAdatper(requireActivity(),contentDTO)
            binding.imageRecylcerview.adapter = RecyclerViewAdapter(contentDTO)
            binding.imageRecylcerview.layoutManager = LinearLayoutManager(context)
            binding.imageRecylcerview.setHasFixedSize(true)
        }
    }

    private fun getProfileInfo(){
        val uid = auth.currentUser!!.uid
        //profile image
        profileListener = firestore.collection("user").document(uid).addSnapshotListener { value, error ->
            if(value?.data!=null){
                imageUrl = value?.data!!["profileImage"] as String
                val userName = value?.data!!["userName"].toString()
                Glide.with(requireContext()).load(imageUrl).apply(RequestOptions().centerCrop()).into(binding.userImageImageView)
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

    inner class RecyclerViewAdapter(var itemList: ArrayList<PostDTO>) : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

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
            val postMenu : Button = itemView.findViewById(R.id.post_menu)
            val postComment : ImageView = itemView.findViewById(R.id.comment_button)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.postUser.text = itemList[position].userName
            holder.userName.text = itemList[position].userName
            holder.postContent.text = itemList[position].content
            holder.postTime.text = convertTimestampToDate(itemList[position].timestamp!!)

            //user 이미지 -> 내계정조회
            Glide.with(holder.userImage.context).load(imageUrl).into(holder.userImage)

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
                holder.postFavoriteCnt.text = "좋아요 ${itemList[position].favoriteCount}개"
            }else{
                holder.postFavoriteCnt.text = ""
            }
            if(!itemList[position].uid.equals(auth.currentUser?.uid)) {
                holder.postMenu.visibility = View.INVISIBLE
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
                    val post = transaction.get(doc).toObject(PostDTO::class.java)

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
                var message = String.format("%s 님이 좋아요를 눌렀습니다.",userName)
                pushMessage()?.sendMessage(postUseruid, "알림 메세지 입니다.", message)
            }
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