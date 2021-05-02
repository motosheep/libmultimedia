package com.north.light.libfilesel.thread

import com.north.light.libfilesel.api.FinishCallback
import com.north.light.libfilesel.bean.FileScanInfo
import com.north.light.libfilesel.utils.ListSpilt
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * author:li
 * date:2021/1/10
 * desc:多线程扫描父类runnable
 */
class FileLocalScanParentRun : Runnable {
    private val mThreadCounter = AtomicInteger(0)
    private val mTotalCounter = AtomicInteger(0)
    private val mNewThreadTAG = AtomicBoolean(true)
    private val mNumCounter = AtomicInteger(0)
    //一个List的大小--分割list
    private val MAX_LIST_COUNT = 2
    private val THREAD_COUNT = 15

    private var path = ""
    private var mListener: FinishCallback? = null

    constructor(path: String, listener: FinishCallback) {
        this.path = path
        this.mListener = listener
    }

    override fun run() {
        FileThreadManager.getInstance().closeAllExecutors()
        FileScanInfo.clearAll()
        val files = File(path).listFiles()?.toMutableList()
        val result = ListSpilt.splitList(files, MAX_LIST_COUNT)
        val finalList: MutableList<MutableList<File>?> = result
        mTotalCounter.set(0)
        mThreadCounter.set(0)
        mNumCounter.set(0)
        mNewThreadTAG.set(true)

        FileThreadManager.getInstance().getCacheExecutors("SCAN_PARENT")
            .execute {
                while (!FileScanInfo.mStopTAG.get()) {
                    if (mThreadCounter.get() < THREAD_COUNT) {
                        if (!mNewThreadTAG.get()) {
                            continue
                        }
                        mNewThreadTAG.set(false)
                        mThreadCounter.incrementAndGet()
                        if (mNumCounter.get() == finalList.size) {
                            continue
                        }
                        FileThreadManager.getInstance().getAutoCacheExecutors(
                            mNumCounter.get(),
                            FileLocalScanRunnable(
                                finalList[mNumCounter.getAndIncrement()]!!,
                                path,
                                object : FinishCallback {
                                    override fun error(message: String?) {
                                        mListener?.error(message)
                                    }

                                    override fun finish() {
                                        mThreadCounter.decrementAndGet()
                                        mTotalCounter.incrementAndGet()
                                        //执行到最后的判断
                                        if (mTotalCounter.get() == finalList.size) { //至此，全部扫描完毕
                                            mListener?.finish()
                                        }
                                    }

                                    override fun init() { //已经初始化完成
                                        mNewThreadTAG.set(true)
                                    }
                                })
                        )
                    }
                }
            }
    }
}