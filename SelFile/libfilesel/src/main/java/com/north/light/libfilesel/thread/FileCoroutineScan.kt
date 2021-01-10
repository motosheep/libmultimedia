package com.north.light.libfilesel.thread

import android.util.Log
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.api.FinishCallback
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileScanInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * author:li
 * date:2021/1/10
 * desc:kotlin conroutine 扫描
 */
class FileCoroutineScan : Serializable {
    //协程对象集合
    private var mCorMap = ConcurrentHashMap<String, Deferred<*>>()

    companion object {
        fun getInstance(): FileCoroutineScan {
            return SingleHolder.mInstance
        }
    }

    object SingleHolder {
        val mInstance = FileCoroutineScan()
    }

    /**
     * 移除线协程任务
     * */
    fun removeJob() {
        for (map in mCorMap) {
            try {
                map.value.cancel()
                mCorMap.remove(map.key)
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 传入目录为根目录
     * */
    fun run(path: String, listener: FinishCallback?) {
        try {
            Log.d("cor", "协程扫描: start")
            val totalJob = GlobalScope.async {
                removeJob()
                //清空原来的数据
                FileScanInfo.getCorList().clear()
                val files = File(path).listFiles()?.toMutableList()
                //目录粒度精细到二级目录
                var scanFileList: MutableList<File> = ArrayList()
                for (file in files ?: ArrayList()) {
                    if (file.isDirectory) {
                        var childFileList = file.listFiles()?.toMutableList()
                        if (!childFileList.isNullOrEmpty()) {
                            for (c in childFileList) {
                                if (c.isDirectory) {
                                    scanFileList.add(c)
                                } else {
                                    //文件，加入数据集合
                                    putFileToList(file)
                                }
                            }
                        }
                    } else {
                        //文件，加入数据集合
                        putFileToList(file)
                    }
                }
                listener?.init()
                //至此，目录统计完成，使用协程多开，全部扫描
                var counter = AtomicInteger(0)
                for (file in scanFileList) {
                    val job = GlobalScope.async(Dispatchers.IO) {
                        listFile(file)
                        counter.incrementAndGet()
                        if (counter.get() == scanFileList.size) {
                            //扫描完成
                            listener?.finish()
                            Log.d("cor", "协程扫描: end")
                        }
                    }
                    mCorMap[job.toString()] = job
                    job.start()
                }
            }
            mCorMap[totalJob.toString()] = totalJob
            totalJob.start()
        } catch (e: Exception) {
            listener?.error(e.message)
        }
    }

    /**
     * 添加file到集合
     * */
    private fun putFileToList(cacheFile: File) {
        try {
            val params = FileManager.getInstance().getParams() ?: return
            val format =
                cacheFile.name.substring(cacheFile.name.lastIndexOf(".") + 1)
                    .toLowerCase()
            if (!params.mFormat.contains(format)) {
                return
            }
            //文件大小判断
            if (params.mSelMinSize != 0L && params.mSelMinSize > cacheFile.length()) {
                return
            }
            if (params.mSelMaxSize != 0L && params.mSelMaxSize < cacheFile.length()) {
                return
            }
            //至此，认为符合条件--加入集合
            val info = FileInfo()
            info.fileName = cacheFile.name
            info.filePath = cacheFile.absolutePath
            info.fileModifyDate = cacheFile.lastModified()
            info.fileLength = cacheFile.length()
            info.fileParentPath = cacheFile.parent
            FileScanInfo.getDataBaseList().add(info)
            FileScanInfo.getCorList().add(info)
        } catch (e: Exception) {

        }
    }


    /**
     * list file递归
     * 使用递归标识控制
     */
    @Throws(Exception::class)
    private suspend fun listFile(file: File) {
        if (FileScanInfo.mStopTAG.get()) {
            return
        }
        val files: Array<File>? = file.listFiles()
        if (files != null)
            for (f in files) {
                if (!f.isDirectory) {
                    putFileToList(f)
                } else if (f.isDirectory) {
                    //如果是目录，迭代进入该目录
                    listFile(f)
                }
            }
    }

}