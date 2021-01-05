package com.north.light.libfilesel.utils

import android.content.Context

/**
 * Created by lzt
 * time 2021/1/5
 * 描述：file scan manager interface
 */
interface FileScanManagerInterface {
    fun init(context: Context)
    fun release()
    //扫描本地全部
    fun scanLocal()
    //扫描数据库
    fun scanDatabase()
}