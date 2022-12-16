package com.app.sns_project.data

import android.util.Log
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import com.app.sns_project.data.model.Post
import com.app.sns_project.util.pushMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.HashMap

class FirebaseDbService {
    var store : FirebaseFirestore = FirebaseFirestore.getInstance()
    var auth : FirebaseAuth = FirebaseAuth.getInstance()
    var uid : String = auth.currentUser!!.uid

    //스냅샷
    var postSnapshot : ListenerRegistration?=null


    //좋아요 트랜잭션
    fun transactionFavorite(postId:String){
        val doc = store?.collection("post").document(postId)
        store?.runTransaction { transaction ->
            val post = transaction.get(doc).toObject(Post::class.java)

            if (post!!.favorites.containsKey(uid)) {
                post.favoriteCount = post?.favoriteCount - 1
                post.favorites.remove(uid) // 사용자 remove
            } else {
                post.favoriteCount = post?.favoriteCount + 1
                post.favorites[uid] = true //사용자 추가
                alarm("favorite",post.uid!!)
            }
            transaction.set(doc, post)
        }
    }

    fun alarm(type:String,userId:String){
        var msg = ""
        store.collection("user").document(uid).get().addOnSuccessListener {
            val userName = it["userName"] as String

            when(type){
                "follow" -> {
                    msg = "%s 님이 회원님을 팔로우합니다."
                    var message = String.format(msg,userName)

                    store.collection("user").whereEqualTo("userName",userId).get()
                        .addOnSuccessListener {
                            for (doc in it){
                                pushMessage()?.sendMessage(doc.id, "알림 메세지 입니다.", message)
                            }
                        }

                }
                "favorite"-> {
                    msg = "%s 님이 좋아요를 눌렀습니다."

                    if(!userId.equals(uid)){
                        var message = String.format(msg,userName)
                        pushMessage()?.sendMessage(userId, "알림 메세지 입니다.", message)
                    }
                }
                "comment"-> {
                    msg = "%s 님이 댓글을 남겼습니다."
                    var message = String.format("%s 님이 댓글을 남겼습니다.",userName)
                    pushMessage()?.sendMessage(userId, "알림 메세지 입니다.", message)
                }
            }

        }
    }
}