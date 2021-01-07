package com.north.light.libfilesel.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.north.light.libfilesel.R
import kotlinx.android.synthetic.main.activity_file_sel.*


/**
 * 文件选择activity
 * FileManager.getInstance().fileResult(result)
 * FileManager.getInstance().fileError(message)
 * */
class FileSelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_sel)
        initData()
    }

    private fun initData() {
        //设置fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_file_sel_content, FileSelFragment.newInstance())
            .commitAllowingStateLoss()

        //点击监听
        activity_file_sel_back.setOnClickListener {
            finish()
        }
        activity_file_sel_confirm.setOnClickListener {
            //选择
            for (frag in supportFragmentManager.fragments) {
                (frag as? BaseFileSelFragment)?.confirm()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}
