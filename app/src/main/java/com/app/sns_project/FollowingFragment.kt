package com.app.sns_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FollowingFragment : Fragment() {
    private val viewModel by viewModels<MyViewModel>()

    val db = Firebase.firestore

    // 현재 로그인한 user의 username
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
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // firestore에서 로그인한 user의 uid인 document에서 팔로잉 목록과 프로필 사진을 끌어와 viewmodel에 저장
        userColRef.document(currentUid).get()
            .addOnSuccessListener {
                for (i in it["following"] as MutableMap<*, *>)
                    viewModel.addItem2(Item(i.key.toString(),i.value.toString()))
            }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = RecyclerViewAdapter2(viewModel,context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.itemsListData2.observe(viewLifecycleOwner){
            adapter.notifyDataSetChanged()
        }

    }

}