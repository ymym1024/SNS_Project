package com.app.sns_project

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class RecyclerViewAdapter2(private val viewModel: MyViewModel, val context: Context?):
    RecyclerView.Adapter<RecyclerViewAdapter2.RecyclerViewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_view_following,
            parent, false)
//        val inflater = LayoutInflater.from(parent.context)
//        val binding = ItemViewBinding.inflate(inflater,parent,false)
        return RecyclerViewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount(): Int {
        // 팔로잉 수
        return viewModel.items2.size
    }

    inner class RecyclerViewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val db = Firebase.firestore
        val userColRef = db.collection("user")
        val currentUid = "currentUserUid"


        private val profileImage: CircleImageView = itemView.findViewById(R.id.circleImageView)
        private val followerUsername: TextView = itemView.findViewById(R.id.userNametextView)
        private val followingButton: ImageButton = itemView.findViewById(R.id.followingButton)
        private val followButton: ImageButton = itemView.findViewById(R.id.followButton3)

        fun setContents(pos: Int){
            with(viewModel.items2[pos]){
                // profileImage 세팅
                Glide.with(itemView).load(profileImageUrl).into(profileImage)
                // username 세팅
                followerUsername.text = username
            }

            followingButton.setOnClickListener {

                val layoutInflater = LayoutInflater.from(context)
                val view = layoutInflater.inflate(R.layout.custom_dialog2,null)

                val alertDialog = AlertDialog.Builder(context,R.style.CustomAlertDialog)
                    .setView(view)
                    .create()

                val confirmButton = view.findViewById<ImageButton>(R.id.confirmButton)
                val cancelButton = view.findViewById<ImageButton>(R.id.cancelButton)

                confirmButton.setOnClickListener {
                    // 내 팔로잉 숫자 -1 , 내 팔로잉 목록에서 해당 유저 삭제
                    // 해당 유저 팔로워 숫자 -1 , 해당 유저 팔로워 목록에서 나 삭제
                    unfollowUser()
                    alertDialog.dismiss()
                }
                cancelButton.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()

            }
        }

        private fun unfollowUser(){

            // alert로 언팔로우 하겠냐고 되묻는거 추가

            // <issue> <해결됨> 맞팔중인 상태에서 팔로잉 목록에서 unfollow 했을때,
            // 팔로워 목록에 있는 해당 유저에 Follow 버튼이 다시 활성화 되어야 하는데 보이지 않음.

            val index = adapterPosition
            val clickedUser = viewModel.items2[index] // 현재 로그인한 user의 팔로잉 목록에서 언팔로우당한 user
            userColRef.document(currentUid).get()
                .addOnSuccessListener {
                    val followingList = it["following"] as MutableMap<String,String> // 현재 로그인한 user의 팔로잉 목록에서
                    followingList.remove(clickedUser.username) // 해당 유저 삭제

                    userColRef.document(currentUid)
                        .update("following",followingList) // firestore 팔로잉 목록 update
                    userColRef.document(currentUid)
                        .update("following count",followingList.size) // firestore 팔로잉 수 update





                    val currentUsername = it["username"].toString() // 현재 로그인한 user의 username 받아오기


                    userColRef.whereEqualTo("username",clickedUser.username).get()
                        .addOnSuccessListener {
                            for(doc in it){
                                val followerList = doc["follower"] as MutableMap<String, String> // 언팔로우당한 user의 팔로워 목록에서
                                followerList.remove(currentUsername) // 현재 로그인한 user 삭제

                                userColRef.document(doc.id)
                                    .update("follower",followerList) // firestore 팔로워 목록 update
                                userColRef.document(doc.id)
                                    .update("follower count",followerList.size) // firestore 팔로워 수 update
                            }
                        }
                }

            // <issue> <해결됨> 팔로우 버튼 다른 버튼으로(다시 팔로우하는) 변경하는거 추가해야함
            // 한 가지 대안으로, 버튼 텍스트를 "팔로잉" 말고 "팔로잉 중" 으로 바꾸고 follower 탭에서 삭제 버튼과 유사한 로직으로 그냥 삭제시켜버리면
            // 굳이 버튼을 교체하지 않아도 됨.
            // 언팔로우 alertDialog로 물어볼거면 -> 보여지는 팔로잉 목록에서 바로 없애고
            // 안 물어볼거면 -> 밑에처럼 버튼 변경하는 방법으로
//            followingButton.visibility = View.INVISIBLE
//            followButton.visibility = View.VISIBLE

            // 앱에서 보여지는 현재 로그인한 user의 팔로잉 목록 업데이트
            viewModel.deleteItem2(index)

        }

    }

}