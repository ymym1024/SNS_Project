package com.app.sns_project.util

import java.text.SimpleDateFormat

class CommonService {
    fun convertTimestampToDate(time: Long?): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val date = sdf.format(time).toString()
        return date
    }
}