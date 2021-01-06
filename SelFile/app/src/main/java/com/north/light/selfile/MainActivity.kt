package com.north.light.selfile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.bean.FileSelParams

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public fun scan(view: View){
        FileManager.getInstance().setParams(FileSelParams().apply {
            mFormat = mutableListOf("mp3","jpg","jpeg")
        }).start(this)
    }
}
