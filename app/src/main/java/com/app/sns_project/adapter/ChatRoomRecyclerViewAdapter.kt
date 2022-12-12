package com.app.sns_project.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.MyViewModel
import com.app.sns_project.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat


class ChatRoomRecyclerViewAdapter(
    private val viewModel: MyViewModel,
    val context: Context?
):
    RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ChatRoomRecyclerViewViewHolder>() {


    val db = Firebase.firestore
    // 현재 로그인한 user의 uid
    val currentUid = Firebase.auth.currentUser?.uid.toString()
//    val currentUid = "uid1"
    // user Collection Ref
    val userColRef = db.collection("user")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomRecyclerViewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.chatroom_itemview,
            parent, false)
        return ChatRoomRecyclerViewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatRoomRecyclerViewViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount(): Int {
        // 채팅방 메세지 수
        return viewModel.chatItemsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    inner class ChatRoomRecyclerViewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val yourProfileImage: CircleImageView = itemView.findViewById(R.id.circleImageView)
        private val yourMessage: TextView = itemView.findViewById(R.id.yourMessageTextView)
        private val yourTime: TextView = itemView.findViewById(R.id.yourTimeTextView)
        private val myMessage: TextView = itemView.findViewById(R.id.myMessageTextView)
        private val myTime: TextView = itemView.findViewById(R.id.myTimeTextView)

        // <issue> 상단바에 뒤로가기 버튼 옆에 상대 프로필사진,이름 표시하거나 최소 이름이라도 표시해야함

        fun setContents(pos: Int){
            with(viewModel.chatItemsList[pos]){
                userColRef.document(currentUid).get()
                    .addOnSuccessListener {
                        if(username == it["userName"]){ // 내가 보낸 메세지면
                            myMessage.text = message
                            myTime.text = convertTimestampToDate(time)
                            myMessage.visibility = View.VISIBLE
                            myTime.visibility = View.VISIBLE
                        }
                        else{ // 상대방이 보낸 메세지면
                            userColRef.whereEqualTo("userName",username).get()
                                .addOnSuccessListener {
                                    for(doc in it) {
                                        Glide.with(itemView).load(doc["profileImage"].toString()).into(yourProfileImage)
                                        yourProfileImage.visibility = View.VISIBLE
                                    }
                                }
                            yourMessage.text = message
                            yourTime.text = convertTimestampToDate(time)
                            yourMessage.visibility = View.VISIBLE
                            yourTime.visibility = View.VISIBLE
                        }
                    }
            }
        }

        private fun convertTimestampToDate(time: Long?): String {
//            val currentDateTime =
//                Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime()
//            val date = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").format(currentDateTime)
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
            val date = sdf.format(time).toString()
            return date
        }

    }

}