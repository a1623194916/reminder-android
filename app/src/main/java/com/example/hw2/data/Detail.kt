package com.example.hw2.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 设置isChecked的初始值为false
@Parcelize
data class detail(
    var title: String, //标题
    val description: String? =null, //描述
    var isChecked: Boolean = false, //是否完成
    var begindate: Long? = null,
    var enddate: Long?=null,
    var begintime:Long?=null,
    var endtime:Long?=null,
    var priority:Int?=3, //默认无优先级
    var id:Int?=null,
    var imageUrl:String?=null,
    var fileUrl:String?=null,
): Parcelable
