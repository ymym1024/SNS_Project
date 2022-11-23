package com.app.sns_project

import android.content.ContentValues.TAG
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.firestore.DocumentSnapshot

class CommentFragment : Fragment() { //R.layout.comment_fragment

    private lateinit var binding: CommentFragmentBinding
    var contentUid : String? = null
    //lateinit var v : View
    private lateinit var myAdapter : CommentFragment.CommentRecyclerviewAdapter
    var comments : ArrayList<ContentDTO.Comment> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
           binding = CommentFragmentBinding.inflate(inflater, container, false)
//        val args:CommentFragmentArgs by navArgs()
//        contentUid = args.contentId
//
//        //var v = inflater.inflate(R.layout.activity_comment, container, false)
//        //val commentRecyclerView = v.findViewById<RecyclerView>(R.id.commentRecyclerview)
//        val commentRecyclerView = binding.commentRecyclerview
//        commentRecyclerView.adapter  = CommentFragment().CommentRecyclerviewAdapter()
//        commentRecyclerView.layoutManager = LinearLayoutManager(activity)
//
////        val commentSendButton = v.findViewById<Button>(R.id.commentSendButton)
//        val commentSendButton = binding.commentSendButton
//        commentSendButton.setOnClickListener {
//            var comment = ContentDTO.Comment()
//            comment.userId = FirebaseAuth.getInstance().currentUser?.email
//            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
//            comment.userName = FirebaseAuth.getInstance().currentUser?.displayName // 유저 이름
////            val commentEditText = v.findViewById<EditText>(R.id.commentEditText)
//            val commentEditText = binding.commentEditText
//            comment.comment = commentEditText.text.toString()
//            comment.timestamp = System.currentTimeMillis()
//
//            FirebaseFirestore.getInstance().collection("posts").document(contentUid!!)
//                .collection("comments").document().set(comment)
//            commentEditText.setText("")
//        }
//
//        //val userName = v.findViewById<TextView>(R.id.commentViewUserName) // 게시글을 올린 유저의 이름
//        val userName = binding.commentViewUserName
//        var postUserId: String? = null // 게시글을 올린 유저의 uid
////        var userContent = v.findViewById<TextView>(R.id.my_text) // 게시글 content
//        var userContent = binding.myText
//        FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
//            .get().addOnSuccessListener { document ->
//                if (document != null) {
//                    userName.text = document.get("userName").toString() // 유저의 이름을 기입
//                    postUserId = document.get("uid").toString()
//                    userContent.text = document.get("content").toString() // 게시물의 content 기입
//                    //Log.d(TAG, "DocumentSnapshot data: ${document.data}")
//                } else {
//                    Log.d(TAG, "No such document")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d(TAG, "get failed with ", exception)
//            }
//
////        val userProfile = v.findViewById<ImageView>(R.id.postViewProfile) // 게시글을 올린 유저의 프로필
//        val userProfile = binding.postViewProfile
//        FirebaseFirestore.getInstance().collection("user").document(postUserId!!).get()
//            .addOnSuccessListener { document ->
//                if(document != null) {
//                    var url = document.get("profileImage")
//                    Glide.with(this).load(url).apply(RequestOptions().circleCrop())
//                        .into(userProfile)
//                }
//            }
//        return if(this::binding.isInitialized){
//            binding.root
//        } else{
//            null
//        }
        Log.d("onCreateView", "zzz")
        val args:CommentFragmentArgs by navArgs()
        contentUid = args.contentId
        Log.d("contentUid in onCreateView", contentUid!!)

        val commentRecyclerView = binding.commentRecyclerview
        myAdapter = CommentFragment().CommentRecyclerviewAdapter(comments, contentUid)
        commentRecyclerView.adapter = myAdapter
        //commentRecyclerView.adapter  = CommentFragment().CommentRecyclerviewAdapter()
        commentRecyclerView.layoutManager = LinearLayoutManager(context) // activity?
        commentRecyclerView.setHasFixedSize(true)

        return binding.root
        //return v
    }

    override fun onResume() {
        super.onResume()
        commentLoading()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val args:CommentFragmentArgs by navArgs()
//        contentUid = args.contentId
        Log.d("contentUid in onViewCreated", contentUid!!)

        //var v = inflater.inflate(R.layout.activity_comment, container, false)
        //val commentRecyclerView = v.findViewById<RecyclerView>(R.id.commentRecyclerview)
//        val commentRecyclerView = binding.commentRecyclerview
//        myAdapter = CommentFragment().CommentRecyclerviewAdapter()
//        commentRecyclerView.adapter = myAdapter
//                //commentRecyclerView.adapter  = CommentFragment().CommentRecyclerviewAdapter()
//        commentRecyclerView.layoutManager = LinearLayoutManager(context) // activity?
        Log.d("onViewCreated", this::binding.toString())
//        val commentSendButton = v.findViewById<Button>(R.id.commentSendButton)
        val commentSendButton = binding.commentSendButton
        var name : String? = null
        commentSendButton.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            //comment.userName = FirebaseAuth.getInstance().currentUser?.displayName // 유저 이름
            FirebaseFirestore.getInstance().collection("user")
                .document(comment.uid!!)
                .get().addOnSuccessListener { document ->
                    if (document != null) {
                        comment.userName = document.get("userName").toString() // ??null??
                        Log.d("userName", comment.userName!!)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                    FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
                        .collection("comments").document().set(comment)
                }
//            val commentEditText = v.findViewById<EditText>(R.id.commentEditText)
            val commentEditText = binding.commentEditText
            comment.comment = commentEditText.text.toString()
            comment.timestamp = System.currentTimeMillis()

//            FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
//                .collection("comments").document().set(comment)
            commentEditText.setText("")
        }

        //val userName = v.findViewById<TextView>(R.id.commentViewUserName) // 게시글을 올린 유저의 이름
        val userName = binding.commentViewUserName
        var postUserId: String? = null // 게시글을 올린 유저의 uid
        //FirebaseFirestore.getInstance().collection("post").document(contentUid!!)
//        var userContent = v.findViewById<TextView>(R.id.my_text) // 게시글 content
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
//                    if (url != null) {
//                        println(url.javaClass.name)
//                    }
//                    println(url[0])
                    Glide.with(this).load(url[0]).apply(RequestOptions())
                        .into(userImageContent)
                    //Log.d(TAG, "DocumentSnapshot data: ${document.data}")
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
       // Log.d("userName", userName.text)
//        val userProfile = v.findViewById<ImageView>(R.id.postViewProfile) // 게시글을 올린 유저의 프로필
        println("kkkkkkkkkk")
//        val userProfile = binding.postViewProfile // ok

//        postUserId = "m2Jby2V5X9bhhNJrcI97BaulRH32" // *******
//        Log.d("postUserId after Firebase", postUserId!!)
//        FirebaseFirestore.getInstance().collection("user").document(postUserId!!).get()
//            .addOnSuccessListener { document ->
//                if(document != null) {
//                    var url = document.get("profileImage")
//                    Glide.with(this).load(url).apply(RequestOptions().circleCrop())
//                        .into(userProfile)
//                }
//            }
    }

