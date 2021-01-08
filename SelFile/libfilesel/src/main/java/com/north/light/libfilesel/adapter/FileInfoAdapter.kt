package com.north.light.libfilesel.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.R
import com.north.light.libfilesel.bean.FileAdapterInfo
import com.north.light.libfilesel.bean.FileInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * author:li
 * date:2021/1/7
 * desc:文件信息adapter
 */
class FileInfoAdapter(context: Context) : RecyclerView.Adapter<FileInfoAdapter.InfoHolder>() {
    //数据集合
    private var mData: MutableList<FileAdapterInfo> = ArrayList()
    private var mContext: Context? = null

    private var mOnClick: OnClickListener? = null

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
        val result = mData[position]
        result.apply {
            //1音乐 2视频 3pdf 4word 5excel 6pic 7ppt 0未识别
            holder.mPic.setImageResource(
                when (fileSource) {
                    1 -> R.mipmap.ic_file_music
                    2 -> R.mipmap.ic_file_video
                    3 -> R.mipmap.ic_file_pdf
                    4 -> R.mipmap.ic_file_word
                    5 -> R.mipmap.ic_file_excel
                    6 -> R.mipmap.ic_file_pic
                    7 -> R.mipmap.ic_file_ppt
                    else -> R.mipmap.ic_file_other

                }
            )
            //名字
            holder.mName.text = fileName ?: "暂无名字"
            try {
                holder.mTime.text =
                    SimpleDateFormat("yyyy-MM-dd HH-mm").format(Date(fileModifyDate))
            } catch (e: Exception) {
                holder.mTime.text = ""
            }
            //是否选中
            holder.mCheck.isChecked = isSelect
            //监听
            holder.mCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                val selCount = mData.filter { it.isSelect }.count()
                val limit = FileManager.getInstance().getParams()?.mSelNum ?: 0
                if (selCount < limit) {
                    isSelect = isChecked
                    notifyDataSetChanged()
                } else {
                    isSelect = false
                    notifyItemChanged(position)
                    mOnClick?.tips("只能选择${limit}个文件")
                }

            }
        }
    }

    /**
     * 获取选中的数据
     * */
    fun getSelList(): MutableList<FileInfo> {
        return mData.filter {
            it.isSelect
        }.map {
            var fileCache = FileInfo()
            fileCache.fileLength = it.fileLength
            fileCache.fileModifyDate = it.fileModifyDate
            fileCache.fileName = it.fileName
            fileCache.fileParentPath = it.fileParentPath
            fileCache.filePath = it.filePath
            return@map fileCache
        }.toMutableList()
    }

    override fun onViewRecycled(holder: InfoHolder) {
        super.onViewRecycled(holder)
        holder.mCheck?.setOnCheckedChangeListener(null)
    }

    /**
     * 设置数据集合
     * */
    fun setInfo(result: MutableList<FileInfo>?) {
        if (result.isNullOrEmpty()) {
            mData = ArrayList()
            notifyDataSetChanged()
        } else {
            //有数据--转换
            mData.clear()
            mData = result.map {
                var cache = FileAdapterInfo()
                cache.fileLength = it.fileLength
                cache.fileModifyDate = it.fileModifyDate
                cache.fileName = it.fileName
                cache.fileParentPath = it.fileParentPath
                cache.filePath = it.filePath
                cache.fileSource = cache.getFileType()
                return@map cache
            }.toMutableList()
            notifyDataSetChanged()
        }
    }


    inner class InfoHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mName = view.findViewById<TextView>(R.id.recy_fragment_file_sel_item_name)
        var mTime = view.findViewById<TextView>(R.id.recy_fragment_file_sel_item_time)
        var mCheck = view.findViewById<CheckBox>(R.id.recy_fragment_file_sel_item_check)
        var mPic = view.findViewById<ImageView>(R.id.recy_fragment_file_sel_item_pic)
    }

    //点击事件
    interface OnClickListener {

        fun tips(message: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.mOnClick = onClickListener
    }

    fun removeOnClickListener() {
        this.mOnClick = null
    }


}