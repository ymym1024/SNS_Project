package com.app.sns_project

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.System.currentTimeMillis
import java.util.*
import kotlin.collections.LinkedHashMap

class ChatRoomFragment : Fragment() {
    private val viewModel by viewModels<MyViewModel>()

    val db = Firebase.firestore

    // 현재 로그인한 user의 uid
    val currentUid = Firebase.auth.currentUser?.uid.toString()
//    val currentUid = "uid1"
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
        return inflater.inflate(R.layout.fragment_chatroom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args:ChatRoomFragmentArgs by navArgs()
        val chatUserName = args.chatUserName

        val sendbutton = view.findViewById<ImageButton>(R.id.sendButton)
        val editTextSendMessage = view.findViewById<EditText>(R.id.editTextSendMessage)

        // 채팅방 받아오기
        readChatRoom(chatUserName)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = ChatRoomRecyclerViewAdapter(viewModel, context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.scrollToPosition(adapter.itemCount-1)

        viewModel.chatItemsListData.observe(viewLifecycleOwner){
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(adapter.itemCount-1)
        }


        // 채팅 보내기
        sendbutton.setOnClickListener {
            userColRef.document(currentUid).get()
                .addOnSuccessListener {
                    // 메세지 아무것도 입력하지 않고 send버튼 눌렀을때 alertDialog 처리
                    if(editTextSendMessage.text == null || editTextSendMessage.text.toString() == ""){
                        val layoutInflater = LayoutInflater.from(context)
                        val view = layoutInflater.inflate(R.layout.custom_dialog_message,null)

                        val alertDialog = AlertDialog.Builder(context,R.style.CustomAlertDialog)
                            .setView(view)
                            .create()

                        val confirmButton = view.findViewById<ImageButton>(R.id.confirmButton)

                        confirmButton.setOnClickListener {
                            alertDialog.dismiss()
                        }
                        alertDialog.show()
                    }
                    else {
                        // 메세지 보내는 부분
                        val chatMap = it["chat"] as MutableMap<String,Map<*,*>> // chat 필드 전체
                        if(!chatMap.containsKey(chatUserName)) { // 첫 채팅이면
                            val emptyMap = mutableMapOf<String,Map<*,*>>()
                            chatMap.put(chatUserName,emptyMap)
                        }
                        val specificChat = chatMap.get(chatUserName) as MutableMap<String,Map<*,*>> // 특정 상대와의 채팅내용 // 맵의 키가 Long일 수 없어서 String으로 넣음
                        val tempMap = mutableMapOf<String,String>(it["userName"].toString() to editTextSendMessage.text.toString()) // 내가 메세지 보내는거
                        specificChat.put(currentTimeMillis().toString(),tempMap)
                        // <issue> <해결됨> 현재시간이 제대로 저장되지 않는 버그 있음
                        Log.e("current time:",currentTimeMillis().toString())
                        chatMap.put(chatUserName,specificChat)

                        userColRef.document(currentUid)
                            .update("chat", chatMap) // 내 firestore 'chat' 필드 update

                        val currentUsername = it["userName"].toString() // 현재 로그인한 user의 username 받아오기

                        // 상대 firestore도 update
                        userColRef.whereEqualTo("userName",chatUserName).get()
                            .addOnSuccessListener {
                                for(doc in it){
                                    val yourChatMap = doc["chat"] as MutableMap<String,Map<*,*>> // 상대 채팅 전체
                                    if(!chatMap.containsKey(currentUsername)){ // 첫 채팅이면
                                        val emptyMap = mutableMapOf<String,Map<*,*>>()
                                        yourChatMap.put(currentUsername,emptyMap)
                                    }
                                    yourChatMap.put(currentUsername,specificChat)

                                    userColRef.document(doc.id)
                                        .update("chat",yourChatMap)

                                    editTextSendMessage.text = null

                                }
                            }
                    }
                }
        }

        // firestore db가 변할시 recyclerView 업데이트
        userColRef.document(currentUid).addSnapshotListener{snapshot, error ->
//            Log.d("change","${snapshot?.id} ${snapshot?.data}")
            val chatRoomFragment = ChatRoomFragment()
            if(snapshot?.data?.contains("chat") == true) { // 로직 불완전함
                readChatRoom(chatUserName)
                recyclerView.scrollToPosition(adapter.itemCount-1)
            }
        }

    }

    fun readChatRoom(chatUserName: String) {
//        val args: ChatRoomFragmentArgs by navArgs()
//        val chatUserName = args.chatUserName
        userColRef.document(currentUid).get()
            .addOnSuccessListener {
                viewModel.deleteAllChatItem()
                // 채팅방 출력하는 부분
                val chatMap = it["chat"] as MutableMap<String, Map<*,*>>
                if (chatMap.isNotEmpty() && chatMap.containsKey(chatUserName)) {
                    val tempSpecificChat = chatMap.get(chatUserName) as MutableMap<String, *>
                    val specificChat = tempSpecificChat.toSortedMap()
                    if (specificChat.isNotEmpty()) { // 채팅이 하나라도 있으면
                        for ((key, value) in specificChat) {
                            val tempMap = value as MutableMap<String, String>
                            for ((key2, value2) in tempMap) {
                                viewModel.addChatItem(
                                    chatItem(
                                        key.toString().toLong(),
                                        key2,
                                        value2
                                    )
                                )
                            }
                        }
                    }
                }
                else{
                    val emptyMap = mutableMapOf<String,Map<*,*>>()
                    chatMap.put(chatUserName,emptyMap)

                    userColRef.document(currentUid)
                        .update("chat", chatMap) // 내 firestore 'chat' 필드 update

                    readChatRoom(chatUserName)
                }
            }
    }

}