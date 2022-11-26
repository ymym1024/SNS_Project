package com.app.sns_project

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.model.ContentDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.app.sns_project.databinding.CommentFragmentBinding
import com.app.sns_project.util.pushMessage
import com.google.android.material.snackbar.Snackbar

class CommentFragment : Fragment() { //R.layout.comment_fragment

    private lateinit var binding: CommentFragmentBinding
    private var contentUid : String? = null
    private lateinit var myAdapter : CommentFragment.CommentRecyclerviewAdapter
    var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
    var commentDoc : ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
           binding = CommentFragmentBinding.inflate(inflater, container, false)

        Log.d("onCreateView", "zzz")
        val args:CommentFragmentArgs by navArgs()
        contentUid = args.contentId
        Log.d("contentUid in onCreateView", contentUid!!)

        return binding.root
        //return v
    }

    override fun onResume() {
        super.onResume()
        commentLoading()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("contentUid in onViewCreated", contentUid!!)
        Log.d("onViewCreated", this::binding.toString())
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
                            Log.d(TAG, "No such document")
                        }
                        FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
                            .collection("comments").document().set(comment)
                        myAdapter.notifyDataSetChanged()
                    }
            }

            commentEditText.setText("")
            FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
                .get().addOnSuccessListener {
                    Log.d("PostUserName", it.get("userName").toString())
                    commentAlarm(it.get("userName").toString())
                }
            commentLoading()
            //myAdapter.notifyDataSetChanged()
        }

        val userName = binding.commentViewUserName
        var postUserId: String? = null // 게시글을 올린 유저의 uid
        var userContent = binding.myText
        var userImageContent = binding.myImg
        FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
            .get().addOnSuccessListener { document ->
                if (document != null) {
                    userName.text = document.get("userName").toString() // 유저의 이름을 기입
                    postUserId = document.get("uid").toString()
                    Log.d("uid = ", postUserId!!)
                    userContent.text = document.get("content").toString() // 게시물의 content 기입
                    var url : ArrayList<String> = document.get("imageUrl") as ArrayList<String>
                    try{
                        Glide.with(this).load(url[0]).apply(RequestOptions())
                            .into(userImageContent)
                    }catch (e : Exception){
                        Log.d("ddd", "ddd")
                        userImageContent.visibility=View.GONE
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
                val userProfile = binding.postViewProfile // ok
                FirebaseFirestore.getInstance().collection("user").document(postUserId!!).get()
                    .addOnSuccessListener { document ->
                        if(document != null) {
                            var url = document.get("profileImage")
                            Glide.with(this).load(url).apply(RequestOptions().circleCrop())
                                .into(userProfile)
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

//    private fun logOut() {
//        val uid : String? = null
//        var userId = FirebaseAuth.getInstance().currentUser?.uid
//        val logoutButton = binding.logoutButton
//        if(uid == userId) {
//            logoutButton.setOnclickListener {
//                startActivity(Intent(activity, LoginActivity))
//            }
//        }
//        else {
//            logoutButton.visibility = View.INVISIBLE
//        }
//    }

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
        myAdapter = CommentFragment().CommentRecyclerviewAdapter(comments, contentUid, commentDoc)
        commentRecyclerView.adapter = myAdapter
        commentRecyclerView.layoutManager = LinearLayoutManager(context) // activity?
        commentRecyclerView.setHasFixedSize(true);
    }

    private fun deleteComment(commentId : String, contentUid: String?) {
        FirebaseFirestore.getInstance()
            .collection("post")
            .document(contentUid!!)
            .collection("comments")
            .document(commentId)
            .delete()
            .addOnSuccessListener {
                //notifyItemRangeChanged(position, comments.size-1)
                Log.d("delete", "complete")
            }
            .addOnFailureListener {
                Log.d("error", it.toString())
            }
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