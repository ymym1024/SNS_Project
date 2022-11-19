package com.app.sns_project

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.model.ContentDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentFragment : Fragment(R.layout.activity_comment) {
    var contentUid : String? = null
    var a = null;
    lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.activity_comment, container, false)
        val commentRecyclerView = v.findViewById<RecyclerView>(R.id.commentRecyclerview)
        commentRecyclerView.adapter  = CommentFragment().CommentRecyclerviewAdapter()
        commentRecyclerView.layoutManager = LinearLayoutManager(activity)

        val commentSendButton = v.findViewById<Button>(R.id.commentSendButton)
        commentSendButton.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.userName = FirebaseAuth.getInstance().currentUser?.displayName // 유저 이름
            val commentEditText = v.findViewById<EditText>(R.id.commentEditText)
            comment.comment = commentEditText.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("posts").document(contentUid!!)
                .collection("comments").document().set(comment)
            commentEditText.setText("")
        }

        val userName = v.findViewById<TextView>(R.id.commentViewUserName) // 게시글을 올린 유저의 이름
        var postUserId: String? = null // 게시글을 올린 유저의 uid
        var userContent = v.findViewById<TextView>(R.id.my_text) // 게시글 content
        FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
            .get().addOnSuccessListener { document ->
                if (document != null) {
                    userName.text = document.get("userName").toString() // 유저의 이름을 기입
                    postUserId = document.get("uid").toString()
                    userContent.text = document.get("content").toString() // 게시물의 content 기입
                    //Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        val userProfile = v.findViewById<ImageView>(R.id.postViewProfile) // 게시글을 올린 유저의 프로필
        FirebaseFirestore.getInstance().collection("user").document(postUserId!!).get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    var url = document.get("profileImage")
                    Glide.with(this).load(url).apply(RequestOptions().circleCrop())
                        .into(userProfile)
                }
            }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentUid = null // contentUid 값을 가져온다.
    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val deleteButton: ImageButton = v.findViewById<ImageButton>(R.id.deleteButton)

        var comments : ArrayList<ContentDTO.Comment> = arrayListOf() // comment 를 담을 ArrayList
        init {
            FirebaseFirestore.getInstance()
                .collection("post")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapShot, FirebaseFirestoreException ->
                    if(querySnapShot == null)
                        return@addSnapshotListener
                    for(snapshot in querySnapShot.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged() // 리싸이클러 뷰 새로고침
                }
        }
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

            FirebaseFirestore.getInstance().collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop())
                            .into(view.findViewById(R.id.commentViewProfile))
                    }
                }
            deleteButton.setOnClickListener {
                comments.remove(comments[position])
                println("comment deleted")
            }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
}