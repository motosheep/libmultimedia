package com.north.light.libfilesel.ui

import androidx.fragment.app.Fragment
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.utils.FileScanManager

/**
 * author:li
 * date:2021/1/7
 * desc:文件选择基类fragment
 */
abstract class BaseFileSelFragment : Fragment() {

    /**
     * 选择数据
     * */
    open fun confirm() {
    }

    /**
     * init scan
     * */
    fun initScan() {
        FileScanManager.instance.init(this.context!!.applicationContext)
        FileScanManager.instance
            .setScanFileListener(object : FileScanManager.ScanFileListener {
                override fun scanResult(result: MutableList<FileInfo>?) {
                    this@BaseFileSelFragment.scanResult(result)
                }


                override fun error(message: String?) {
                    this@BaseFileSelFragment.scanError(message)
                }
            })
    }

    /**
     * 开始扫描
     * */
    fun scan() {
        //查询数据--根据入参，进行不同的方式扫描
        when (FileManager.getInstance().getParams()?.mScanWay) {
            1 -> FileScanManager.instance.scanDatabase()
            else -> FileScanManager.instance.scanLocal()
        }
    }

    /**
     * 停止扫描
     * */
    fun stopScan() {
        FileScanManager.instance.removeScanFileListener()
        FileScanManager.instance.release()
    }

    /**
     * 扫描结果回调
     * */
    open fun scanResult(result: MutableList<FileInfo>?) {

    }

    /**
     * 错误回调
     * */
    open fun scanError(message: String?) {

    }
}