package com.north.light.libfilesel.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.north.light.libfilesel.api.FinishCallback
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileScanInfo
import com.north.light.libfilesel.thread.FileCoroutineScan
import com.north.light.libfilesel.thread.FileDatabaseScanRunnable
import com.north.light.libfilesel.thread.FileLocalScanParentRun
import com.north.light.libfilesel.thread.FileThreadManager
import java.io.Serializable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：文件扫描工具类
 *
 *
 * 使用：---
 * 1、init(this)
 * 2、监听
 * 3、扫描
 * 4、移除监听
 * 5、release（）
 */
class FileScanManager : Serializable, FileScanManagerInterface {
    //全盘扫描----------------------------------------------------------
    var context: Context? = null
    private var mListener: ScanFileListener? = null

    private object SingleHolder {
        val mInstance = FileScanManager()
    }

    /**
     * 递归扫描本地文件
     */
    override fun scanLocal() {
        val localRootPath = OpenFileUtils.rootPath
        if (!TextUtils.isEmpty(localRootPath)) {
            scanStart(localRootPath)
        }
    }

    /**
     * 扫描content provider
     */
    override fun scanDatabase() {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mListener?.error("需读写权限")

            return
        }
        if (context == null) {
            mListener?.error("初始化失败，停止扫描")
            return
        }
        FileScanInfo.mStopTAG = AtomicBoolean(false)
        FileThreadManager.getInstance().closeAllExecutors()
        FileThreadManager.getInstance().getCacheExecutors("LOCAL_DATABASE")
            .execute(FileDatabaseScanRunnable(object : FinishCallback {
                override fun finish() {
                    mListener?.scanResult(FileScanInfo.getDataBaseList())
                }

                override fun init() {}
                override fun error(message: String?) {
                    mListener?.error(message)
                }
            }))
    }

    /**
     * 协程扫描
     * */
    override fun scanLocalWithCor() {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mListener?.error("需要读写权限")
            return
        }
        if (context == null) {
            mListener?.error("初始化失败，停止扫描")
            return
        }
        val localRootPath = OpenFileUtils.rootPath
        if (TextUtils.isEmpty(localRootPath)) {
            mListener?.error("目录错误，停止扫描")
            return
        }
        FileScanInfo.mStopTAG.set(false)
        FileCoroutineScan.getInstance().run(localRootPath, object : FinishCallback {
            override fun finish() {
                mListener?.scanResult(FileScanInfo.getCorList())
            }

            override fun init() {
            }

            override fun error(message: String?) {
                mListener?.error(message)
            }
        })
    }

    /**
     * 初始化
     */
    override fun init(context: Context) {
        if (this.context == null) {
            this.context = context.applicationContext
        }
    }

    /**
     * 释放
     */
    override fun release() {
        try {
            FileCoroutineScan.getInstance().removeJob()
            FileScanInfo.mStopTAG = AtomicBoolean(true)
            FileThreadManager.getInstance().closeAllExecutors()
            context = null
        } catch (e: Exception) {
        }
    }

    /**
     * 全盘扫描：扫描特定目录下的文件
     */
    private fun scanStart(path: String) {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mListener?.error("需要读写权限")
            return
        }
        if (context == null) {
            mListener?.error("初始化失败，停止扫描")
            return
        }
        if (TextUtils.isEmpty(path)) {
            mListener?.error("目录错误，停止扫描")
            return
        }
        //扫描--通过数据集合，平局分配对应的线程任务
        try {
            FileScanInfo.mStopTAG = AtomicBoolean(false)
            FileThreadManager.getInstance().getCacheExecutors("SCAN_PARENT_RUN")
                .execute(FileLocalScanParentRun(path, object : FinishCallback {
                    override fun finish() {
                        mListener?.scanResult(FileScanInfo.getLocalList())

                    }

                    override fun init() {
                    }

                    override fun error(message: String?) {
                        mListener?.error(message)

                    }

                }))
        } catch (e: Exception) {
            mListener?.error(e.message)
        }
    }

    /**
     * 扫描监听
     */
    interface ScanFileListener {
        fun scanResult(result: MutableList<FileInfo>?)
        fun error(message: String?)
    }

    fun setScanFileListener(listener: ScanFileListener?) {
        mListener = listener
    }

    fun removeScanFileListener() {
        mListener = null
    }

    companion object {
        val instance: FileScanManager get() = SingleHolder.mInstance
    }
}