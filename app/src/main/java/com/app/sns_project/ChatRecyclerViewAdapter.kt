package com.app.sns_project

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class ChatRecyclerViewAdapter(
    private val viewModel: MyViewModel,
    val context: Context?,
    val fragment: Fragment
):
    RecyclerView.Adapter<ChatRecyclerViewAdapter.ChatRecyclerViewViewHolder>() {


    val db = Firebase.firestore
    // 현재 로그인한 user의 uid
//    val currentUid = Firebase.auth.currentUser?.uid.toString()
    val currentUid = "uid1"
    // user Collection Ref
    val userColRef = db.collection("test")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecyclerViewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_itemview,
            parent, false)
        return ChatRecyclerViewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatRecyclerViewViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount(): Int {
        // 채팅방 수
        return viewModel.chatTitleItemList.size
    }

    inner class ChatRecyclerViewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val chatUserProfileImage: CircleImageView = itemView.findViewById(R.id.circleImageView)
        private val chatUsername: TextView = itemView.findViewById(R.id.chatUserNameTextView)
        private val chatLastMessage: TextView = itemView.findViewById(R.id.lastMessageTextView)

        fun setContents(pos: Int){
            with(viewModel.chatTitleItemList[pos]){
                // profileImage 세팅
                Glide.with(itemView).load(profileImageUrl).into(chatUserProfileImage)
                // username 세팅
                chatUsername.text = username
                // last message 세팅
                chatLastMessage.text = lastMessage

            }

            chatLastMessage.setOnClickListener {
//                NavController(context).navigate(ChatFragmentDirections.actionChatFragmentToChatRoomFragment(""))
                fragment.findNavController().navigate(ChatFragmentDirections.actionChatFragmentToChatRoomFragment(chatUsername.text.toString()))
            }


        }

    }

}