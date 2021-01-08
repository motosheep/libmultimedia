package com.north.light.libfilesel.bean

import java.io.Serializable

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：文件访问参数
 */
class FileSelParams : Serializable {
    //选择的格式
    var mFormat: MutableList<String> = ArrayList()
    //选择的数量
    var mSelNum: Int = 1
    //选择文件的大小--默认为0不过滤--最小的大小--字节为单位
    var mSelMinSize: Long = 0
    //选择文件的大小--默认为0不过滤--最大的大小--字节为单位
    var mSelMaxSize: Long = 0

}