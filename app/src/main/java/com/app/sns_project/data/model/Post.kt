package com.app.sns_project.data.model

data class Post(
    var uid : String?=null,
    var userName:String?=null,
    var content : String?=null,
    var timestamp : Long?=null,
    var imageUrl : List<String>? = arrayListOf(),
    var favoriteCount:Int = 0,
    var favorites : HashMap<String,Boolean> = HashMap()
)