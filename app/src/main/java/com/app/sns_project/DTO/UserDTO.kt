package com.app.sns_project.DTO

data class UserDTO(
    var userName: String? = null,//uid 관리
    var profileImage: String? = null,
    var followerCount: Int = 0,
    var followingCount: Int = 0,
    var followers: HashMap<String, String> = HashMap(),
    var following: HashMap<String, String> = HashMap()
)
