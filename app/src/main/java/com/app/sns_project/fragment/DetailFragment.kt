package com.app.sns_project.fragment

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.CommentFragment
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.R
import com.app.sns_project.databinding.FragmentDetailBinding
import com.app.sns_project.model.ContentDTO
import com.app.sns_project.util.pushMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var contentUid : String? = null
    private lateinit var myAdapter : CommentRecyclerviewAdapter
    var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
    var commentDoc : ArrayList<String> = arrayListOf()

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

        contentUid = postId

        getProfile(postId,postUser)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        commentLoading()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val commentSendButton = binding.commentSendButton
        var name : String? = null
        commentSendButton.setOnClickListener {
            //commentLoading()
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            val commentEditText = binding.commentEditText
            comment.comment = commentEditText.text.toString()
            comment.timestamp = System.currentTimeMillis()
            Log.d("comment => ", comment.comment!!)
            if(comment.comment == null || comment.comment == "") {
                Snackbar.make(binding.root, "댓글을 입력해 주세요", Snackbar.LENGTH_SHORT).show()
            }
            else if(comment.comment!!.length > 100) {
                Snackbar.make(binding.root, "100자 이하의 댓글을 입력해 주세요", Snackbar.LENGTH_SHORT).show()
            }
            else {
                FirebaseFirestore.getInstance().collection("user")
                    .document(comment.uid!!)
                    .get().addOnSuccessListener { document ->
                        if (document != null) {
                            comment.userName = document.get("userName").toString() // ??null??
                            Log.d("userName", comment.userName!!)
                            myAdapter.notifyDataSetChanged()
                        } else {
                            Log.d(ContentValues.TAG, "No such document")
                        }
                        FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
                            .collection("comments").document().set(comment)
                        myAdapter.notifyDataSetChanged()
                    }
            }

            commentEditText.setText("")
            FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
                .get().addOnSuccessListener {
                    Log.d("PostUserName", it.get("uid").toString())
                    commentAlarm(it.get("uid").toString())
                }
            commentLoading()
            //myAdapter.notifyDataSetChanged()
        }
    }

    private fun commentAlarm(postUseruid:String){
        FirebaseFirestore.getInstance().collection("user").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
            val userName = it["userName"] as String

            Log.d("userName",userName)
            var message = String.format("%s 님이 댓글을 남겼습니다.",userName)
            pushMessage()?.sendMessage(postUseruid, "알림 메세지 입니다.", message)
        }
    }

    private fun commentLoading(){
        setAdapter()
        myAdapter.notifyDataSetChanged()
        println("in init $contentUid")
        Log.d("contentUid in init", contentUid!!)
        Log.d("in inner class init", "ok")
        FirebaseFirestore.getInstance()
            .collection("post")
            .document(contentUid!!)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { querySnapShot, FirebaseFirestoreException ->
                commentDoc.clear()
                comments.clear()
                if(querySnapShot == null)
                    return@addSnapshotListener
                for(snapshot in querySnapShot.documents!!) {
                    comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    commentDoc.add(snapshot.id) // comment document id를 타임 순서대로 모음
                    Log.d("commentDoc Id size", commentDoc.size.toString())
                    Log.d("inquerySnapShot", comments.size.toString())
                }
                myAdapter.notifyDataSetChanged() // 리싸이클러 뷰 새로고침
            }
    }

    private fun setAdapter(){
        val commentRecyclerView = binding.commentRecyclerview
        myAdapter = CommentRecyclerviewAdapter(comments, contentUid, commentDoc)
        commentRecyclerView.adapter = myAdapter
        commentRecyclerView.layoutManager = LinearLayoutManager(context) // activity?
        commentRecyclerView.setHasFixedSize(true);
    }

    private fun getProfile(postId : String,postUser:String){
        firestore.collection("user").document(postUser).get().addOnSuccessListener {
            val image = it.data!!["profileImage"]
            Glide.with(binding.userImageImageView.context).load(image).into(binding.userImageImageView)
            Log.d("postId",postId)

            firestore?.collection("post").document(postId).get().addOnSuccessListener{ value ->
                if (value == null) return@addOnSuccessListener
                val data = value?.data!!

                profile.userName = data["userName"] as String
                profile.content = data["content"] as String
                profile.timestamp = data["timestamp"] as Long
                profile.imageUrl = data["imageUrl"] as ArrayList<String>
                profile.favoriteCount = Integer.parseInt(data["favoriteCount"].toString())
                profile.uid = data["uid"] as String
                profile.favorites = data["favorites"] as HashMap<String, Boolean>

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

    inner class CommentRecyclerviewAdapter(
        var comments: ArrayList<ContentDTO.Comment>, var contentUid: String?,
        var commentDoc: ArrayList<String>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.findViewById<TextView>(R.id.commentViewComment).text = comments[position].comment
            view.findViewById<TextView>(R.id.commentViewUserID).text = comments[position].userName // userId로 해도됨
            FirebaseFirestore.getInstance().collection("user")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        var url = task.result!!["profileImage"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop())
                            .into(view.findViewById(R.id.commentViewProfile))
                    }
                }
            Log.d("----------", "onBindViewHolder")
            val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)
            Log.d("commentDocId.size", commentDoc.size.toString())
            var commUid = commentDoc[position]
            FirebaseFirestore.getInstance().collection("post")
                .document(contentUid!!)
                .collection("comments")
                .document(commUid)
                .get()
                .addOnSuccessListener {
                    var cUid = it.get("uid")
                    Log.d("cUid", cUid.toString())
                    if(cUid != FirebaseAuth.getInstance().currentUser?.uid){
                        deleteButton.visibility = View.GONE
                    }
                    else if(cUid == FirebaseAuth.getInstance().currentUser?.uid){
                        deleteButton.visibility = View.VISIBLE
                    }
                }


            deleteButton.setOnClickListener {
//                comments.removeAt(position)
//                notifyItemRemoved(position)
//                notifyItemRangeChanged(position, comments.size)
                val commentDocId = commentDoc[position]
                Log.d("commentDocId -> ", commentDocId)
                FirebaseFirestore.getInstance().collection("post")
                    .document(contentUid!!)
                    .collection("comments")
                    .document(commentDocId)
                    .delete()
                    .addOnSuccessListener {
//                        notifyDataSetChanged()
                        Log.d("delete", "completed")
                    }
                notifyItemRemoved(position)
                //notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            //Log.d("getItemCount", comments.size.toString())
            return comments.size
        }

    }
}