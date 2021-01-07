package com.north.light.libfilesel.bean

import java.io.Serializable

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：选择文件的信息
 */
class FileInfo : Serializable {
    //文件名称
    var fileName: String? = null
    //文件路径
    var filePath: String? = null
    //文件父路径
    var fileParentPath: String? = null
    //文件修改时间
    var fileModifyDate: Long = 0L
    //文件大小
    var fileLength: Long = 0L

}