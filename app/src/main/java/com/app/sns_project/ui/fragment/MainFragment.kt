package com.app.sns_project.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.arrayMapOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.sns_project.*
import com.app.sns_project.adapter.PagerFragmentStateAdapter
import com.app.sns_project.adapter.PostAdapter
import com.app.sns_project.adapter.RecyclerViewAdapter
import com.app.sns_project.adapter.onListMoveInterface
import com.app.sns_project.data.FirebaseDbService
import com.app.sns_project.data.model.Post
import com.app.sns_project.data.model.Item
import com.app.sns_project.databinding.FragmentMainBinding
import com.app.sns_project.ui.fragment.FollowingFragment
import com.app.sns_project.util.pushMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat


class MainFragment : Fragment(),onListMoveInterface{
    private lateinit var binding: FragmentMainBinding
    private var dbService: FirebaseDbService = FirebaseDbService()

    private var postList : ArrayList<Post> = arrayListOf()
    private var postIdList : ArrayList<String> = arrayListOf()
    private var userFollowingList : HashMap<String,String> = HashMap()

    private lateinit var mAdapter : PostAdapter

    //스냅샷
    var postSnapshot : ListenerRegistration?=null
    var uid = dbService.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        //adapter 셋팅
        setAdapter()
        dataRefresh()

        //사용자 정보 저장
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        if(dbService.postSnapshot!=null){
            dbService.postSnapshot!!.remove()
        }
    }
    private fun dataRefresh(){
        dbService.store.collection("user").document(uid).get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val list = task.result["following"]
                if(list!=null){
                    userFollowingList = list as HashMap<String,String>
                    Log.d("userFollowList",userFollowingList.toString())
                    postSnapshot = dbService.store.collection("post").orderBy("timestamp", Query.Direction.DESCENDING)?.addSnapshotListener { value, error ->
                        postList.clear()
                        postIdList.clear()
                        if(value == null) return@addSnapshotListener
                        for(post in value!!.documents) {
                            var item = post.toObject(Post::class.java)!!
                            if (userFollowingList.keys?.contains(item.userName)!!) {
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
        mAdapter = PostAdapter(postList,postIdList,userFollowingList,this)
        binding.postRecyclerview.adapter = mAdapter
        binding.postRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.postRecyclerview.setHasFixedSize(true)
    }

    override fun movePostToProfile(position: Int) {
        val bundle = Bundle()
        bundle.putString("uid", postList[position].uid)
        bundle.putString("userName",postList[position].userName)
        findNavController().navigate(R.id.action_mainFragment_to_userProfileFragment,bundle)

    }

    override fun movePostToComment(position: Int) {
        val postId = postIdList[position]
        val postUid = postList[position].uid.toString()

        findNavController().navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(postId,postUid))
    }

}