package com.north.light.libfilesel.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.north.light.libfilesel.R
import com.north.light.libfilesel.bean.FileAdapterInfo

/**
 * author:li
 * date:2021/1/7
 * desc:文件信息adapter
 */
class FileInfoAdapter(context: Context) : RecyclerView.Adapter<FileInfoAdapter.InfoHolder>() {
    //数据集合
    private var mData: MutableList<FileAdapterInfo> = ArrayList()
    private var mContext: Context? = null

    init {
        this.mContext = context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.recy_fragment_file_sel_item, parent, false)
        return InfoHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {

    }


    inner class InfoHolder(view: View) : RecyclerView.ViewHolder(view) {

    }
}