package com.north.light.selfile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.bean.FileSelParams

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FileManager.getInstance().setParams(FileSelParams()).start(this)
    }
}
