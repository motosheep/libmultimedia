package com.north.light.libfilesel.bean

import java.io.Serializable

/**
 * author:li
 * date:2021/1/7
 * desc:file adapter info 信息
 */
class FileAdapterInfo : Serializable {
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

    //判断文件类型----------------------------------
    fun isMusic(): Boolean {
        return fileName?.toLowerCase()?.contains("mp3") == true
    }

    fun isVideo(): Boolean {
        return fileName?.toLowerCase()?.contains("mp4") == true ||
                fileName?.toLowerCase()?.contains("avi") == true ||
                fileName?.toLowerCase()?.contains("rmvb") == true
    }

    fun isPdf(): Boolean {
        return fileName?.toLowerCase()?.contains("pdf") == true
    }

    fun isWord(): Boolean {
        return fileName?.toLowerCase()?.contains("doc") == true ||
                fileName?.toLowerCase()?.contains("docx") == true
    }

    fun isExcel(): Boolean {
        return fileName?.toLowerCase()?.contains("xls") == true ||
                fileName?.toLowerCase()?.contains("xlsx") == true
    }

}