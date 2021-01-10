package com.north.light.libfilesel.thread

import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.api.FinishCallback
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileScanInfo
import java.io.File

/**
 * Created by lzt
 * time 2021/1/7
 * 描述：文件扫描runnable
 */
class FileLocalScanRunnable : Runnable {

    var scanPath: List<File>
    var path: String
    var listener: FinishCallback? = null

    constructor(file: List<File>, path: String, callback: FinishCallback) {
        this.scanPath = file
        this.path = path
        this.listener = callback
        this.listener?.init()
    }

    override fun run() {
        try {
            for (file in scanPath) {
                if (FileScanInfo.mStopTAG.get()) {
                    break
                }
                listFile(file, path)
            }
            listener?.finish()
        } catch (e: Exception) {
            listener?.error(e.message)
        } finally {
            listener = null
        }

    }

    /**
     * list file递归
     * 使用递归标识控制
     */
    @Throws(Exception::class)
    private fun listFile(file: File, originalPath: String) {
        if (FileScanInfo.mStopTAG.get()) {
            return
        }
        val files: Array<File>? = file.listFiles()
        if (files != null)
            for (f in files) {
                if (!f.isDirectory) {
                    val params = FileManager.getInstance().getParams()
                    val format = f.name.substring(f.name.lastIndexOf(".") + 1).toLowerCase()
                    if (params != null) {
                        //格式判断
                        if (!params.mFormat.contains(format)) {
                            continue
                        }
                        //文件大小判断
                        if (params.mSelMinSize != 0L && params.mSelMinSize > f.length()) {
                            continue
                        }
                        if (params.mSelMaxSize != 0L && params.mSelMaxSize < f.length()) {
                            continue
                        }
                        val info = FileInfo()
                        info.fileName = f.name
                        info.filePath = f.absolutePath
                        info.fileModifyDate = f.lastModified()
                        info.fileLength = f.length()
                        info.fileParentPath = f.parent
                        FileScanInfo.getLocalList().add(info)
                    }
                } else if (f.isDirectory) {
                    //如果是目录，迭代进入该目录
                    listFile(f, originalPath)
                }
            }
    }
}