package com.north.light.libfilesel.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.R
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.utils.FileScanManager


/**
 * 文件选择activity
 * */
class FileSelActivity : AppCompatActivity() {
    val TAG = FileSelActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sel)
        initData()
    }

    private fun initData() {
        //查询数据
        FileScanManager.getInstance().init(this)
        FileScanManager.getInstance().setScanFileListener(object :FileScanManager.ScanFileListener{
            override fun scanResult(result: MutableList<FileInfo>?) {
                Log.e(TAG,"scanResult: ${Gson().toJson(result)}" )
            }

            override fun error(message: String?) {
                Log.e(TAG,"error message: $message" )
            }
        })
        FileScanManager.getInstance().scanLocal()
    }


    override fun onDestroy() {
        FileScanManager.getInstance().removeScanFileListener()
        FileScanManager.getInstance().release()
        super.onDestroy()
    }

}
