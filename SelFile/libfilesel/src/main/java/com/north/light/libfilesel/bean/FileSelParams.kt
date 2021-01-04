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
    var mSelNum: Int = 9
    //选择文件的大小--默认为0不过滤
    var mSelSize: Long = 0

//    /**
//     * 重置参数
//     * */
//    fun reset() {
//        mFormat.clear()
//        mSelSize = 0
//        mSelNum = 9
//    }

}