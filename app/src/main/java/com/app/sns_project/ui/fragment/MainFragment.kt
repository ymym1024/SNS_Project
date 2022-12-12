package com.app.sns_project.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.sns_project.*
import com.app.sns_project.adapter.PagerFragmentStateAdapter
import com.app.sns_project.adapter.RecyclerViewAdapter
import com.app.sns_project.data.model.PostDTO
import com.app.sns_project.data.model.ContentDTO
import com.app.sns_project.databinding.CommentFragmentBinding
import com.app.sns_project.databinding.FragmentMainBinding
import com.app.sns_project.databinding.FragmentProfileUpdateBinding
import com.app.sns_project.ui.fragment.FollowingFragment
import com.app.sns_project.util.pushMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import java.io.File
import java.text.SimpleDateFormat


class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var uid = ""

    private var postList : ArrayList<PostDTO> = arrayListOf()
    private var postIdList : ArrayList<String> = arrayListOf()
    private var userFollowingList = HashMap<String,String>()

    private lateinit var mAdapter : RecyclerViewAdapter

    var postSnapshot  : ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!

        //사용자 정보 저장
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dataRefresh()
    }

    override fun onStop() {
        super.onStop()
        if(postSnapshot!=null){
            postSnapshot!!.remove()
        }
    }
    private fun dataRefresh(){
        setAdapter()

        firestore.collection("user").document(uid).get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val list = task.result["following"]
                if(list!=null){
                    userFollowingList = list as HashMap<String,String>
                    postSnapshot = firestore.collection("post").orderBy("timestamp", Query.Direction.DESCENDING)?.addSnapshotListener { value, error ->
                        postList.clear()
                        postIdList.clear()
                        if(value == null) return@addSnapshotListener
                        for(post in value!!.documents){
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
            val userImage : ImageView = itemView.findViewById(R.id.user_image_imageView)
            val postContent : TextView = itemView.findViewById(R.id.post_content)
            val postTime : TextView = itemView.findViewById(R.id.post_time)
            val postImageList : ViewPager2 = itemView.findViewById(R.id.post_image)
            val postIndicator :DotsIndicator = itemView.findViewById(R.id.post_image_indicator)
            val postFavoriteCnt : TextView = itemView.findViewById(R.id.post_favorite_cnt)
            val postFavorite : ImageView = itemView.findViewById(R.id.post_favorite)
            val postComment : ImageView = itemView.findViewById(R.id.comment_button)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.postUser.text = itemList[position].userName
            holder.userName.text = itemList[position].userName
            holder.postContent.text = itemList[position].content
            holder.postTime.text = convertTimestampToDate(itemList[position].timestamp!!)

            //user 이미지
            Glide.with(holder.userImage.context).load(userFollowingList.get(itemList[position].userName)).apply(RequestOptions().centerCrop()).into(holder.userImage)

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

            //댓글 상세화면으로 이동
            holder.postComment.setOnClickListener {
               findNavController().navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(postIdList[position],itemList[position].uid.toString()))
            }

            holder.userImage.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("uid", postList[position].uid)
                bundle.putString("userName",postList[position].userName)
                findNavController().navigate(R.id.action_mainFragment_to_profileFragment,bundle)
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
class ImageViewPager2(private var list: List<String>?): RecyclerView.Adapter<ImageViewPager2.ViewHolder>(){

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val Image : ImageView = view.findViewById(R.id.pagerImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(
        R.layout.layout_pager_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.Image.context).load(list?.get(position)!!).into(holder.Image)
    }

    override fun getItemCount(): Int = list!!.size
}

class ProfileUpdateFragment : Fragment() {
    private lateinit var binding: FragmentProfileUpdateBinding

    private lateinit var uid :String
    private lateinit var userName : String

    private var imageUri = ""

    companion object {
        const val REQ_UPDATE_GALLERY = 2
    }

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            result.data?.data?.let {
                val imagePath = getRealPathFromURI(it)
                Glide.with(this).load(imagePath).apply(RequestOptions().circleCrop()).into(binding.userImageImageView)

                imageUri = imagePath
            }
        }else{
            return@registerForActivityResult
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileUpdateBinding.inflate(inflater, container, false)
        uid = FirebaseAuth.getInstance().currentUser!!.uid

        updateData()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initData(uid)
        getImage()
    }

    private fun initData(uid : String){
        FirebaseFirestore.getInstance().collection("user").document(uid).get().addOnSuccessListener {
            val name = it["userName"] as String
            val profile = it["profileImage"] as String

            //뷰 갱신
            userName = name // 이름 초기화
            binding.nameEdittext.setText(name)
            Glide.with(this).load(profile).apply(RequestOptions().circleCrop()).into(binding.userImageImageView)

            binding.emailEdittext.setText(FirebaseAuth.getInstance().currentUser!!.email)
        }
    }

    private fun getImage(){
        binding.profileUpdateTextview.setOnClickListener {
            val readPermission = ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writePermission = ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if(readPermission == PackageManager.PERMISSION_DENIED || writePermission == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQ_UPDATE_GALLERY
                )
            }else{
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
                imageResult.launch(intent)
            }
        }
    }

    private fun updateData(){
        binding.saveButton.setOnClickListener {
            var userName = binding.nameEdittext.text.toString()

            if(!imageUri.isEmpty()){
                var imageFileName = "profile_images/"+userName+".png"
                val fileName = File(imageUri).toUri()

                var imageRef = FirebaseStorage.getInstance().reference.child("images").child(imageFileName)

                imageRef.putFile(fileName).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri->
                        val imageUri = uri.toString()

                        FirebaseFirestore.getInstance().collection("user").document(uid)
                            .update("profileImage",imageUri)
                            .addOnSuccessListener {
                                Snackbar.make(binding.root, "프로필이 수정되었습니다", Snackbar.LENGTH_SHORT).show()

                            }.addOnFailureListener {
                                Snackbar.make(
                                    binding.root,
                                    "등록이 실패했습니다. 네트워크를 확인해주세요",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }else{
                Snackbar.make(binding.root, "수정을 원하시지 않으면 뒤로가기를 눌러주세요", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun getRealPathFromURI(uri : Uri):String{
        val buildName = Build.MANUFACTURER

        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity?.contentResolver?.query(uri, proj, null, null, null)
        if(cursor!!.moveToFirst()){
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        val result = cursor.getString(columnIndex)
        cursor.close()
        return result
    }
}

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
       // val args:CommentFragmentArgs by navArgs()
      //  contentUid = args.contentId
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
                        userImageContent.visibility= View.GONE
                    }
                } else {
                    Log.d(ContentValues.TAG, "No such document")
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
                Log.d(ContentValues.TAG, "get failed with ", exception)
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

            Log.d("userName", userName)
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
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
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

class FollowerFragment : Fragment() {
    private val viewModel by viewModels<MyViewModel>()

    val db = Firebase.firestore

    // 현재 로그인한 user의 uid
    val currentUid = Firebase.auth.currentUser?.uid.toString()

    // user Collection Ref
    val userColRef = db.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_follower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // firestore에서 로그인한 user의 uid인 document에서 팔로워 목록과 프로필 사진을 끌어와 viewmodel에 저장
        userColRef.document(currentUid).get()
            .addOnSuccessListener {
                for (i in it["followers"] as MutableMap<*, *>)
                    viewModel.addItem(Item(i.key.toString(), i.value.toString()))
            }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = RecyclerViewAdapter(viewModel, context, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.itemsListData.observe(viewLifecycleOwner){
            adapter.notifyDataSetChanged()
        }

    }

}

class FollowFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val tabTitleArray = arrayOf(
        "팔로워",
        "팔로잉"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_follow, container, false)
        viewPager = view.findViewById(R.id.pager)
        tabLayout = view.findViewById(R.id.tab_layout)
        return view
//        return inflater.inflate(R.layout.fragment_follower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())
        // 2개의 fragment add
        pagerAdapter.addFragment(FollowerFragment())
        pagerAdapter.addFragment(FollowingFragment())

        // adapter 연결
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0 -> {
                        pagerAdapter.refreshFragment(position, FollowerFragment())
                    }
                    1 -> {
                        pagerAdapter.refreshFragment(position, FollowingFragment())
                    }
                }
                Log.e("ViewPagerFragment", "Page ${position + 1}")
            }
        })

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitleArray[position]
        }.attach()

        val searchbutton = view.findViewById<ImageButton>(R.id.searchButton)
        searchbutton.setOnClickListener {
            println("search button clicked")
            val editTextSearchResult = view.findViewById<EditText>(R.id.editTextSearchPeople).text.toString()
//            Log.e("name",editTextSearchResult)
            if(editTextSearchResult=="" || editTextSearchResult == null){
                val layoutInflater = LayoutInflater.from(context)
                val view = layoutInflater.inflate(R.layout.custom_dialog_search,null)

                val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                    .setView(view)
                    .create()

                val confirmButton = view.findViewById<ImageButton>(R.id.confirmButton)

                confirmButton.setOnClickListener {
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }
            else{
                // bundle 이용
//                val bundle = Bundle()
//                bundle.putString("searchName",editTextSearchResult)
//                val searchFragment = SearchFragment()
//                searchFragment.arguments = bundle
////                Log.e("arguements check:",searchFragment.arguments?.getString("searchName").toString())
//
//                parentFragmentManager.commit {
//                    setReorderingAllowed(true)
//                    replace(R.id.nav_host_fragment, searchFragment, null)
//                    addToBackStack(null)
//                }
                val searchName = editTextSearchResult
                findNavController().navigate(FollowFragmentDirections.actionFollowFragmentToSearchFragment(searchName))
            }

        }
    }
}