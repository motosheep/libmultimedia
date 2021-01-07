package com.north.light.libfilesel.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.north.light.libfilesel.FileManager;
import com.north.light.libfilesel.api.FinishCallback;
import com.north.light.libfilesel.bean.FileInfo;
import com.north.light.libfilesel.bean.FileScanInfo;
import com.north.light.libfilesel.bean.FileSelParams;
import com.north.light.libfilesel.thread.FileThreadManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lzt
 * time 2021/1/4
 * 描述：文件扫描工具类
 * <p>
 * 使用：---
 * 1、init(this)
 * 2、监听
 * 3、扫描
 * 4、移除监听
 * 5、release（）
 */
public class FileScanManager implements Serializable, FileScanManagerInterface {
    private static final String TAG = FileScanManager.class.getSimpleName();
    private Context mContext;
    //监听
    private ScanFileListener mListener;
    //一个List的大小--分割list
    private int MAX_LIST_COUNT = 5;

    //计数器--管理线程数量大小
    private static AtomicInteger mThreadCounter = new AtomicInteger(0);
    private static AtomicInteger mTotalCounter = new AtomicInteger(0);
    private static AtomicBoolean mNewThreadTAG = new AtomicBoolean(true);
    private static AtomicInteger mNumCounter = new AtomicInteger(0);
    private final int THREAD_COUNT = 20;

    private static final class SingleHolder {
        static final FileScanManager mInstance = new FileScanManager();
    }

    public static FileScanManager getInstance() {
        return SingleHolder.mInstance;
    }

    @Override
    public void scanLocal() {
        String localRootPath = OpenFileUtils.getRootPath();
        if (!TextUtils.isEmpty(localRootPath)) {
            scanStart(localRootPath);
        }
    }

    @Override
    public void scanDatabase() {

    }


    /**
     * 初始化
     */
    @Override
    public void init(@NotNull Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
    }

    /**
     * 释放
     */
    @Override
    public void release() {
        try {
            FileScanInfo.setMStopTAG(new AtomicBoolean(true));
            FileThreadManager.getInstance().closeAllExecutors();
            mContext = null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");
        }
    }


    /**
     * 扫描特定目录下的文件
     */
    private void scanStart(final String path) {
        if (mContext == null) {
            if (mListener != null)
                mListener.error("初始化失败，停止扫描");
            return;
        }
        if (TextUtils.isEmpty(path)) {
            if (mListener != null)
                mListener.error("目录错误，停止扫描");
            return;
        }
        //扫描--通过数据集合，平局分配对应的线程任务
        try {
            FileScanInfo.setMStopTAG(new AtomicBoolean(false));
            FileThreadManager.getInstance().closeAllExecutors();
            FileScanInfo.Companion.getDataMap(path).clear();
            File[] files = new File(path).listFiles();
            final List<List<File>> result = ListSpilt.splitList(Arrays.asList(files), MAX_LIST_COUNT);
            final List<List<File>> finalList = new ArrayList(result);
            mTotalCounter.set(0);
            mThreadCounter.set(0);
            mNumCounter.set(0);
            mNewThreadTAG.set(true);
            Log.e(TAG, "cachePos开始---");
            FileThreadManager.getInstance().getCacheExecutors("SCAN_PARENT").execute(new Runnable() {
                @Override
                public void run() {
                    while (!FileScanInfo.getMStopTAG().get()) {
                        if (mThreadCounter.get() < THREAD_COUNT) {
                            if (!mNewThreadTAG.get()) {
                                continue;
                            }
                            mNewThreadTAG.set(false);
                            mThreadCounter.incrementAndGet();
                            if (mNumCounter.get() == finalList.size()) {
                                continue;
                            }
                            FileThreadManager.getInstance().getAutoCacheExecutors(mNumCounter.get(),
                                    new ScanRunnable(finalList.get(mNumCounter.getAndIncrement()), path, new FinishCallback() {
                                        @Override
                                        public void finish() {
                                            mThreadCounter.decrementAndGet();
                                            mTotalCounter.incrementAndGet();
                                            //执行到最后的判断
                                            if (mTotalCounter.get() == finalList.size()) {
                                                //至此，全部扫描完毕
                                                if (mListener != null) {
                                                    mListener.scanResult(FileScanInfo.Companion.getDataMap(path));
                                                }
                                                Log.e(TAG, "cachePos扫描完---");
                                            }
                                        }

                                        @Override
                                        public void init() {
                                            //已经初始化完成
                                            mNewThreadTAG.set(true);
                                        }
                                    }));
                        }
                    }
                }
            });
        } catch (Exception e) {
            if (mListener != null) {
                mListener.scanResult(FileScanInfo.Companion.getDataMap(path));
            }
        }
    }

    /**
     * handler runnable
     * 弱引用--leak memory
     */
    private class ScanRunnable implements Runnable {
        private List<File> scanPath;
        private String path;
        private FinishCallback listener;

        public ScanRunnable(List<File> file, String path, FinishCallback callback) {
            this.scanPath = file;
            this.path = path;
            this.listener = callback;
            this.listener.init();
        }

        @Override
        public void run() {
            for (File file : scanPath) {
                if (FileScanInfo.getMStopTAG().get()) {
                    break;
                }
                listFile(file, path);
            }
            if (this.listener != null) {
                listener.finish();
            }
            listener = null;
        }

    }

    /**
     * list file递归
     * 使用递归标识控制
     */
    private static void listFile(File file, String originalPath) {
        if (FileScanInfo.getMStopTAG().get()) {
            return;
        }
        File[] files = file.listFiles();
        try {
            if (files != null)
                for (File f : files) {
                    if (!f.isDirectory()) {
                        FileSelParams params = FileManager.getInstance().getParams();
                        String format = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
                        if (params != null) {
                            //格式判断
                            if (!params.getMFormat().contains(format)) {
                                continue;
                            }
                            //文件大小判断
                            if (params.getMSelMinSize() != 0 && params.getMSelMinSize() > f.length()) {
                                continue;
                            }
                            if (params.getMSelMaxSize() != 0 && params.getMSelMaxSize() < f.length()) {
                                continue;
                            }
                            FileInfo info = new FileInfo();
                            info.setFileName(f.getName());
                            info.setFilePath(f.getAbsolutePath());
                            info.setFileModifyDate(f.lastModified());
                            info.setFileLength(f.length());
                            info.setFileParentPath(f.getParent());
                            FileScanInfo.Companion.getDataMap(originalPath).add(info);
                            Log.d(TAG, "加入文件路径：" + info.getFileName());
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
     * 扫描监听
     */
    public interface ScanFileListener {
        void scanResult(List<FileInfo> result);

        void error(String message);
    }

    public void setScanFileListener(ScanFileListener listener) {
        this.mListener = listener;
    }

    public void removeScanFileListener() {
        this.mListener = null;
    }

}
