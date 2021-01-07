package com.north.light.libfilesel.bean

import android.text.TextUtils
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by lzt
 * time 2021/1/5
 * 描述：文件扫描存储对象
 */
class FileScanInfo : Serializable {

    companion object {
        //扫描结果
        @JvmStatic
        var mScanResult = ConcurrentHashMap<String, MutableList<FileInfo>>()
        //停止递归标识--用于线程结束
        @JvmStatic
        var mStopTAG = AtomicBoolean(false)


        /**
         * 获取内存中的数据集合--key--value
         */
        fun getDataMap(key: String): MutableList<FileInfo> {
            if (TextUtils.isEmpty(key)) {
                return ArrayList()
            }
            val result = mScanResult[key]
            if (result == null || result.size == 0) {
                mScanResult[key] = ArrayList()
            }
            return mScanResult[key] ?: ArrayList()
        }
    }

}