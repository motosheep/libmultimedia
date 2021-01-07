package com.north.light.libfilesel.thread

import android.text.TextUtils
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Created by lzt
 * time 2021/1/5
 * 描述：文件扫描类线程管理
 */
class FileThreadManager : Serializable {
    private val DEFAULT = "DEFAULT"
    //线程池map
    private val mPoolMap = ConcurrentHashMap<String, ExecutorService>()

    companion object {
        @JvmStatic
        fun getInstance(): FileThreadManager {
            return SingleHolder.mInstance
        }
    }

    object SingleHolder {
        val mInstance = FileThreadManager()
    }

    /**
     * 释放所有线程池
     * */
    fun closeAllExecutors() {
        for (entry in mPoolMap.entries) {
            try {
                // 向学生传达“问题解答完毕后请举手示意！”
                entry.value.shutdown()
                // 向学生传达“XX分之内解答不完的问题全部带回去作为课后作业！”后老师等待学生答题
                // (所有的任务都结束的时候，返回TRUE)
                if (!entry.value.awaitTermination(1, TimeUnit.MILLISECONDS)) {
                    // 超时的时候向线程池中所有的线程发出中断(interrupted)。
                    entry.value.shutdownNow()
                }
            } catch (e: InterruptedException) {
                // awaitTermination方法被中断的时候也中止线程池中全部的线程的执行。
                println("awaitTermination interrupted: $e")
                entry.value.shutdownNow()
            } finally {
                mPoolMap.remove(entry.key)
            }
        }
    }

    /**
     * 自动分配任务
     * */
    fun getAutoCacheExecutors(count: Int, runnable: Runnable) {
        getCacheExecutors("auto" + (count % 10)).submit(runnable)
    }

    /**
     * 获取cache executor
     */
    fun getCacheExecutors(key: String): ExecutorService {
        var name = key
        if (TextUtils.isEmpty(key)) {
            name = DEFAULT + "CACHE"
        }
        val pool = mPoolMap[name]
        if (pool == null) {
            mPoolMap[name] = Executors.newCachedThreadPool()
        }
        return mPoolMap[name]!!
    }


}