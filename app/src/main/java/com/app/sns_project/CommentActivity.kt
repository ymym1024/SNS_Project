package com.app.sns_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.app.sns_project.model.ContentDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class CommentActivity : AppCompatActivity() {
    var contentUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        // 프레그먼트에서 게시물 말풍선을 클릭 하게 되면 setOnclickListener 를 통해서 CommentActivity가 띄워 지도록 코딩.
        // Intent 를 통해서 CommentActivity::class.java로 넘어갈 수 있도록.
        // Intent 안에다가 contentUid 도 넘겨 준다. contentListUidList[position] 값.
        // startActivity(intent)
        contentUid = intent.getStringExtra("contentUid")

        // collection(게시물) -> document -> collection(댓글) -> document -> 필드(comment, timestamp, uid, userId)

        val commentRecyclerView = findViewById<RecyclerView>(R.id.commentRecyclerview)
        commentRecyclerView.adapter = CommentRecyclerviewAdapter()
        commentRecyclerView.layoutManager = LinearLayoutManager(this)

        val commentSendButton = findViewById<Button>(R.id.commentSendButton)
        commentSendButton.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            val commentEditText = findViewById<EditText>(R.id.commentEditText)
            comment.comment = commentEditText.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("posts").document(contentUid!!)
                .collection("comments").document().set(comment)
            commentEditText.setText("")
        }

        //val deleteButton = findViewById<ImageButton>(R.id.deleteButton)
    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val deleteButton = findViewById<ImageButton>(R.id.deleteButton)

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

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var view = holder.itemView
            view.findViewById<TextView>(R.id.commentViewComment).text = comments[position].comment
            view.findViewById<TextView>(R.id.commentViewUserID).text = comments[position].userId

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