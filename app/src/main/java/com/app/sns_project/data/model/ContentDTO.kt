package com.app.sns_project.data.model

data class ContentDTO(val imageUrl : String? = null, // 이미지 관리
                      var content : String? = null, // 글
                      var uid : String? = null, // 어느 유저가 올렸는지 관리
                      var userName : String? = "default user", // 유저 이름 관리
                      var userId : String? = null, // 유저 이메일
                      var timestamp : Long? = null, // 컨텐츠를 올린 시간
                      var favoriteCount : Int = 0, // 좋아요 개수
                      var favorites : Map<String, Boolean> = HashMap()) { // 좋아요 중복을 방지하는 Map

    data class Comment(var uid: String? = null, // uid 관리
                       var userId: String? = null, // 이메일 관리
                       var comment : String? = null, // comment 관리
                       var userName : String? = null, // userName 관리
                       var timestamp: Long? = null) // 댓글을 올린 시간 관리
    data class UserInfo(var userName: String? = null,//uid 관리
                        var followerCount: Int = 0,
                        var followingCount: Int = 0,
                        var profileImage: String? = null,
                        var followers: HashMap<String, String>,
                        var following: HashMap<String, String>,
                        var chat: HashMap<String, String>)
}