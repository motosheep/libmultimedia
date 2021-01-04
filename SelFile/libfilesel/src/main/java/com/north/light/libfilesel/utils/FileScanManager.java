package com.north.light.libfilesel.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.north.light.libfilesel.FileManager;
import com.north.light.libfilesel.bean.FileInfo;
import com.north.light.libfilesel.bean.FileSelParams;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：文件扫描工具类
 */
public class FileScanManager implements Serializable {
    private static final String TAG = FileScanManager.class.getSimpleName();
    //相关线程
    private Handler mIOHandler;
    private HandlerThread mIOThread;
    private Context mContext;
    //监听
    private ScanFileListener mListener;
    //扫描结果
    private ConcurrentMap<String, List<FileInfo>> mScanResult = new ConcurrentHashMap<>();
    //u最大线程数量
    private int MAX_THREAD_COUNT = 10;
    //线程计数器
    private AtomicInteger mThreadCounter = new AtomicInteger(0);

    private static final class SingleHolder {
        static final FileScanManager mInstance = new FileScanManager();
    }

    public static FileScanManager getInstance() {
        return SingleHolder.mInstance;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
            mIOThread = new HandlerThread("FILESCANMANAGER_IO_THREAD");
            mIOThread.start();
            mIOHandler = new Handler(mIOThread.getLooper());
        }
    }

    /**
     * 释放
     */
    public void release() {
        try {
            if (mIOHandler != null) {
                mIOHandler.removeCallbacksAndMessages(null);
            }
            if (mIOThread != null) {
                mIOThread.getLooper().quit();
            }
            mContext = null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");
        }
    }

    /**
     * 扫描全部文件
     */
    public void scanAll() {
        String localRootPath = OpenFileUtils.getRootPath();
        if (!TextUtils.isEmpty(localRootPath)) {
            scanStart(localRootPath);
        }
    }


    /**
     * 扫描特定目录下的文件
     */
    public void scanStart(final String path) {
        if (mContext == null || mIOHandler == null) {
            if (mListener != null)
                mListener.error("初始化失败，停止扫描");
            return;
        }
        if (TextUtils.isEmpty(path)) {
            if (mListener != null)
                mListener.error("目录错误，停止扫描");
            return;
        }
        if (mIOHandler == null) {
            if (mListener != null)
                mListener.error("扫描线程错误，停止扫描");
            return;
        }
        scan(path);
    }

    /**
     * 扫描--通过数据集合，平局分配对应的线程任务
     */
    private void scan(final String path) {
        try {
            mIOHandler.removeCallbacksAndMessages(null);
            getDataMap(path).clear();
            File[] files = new File(path).listFiles();
            final List<List<File>> result = splitList(Arrays.asList(files), MAX_THREAD_COUNT);
            //启动多线程进行扫描
            for (int i = 0; i < MAX_THREAD_COUNT; i++) {
                //多线程
                final int finalI = i;
                mIOHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final List<File> cacheFile = result.get(finalI);
                        final List<File> arrList = new ArrayList(cacheFile);
                        while (arrList.size() > 0) {
                            listFile(arrList.get(arrList.size() - 1), path);
                            arrList.remove(arrList.size() - 1);
                        }
                        //已经没有需要扫描的目录
                        mIOHandler.removeCallbacks(this);
                        mThreadCounter.decrementAndGet();
                        if(mThreadCounter.get()==0){
                            //扫描完成
                            mListener.scanResult(getDataMap(path));
                        }
                    }
                });
                Log.d(TAG, "开启线程： " + i);
                mThreadCounter.incrementAndGet();
            }
        } catch (Exception e) {
            if (mListener != null) {
                mListener.scanResult(getDataMap(path));
            }
        }
    }

    /**
     * 获取内存中的数据集合--key--value
     */
    private List<FileInfo> getDataMap(String key) {
        if (TextUtils.isEmpty(key)) {
            return new ArrayList<FileInfo>();
        }
        List<FileInfo> result = mScanResult.get(key);
        if (result == null || result.size() == 0) {
            mScanResult.put(key, new ArrayList<FileInfo>());
        }
        return mScanResult.get(key);
    }

    private void listFile(File file, String originalPath) {
        File[] files = file.listFiles();
        try {
            if (files != null)
                for (File f : files) {
                    if (!f.isDirectory()) {
                        FileSelParams params = FileManager.getInstance().getParams();
                        String format = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
                        if (params != null && params.getMFormat().contains(format)) {
                            FileInfo info = new FileInfo();
                            info.setFileName(f.getName());
                            info.setFilePath(f.getAbsolutePath());
                            info.setFileModifyDate(f.lastModified());
                            info.setFileLength(f.length());
                            info.setFileParentPath(f.getParent());
                            getDataMap(originalPath).add(info);
                            Log.e(TAG, "加入文件路径：" + info.getFileName());
                        }
                    } else if (f.isDirectory()) {
                        //如果是目录，迭代进入该目录
                        listFile(f, originalPath);
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    /**
     * 扫描监听
     */
    public interface ScanFileListener {
        void scanResult(List<FileInfo> result);

        void error(String message);
    }

    public void setScanFileListener(ScanFileListener listener) {
        this.mListener = listener;
    }

}
