package com.app.sns_project.data.model

data class Comment(var uid: String? = null, // uid 관리
                  var userId: String? = null, // 이메일 관리
                  var comment : String? = null, // comment 관리
                  var userName : String? = null, // userName 관리
                  var timestamp: Long? = null) // 댓글을 올린 시간 관리