//    private fun CommentAlarm(postUseruid:String){
//        FirebaseFirestore.getInstance().collection("user").document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
//            val userName = it["userName"] as String
//
//            Log.d("userName",userName)
//            var message = String.format("%s 님이 좋아요를 눌렀습니다.",userName)
//            pushMessage()?.sendMessage(postUseruid, "알림 메세지 입니다.", message)
//        }
//    }

    private fun commentLoading(){
        //setAdapter()

        //comments = arrayListOf()
        println("in init $contentUid")
        //contentUid = "98gEZ3ziBphbvDw80KAh" /// *******
        Log.d("contentUid in init", contentUid!!)
        Log.d("in inner class init", "ok")
        FirebaseFirestore.getInstance()
            .collection("post")
            .document(contentUid!!)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { querySnapShot, FirebaseFirestoreException ->
                comments.clear()
                if(querySnapShot == null)
                    return@addSnapshotListener
                for(snapshot in querySnapShot.documents!!) {
                    comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    Log.d("inquerySnapShot", comments.size.toString())
                }
                myAdapter.notifyDataSetChanged() // 리싸이클러 뷰 새로고침
            }
    }

    private fun setAdapter(){
        val commentRecyclerView = binding.commentRecyclerview
        myAdapter = CommentFragment().CommentRecyclerviewAdapter(comments, contentUid)
        commentRecyclerView.adapter = myAdapter
        //commentRecyclerView.adapter  = CommentFragment().CommentRecyclerviewAdapter()
        commentRecyclerView.layoutManager = LinearLayoutManager(context) // activity?
        commentRecyclerView.setHasFixedSize(true);
    }

    inner class CommentRecyclerviewAdapter(var comments: ArrayList<ContentDTO.Comment>, var contentUid : String?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//        private val deleteButton: ImageButton = v.findViewById<ImageButton>(R.id.deleteButton)
        //private val deleteButton: ImageButton = binding.deleteCommentButton
        //var comments : ArrayList<ContentDTO.Comment> = arrayListOf() // comment 를 담을 ArrayList
//        init {
////            val args:CommentFragmentArgs by navArgs()
////            contentUid = args.contentId
//            println("in init $contentUid")
//            contentUid = "98gEZ3ziBphbvDw80KAh" /// *******
//            Log.d("contentUid in init", contentUid!!)
//            Log.d("in inner class init", "ok")
//            FirebaseFirestore.getInstance()
//                .collection("post")
//                .document(contentUid!!)
//                .collection("comments")
//                .orderBy("timestamp")
//                .addSnapshotListener { querySnapShot, FirebaseFirestoreException ->
//                    if(querySnapShot == null)
//                        return@addSnapshotListener
//                    for(snapshot in querySnapShot.documents!!) {
//                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
//                    }
//                    notifyDataSetChanged() // 리싸이클러 뷰 새로고침
//                }
//        }
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
            val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)
            deleteButton.setOnClickListener {
//                comments.removeAt(position)
//                notifyItemRemoved(position)
//                notifyItemRangeChanged(position, comments.size)
                //notifyDataSetChanged()
                FirebaseFirestore.getInstance()
                    .collection("post")
                    .document(contentUid!!)
                    .collection("comments")
                    .orderBy("timestamp").addSnapshotListener { value, error ->
                        if(value == null) return@addSnapshotListener
                        val comm = value.documents[position]
                        Log.d("doc Id",comm.id)
//                        FirebaseFirestore.getInstance()
//                            .collection("post")
//                            .document(contentUid!!)
//                            .collection("comments")
//                            .document(comm.id).get().addOnSuccessListener {
//                                var a = it.get("uid")
//                                Log.d("uid", a as String)
//                            }
//                        FirebaseFirestore.getInstance()
//                            .collection("post")
//                            .document(contentUid!!)
//                            .collection("comments")
//                            .document(comm.id)
//                            .delete()
//                            .addOnCompleteListener {
//                                if(it.isSuccessful){
//                                    Log.d("comment delete", "success")
//                                }
//                            }
                    }

            }
        }

        override fun getItemCount(): Int {
            Log.d("getItemCount", comments.size.toString())
            return comments.size
        }

    }
}