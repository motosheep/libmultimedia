package com.north.light.libfilesel

import android.app.Activity
import android.content.Intent
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileSelParams
import com.north.light.libfilesel.ui.FileSelActivity
import java.io.Serializable

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：文件管理类，用于设置访问参数
 * 使用--构造者模式，直接使用即可
 */
class FileManager : Serializable {
    //文件选择参数
    private var mParams: FileSelParams? = null
    //选择文件监听
    private var mListener: onSelFileListener? = null

    companion object {
        @JvmStatic
        fun getInstance(): FileManager {
            return SingleHolder.mInstance
        }
    }

    object SingleHolder : Serializable {
        val mInstance = FileManager()
    }

    fun getParams(): FileSelParams? {
        return mParams
    }

    /**
     * 设置参数
     * */
    fun setParams(params: FileSelParams): FileManager {
        this.mParams = params
        return this
    }

    /**
     * 启动
     * */
    fun start(activity: Activity?) {
        if (activity == null) return
        if (this.getParams()?.mFormat.isNullOrEmpty()) {
            fileError("传入参数错误")
            return
        }
        val intent = Intent(activity, FileSelActivity::class.java)
        activity.startActivity(intent)
    }

    //activity返回---------------------------------------------

    /**
     * activity result返回结果
     * */
    fun fileResult(data: MutableList<FileInfo>?) {
        //返回选择文件路径结果
        try {
            mListener?.fileList(data ?: ArrayList())
        } catch (e: Exception) {
            mListener?.error(e.message)
        }
    }

    /**
     * activity 扫描错误
     * */
    fun fileError(message: String?) {
        mListener?.error(message)
    }

    //监听事件-----------------------------------
    interface onSelFileListener {
        fun fileList(info: MutableList<FileInfo>)
        fun error(message: String?)
    }

    fun setOnSelListener(listener: onSelFileListener) {
        this.mListener = listener
    }

    fun removeSelListener() {
        this.mListener = null
    }


}