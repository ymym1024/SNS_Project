package com.app.sns_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/*
일단 이 ChatFragment는 채팅방만 보여주는 걸로 하고
채팅을 시작하려면 해당 유저 프로필에서 채팅 버튼을 눌러서 채팅을 시작하는 걸로
채팅 버튼을 누르면 -> 해당 유저와의 채팅방으로 바로 연결이 되어야 하기 때문에
누르는 즉시 chat필드에 해당 유저의 username으로 된 빈 map이 생성되는 것으로 하고(메세지가 없는 빈 채팅방이 생성될 수 있는 것)
※ 이 순간은 채팅을 건 쪽에는 채팅방이 존재하지만 상대는 아직 채팅방이 생성되지 않은 상태, 첫 메세지를 보내게 되면 그때 상대방쪽에 생성되어야함
그 버튼 누르는 fragment랑 ChatRoomFragment navigation 연결해야할듯?
그 빈 채팅방에 들어가면 일단 '대화 내용이 없습니다' 같은 문구가 떠야 하고
첫 메세지를 보내면 채팅시작
*/



class ChatFragment : Fragment() {
    private val viewModel by viewModels<MyViewModel>()

    val db = Firebase.firestore

    // 현재 로그인한 user의 uid
//    val currentUid = Firebase.auth.currentUser?.uid.toString()
    val currentUid = "uid1"
    // user Collection Ref
    val userColRef = db.collection("test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // firestore에서 로그인한 user의 uid인 document에서 채팅방이 있는 user의 username과 프로필 사진을 끌어와 viewmodel에 저장
        userColRef.document(currentUid).get()
            .addOnSuccessListener {
                viewModel.deleteChatTitleItem()
                val chatMap = it["chat"] as MutableMap<String, *>
                if (chatMap.isNotEmpty()) {
                    for ((key, value) in chatMap) {
                        userColRef.whereEqualTo("userName", key.toString()).get()
                            .addOnSuccessListener {
                                for (doc in it) {
                                    val tempSpecificChat = value as MutableMap<String, *>
                                    if(tempSpecificChat.isNotEmpty()) { // 채팅이 하나라도 있으면
                                        val specificChat = tempSpecificChat.toSortedMap() // 시간순으로 정렬
                                        var tempMap = mutableMapOf<String, String>()
                                        for ((key2, value2) in specificChat) {
                                            tempMap = value2 as MutableMap<String, String> // 마지막 채팅 표시하기 위함
                                        }
                                        val keys = mutableListOf<String>()
                                        for ((key3, value3) in tempMap) {
                                            keys.add(key3) // map을 인덱스로 접근하기 위함
                                        }
                                        viewModel.addChatTitleItem(
                                            chatTitleItem(
                                                key,
                                                doc["profileImage"].toString(),
                                                tempMap.get(keys[0]) ?: ""
                                            )
                                        )
                                    }
                                    else{ // 빈 채팅방이면
                                        viewModel.addChatTitleItem(
                                            chatTitleItem(
                                                key,
                                                doc["profileImage"].toString(),
                                                "             "
                                            )
                                        )
                                    }
                                }
                            }
                    }
                }
            }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = ChatRecyclerViewAdapter(viewModel, context,this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.chatTitleItemListData.observe(viewLifecycleOwner){
            adapter.notifyDataSetChanged()
        }

    }
}