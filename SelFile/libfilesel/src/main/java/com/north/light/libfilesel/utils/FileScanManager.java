package com.north.light.libfilesel.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.north.light.libfilesel.bean.FileInfo;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    //扫描时线程标识
    private Map<String, Runnable> mScanTAG = new HashMap<>();

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
        //开始扫描，递归
        if (mScanTAG.get(path) != null) {
            mIOHandler.removeCallbacks(Objects.requireNonNull(mScanTAG.get(path)));
            mScanTAG.remove(path);
        }
        getDataMap(path).clear();
        Runnable scanRunnable = new Runnable() {
            @Override
            public void run() {
                getDirList(new File(path), path);
                if (mListener != null) {
                    mListener.scanResult(getDataMap(path));
                }
                //扫描完成，移除runnable
                mScanTAG.remove(path);
            }
        };
        mScanTAG.put(path, scanRunnable);
        mIOHandler.post(scanRunnable);
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

    /**
     * 扫描
     */
    private void getDirList(File file, String originalPath) {
        listFile(file, originalPath);
    }

    private void listFile(File file, String originalPath) {
        File[] files = file.listFiles();
        try {
            for (File f : files) {
                if (!f.isDirectory()) {
                    FileInfo info = new FileInfo();
                    info.setFileName(f.getName());
                    info.setFilePath(f.getAbsolutePath());
                    info.setFileModifyDate(f.lastModified());
                    info.setFileLength(f.length());
                    info.setFileParentPath(f.getParent());
                    getDataMap(originalPath).add(info);
                    Log.e(TAG, "文件：" + f.getAbsolutePath() + "\t加入");
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

}
