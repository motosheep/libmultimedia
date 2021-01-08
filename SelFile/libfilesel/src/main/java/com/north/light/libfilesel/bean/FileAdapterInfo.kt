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
    //文件类型
    var fileSource = 0
    //是否选中
    var isSelect = false


    //判断文件类型----------------------------------
    /**
     * 获取文件类型
     * @return int 1音乐 2视频 3pdf 4word 5excel 6pic 7ppt 0未识别
     * */
    fun getFileType(): Int {
        return when {
            isMusic() -> 1
            isVideo() -> 2
            isPdf() -> 3
            isWord() -> 4
            isExcel() -> 5
            isPic() -> 6
            isPpt() -> 7
            else -> 0
        }
    }


    private fun isPic(): Boolean {
        return fileName?.toLowerCase()?.contains("png") == true ||
                fileName?.toLowerCase()?.contains("jpg") == true ||
                fileName?.toLowerCase()?.contains("jpeg") == true
    }

    private fun isMusic(): Boolean {
        return fileName?.toLowerCase()?.contains("mp3") == true
    }

    private fun isVideo(): Boolean {
        return fileName?.toLowerCase()?.contains("mp4") == true ||
                fileName?.toLowerCase()?.contains("avi") == true ||
                fileName?.toLowerCase()?.contains("rmvb") == true
    }

    private fun isPdf(): Boolean {
        return fileName?.toLowerCase()?.contains("pdf") == true
    }

    private fun isWord(): Boolean {
        return fileName?.toLowerCase()?.contains("doc") == true ||
                fileName?.toLowerCase()?.contains("docx") == true
    }

    private fun isExcel(): Boolean {
        return fileName?.toLowerCase()?.contains("xls") == true ||
                fileName?.toLowerCase()?.contains("xlsx") == true
    }

    private fun isPpt(): Boolean {
        return fileName?.toLowerCase()?.contains("ppt") == true ||
                fileName?.toLowerCase()?.contains("pptx") == true
    }

}