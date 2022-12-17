package com.app.sns_project.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.sns_project.*
import com.app.sns_project.adapter.PostAdapter
import com.app.sns_project.adapter.onListMoveInterface
import com.app.sns_project.data.FirebaseDbService
import com.app.sns_project.data.model.Post
import com.app.sns_project.databinding.FragmentMainBinding
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query


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
        if(postSnapshot!=null){
            postSnapshot!!.remove()
        }
    }
    private fun dataRefresh(){
        dbService.store.collection("user").document(uid).get().addOnSuccessListener { task ->
            val list = task?.data?.get("following")
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
                    mAdapter.setFollowList(userFollowingList)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setAdapter(){
        mAdapter = PostAdapter(postList,postIdList,this)
        binding.postRecyclerview.adapter = mAdapter
        binding.postRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.postRecyclerview.setHasFixedSize(true)
    }

    override fun movePostToProfile(position: Int) {
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToUserProfileFragment(postList[position].uid!!,postList[position].userName!!))

    }

    override fun movePostToComment(position: Int) {
        val postId = postIdList[position]
        val postUid = postList[position].uid.toString()

        findNavController().navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(postId,postUid))
    }

}