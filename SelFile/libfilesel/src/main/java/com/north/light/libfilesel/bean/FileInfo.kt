package com.north.light.libfilesel.bean

import java.io.Serializable

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：选择文件的信息
 */
class FileInfo : Serializable {
    var fileName: String? = null
    var filePath: String? = null
    var fileParentPath: String? = null
    var fileModifyDate: Long = 0L
    var fileLength: Long = 0L

}