package com.north.light.libfilesel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.R
import com.north.light.libfilesel.adapter.FileInfoAdapter
import com.north.light.libfilesel.bean.FileInfo
import kotlinx.android.synthetic.main.fragment_file_sel.*

/**
 * author:li
 * date:2021/1/7
 * desc:文件选择fragment
 */
class FileSelFragment : BaseFileSelFragment() {
    private var mInfoAdapter: FileInfoAdapter? = null

    companion object {
        @JvmStatic
        fun newInstance(): FileSelFragment {
            var args = Bundle()
            var fragment = FileSelFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_file_sel, container, false)
    }


    override fun onDestroyView() {
        stopScan()
        super.onDestroyView()
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    /**
     * 选择数据--adapter选择后返回
     * */
    override fun confirm() {
        super.confirm()
        //选择数据并进行回调
        FileManager.getInstance().fileResult(mInfoAdapter?.getSelList())
        activity?.finish()
    }

    /**
     * 初始化界面
     * */
    private fun initView() {
        //初始化数据
        mInfoAdapter = FileInfoAdapter(context!!)
        fragment_file_sel_recyclerview.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        fragment_file_sel_recyclerview.adapter = mInfoAdapter
        fragment_file_sel_refresh.setColorSchemeColors(
            ContextCompat.getColor(
                context!!,
                R.color.file_theme_color
            )
        )
        mInfoAdapter?.setOnClickListener(object : FileInfoAdapter.OnClickListener {
            override fun tips(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
        initScan()
        fragment_file_sel_refresh.setOnRefreshListener {
            //开始扫描
            scan()
        }
        getData()
    }

    /**
     * 加载数据
     * */
    private fun getData() {
        fragment_file_sel_refresh.isRefreshing = true
        scan()
    }

    /**
     * 数据扫描返回
     * */
    override fun scanResult(result: MutableList<FileInfo>?) {
        super.scanResult(result)
        activity?.runOnUiThread {
            fragment_file_sel_refresh.isRefreshing = false
            //设置数据
            mInfoAdapter?.setInfo(result)
        }
    }

    /**
     * 数据扫描错误
     * */
    override fun scanError(message: String?) {
        super.scanError(message)
        activity?.runOnUiThread {
            fragment_file_sel_refresh.isRefreshing = false
            if (!message.isNullOrBlank()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

    }
}