package com.north.light.selfile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileSelParams
import com.squareup.leakcanary.LeakCanary

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LeakCanary.install(this.application)
        FileManager.getInstance().setOnSelListener(object : FileManager.onSelFileListener {
            override fun fileList(info: MutableList<FileInfo>) {
                Log.d("MainActivity", Gson().toJson(info))
            }

            override fun error(message: String?) {
                Log.d("MainActivity", "error${message}")
            }
        })
    }

    public fun scan(view: View) {
        FileManager.getInstance().setParams(FileSelParams().apply {
            mFormat = mutableListOf("mp3","mp4","pdf","word")
        }).start(this)
    }
}
