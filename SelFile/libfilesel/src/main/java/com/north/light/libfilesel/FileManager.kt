package com.north.light.libfilesel

import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileSelParams
import com.north.light.libfilesel.ui.FileSelActivity
import java.io.Serializable

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：文件管理类，用于设置访问参数
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

    /**
     * 初始化
     * */
    fun init(){

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
        val intent = Intent(activity, FileSelActivity::class.java)
        activity.startActivity(intent)
    }

    /**
     * activity result返回
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
     * 生成json list
     */
    fun <T> getJsonList(jsonString: String, cls: Class<T>): MutableList<T> {
        val list = ArrayList<T>()
        try {
            val gson = Gson()
            val arry = JsonParser().parse(jsonString).asJsonArray
            for (jsonElement in arry) {
                list.add(gson.fromJson(jsonElement, cls))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    //监听事件-----------------------------------
    interface onSelFileListener {
        fun fileList(info: MutableList<FileInfo>)
        fun error(message: String?)
    }

    fun setOnSelListener(listener: onSelFileListener) {
        this.mListener = listener
    }

}