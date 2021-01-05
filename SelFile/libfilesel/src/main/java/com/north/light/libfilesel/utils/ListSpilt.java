package com.north.light.libfilesel.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzt
 * time 2021/1/5
 * 描述：
 */
public class ListSpilt implements Serializable {
    /**
     * 分割List
     */
    public static <T> List<List<T>> splitList(List<T> list, int groupSize) {
        int length = list.size();
        // 计算可以分成多少组
        int num = (length + groupSize - 1) / groupSize; // TODO
        List<List<T>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
            newList.add(list.subList(fromIndex, toIndex));
        }
        return newList;
    }
}
