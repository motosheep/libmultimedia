package com.north.light.selfile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileSelParams

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FileManager.getInstance().setOnSelListener(object : FileManager.onSelFileListener {
            override fun fileList(info: MutableList<FileInfo>) {
            }

            override fun error(message: String?) {
            }
        })
    }

    fun scan(view: View) {
        FileManager.getInstance().setParams(FileSelParams().apply {
            mFormat = mutableListOf("mp3","mp4","pdf","word")
            mScanWay=2
        }).start(this)
    }
}
