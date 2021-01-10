package com.north.light.libfilesel.utils

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by lzt
 * time 2021/1/5
 * 描述：
 */
object ListSpilt : Serializable {
    /**
     * 分割List
     */
    fun <T> splitList(
        list: MutableList<T>?,
        groupSize: Int
    ): MutableList<MutableList<T>?> {
        if (list.isNullOrEmpty()) {
            return ArrayList()
        }
        val length = list.size
        // 计算可以分成多少组
        val num = (length + groupSize - 1) / groupSize // TODO
        val newList: MutableList<MutableList<T>?> =
            ArrayList(num)
        for (i in 0 until num) { // 开始位置
            val fromIndex = i * groupSize
            // 结束位置
            val toIndex = if ((i + 1) * groupSize < length) (i + 1) * groupSize else length
            newList.add(list.subList(fromIndex, toIndex))
        }
        return newList
    }
}