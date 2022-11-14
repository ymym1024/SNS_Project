package com.app.sns_project.DTO

data class PostDTO(
    var uid : String?=null,
    var userId:String?=null,
    var content : String?=null,
    var timestamp : Long?=null,
    var imageUrl : List<String>? =null,
    var favoriteCount:Int = 0,
    var favorites : Map<String,Boolean> = HashMap()
){
    data class Comment(
        val uid:String,
        val userId:String,
        val comment:String,
        var timestamp: Long?=null
    )
}
