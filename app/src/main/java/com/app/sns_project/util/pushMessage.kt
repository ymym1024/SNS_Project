package com.app.sns_project.util

import android.util.Log
import com.app.sns_project.data.model.PushMessageDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class pushMessage {

    val BASE_URL = "https://fcm.googleapis.com/fcm/send"
    val SERVICE_KEY = "AAAAINNd4iU:APA91bEp6lBR6Kh-e_OfWyrYigKwOwvHAY9Z8hBnMVX2cUC6WRhGioUGZwhfob-z8jAdwDbfcKHI6TTXPEvTAgSojchTejWxqW-OrLQCGbUbRozP3GEWrfg37CYt71ecef18mwGa7Jgd"

    var okHttpClient: OkHttpClient? = null
    init {
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(uid:String,title:String,msg:String){
        FirebaseFirestore.getInstance().collection("pushtokens").document(uid).get().addOnCompleteListener {
            if(it.isSuccessful){
                var token = it.result["pushtoken"].toString()
                println("token ::"+token)
                var pushDTO = PushMessageDTO()
                pushDTO.to = token
                pushDTO.notification?.title = title
                pushDTO.notification?.body = msg

                var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gson()?.toJson(pushDTO))
                var request = Request
                    .Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key=$SERVICE_KEY")
                    .url(BASE_URL)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                    }
                    override fun onResponse(call: Call?, response: Response?) {
                        println(response?.body()?.string())
                    }
                })
            }
        }
    }

    fun saveToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            var pushToken = ""
            if(task.isSuccessful){
                pushToken = task.result?: ""
            }
            Log.d("pushToken",pushToken)
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var map = mutableMapOf<String,Any>()
            map["pushtoken"] = pushToken!!
            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
        }
    }
}