package com.north.light.libfilesel.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.north.light.libfilesel.FileManager;
import com.north.light.libfilesel.api.FinishCallback;
import com.north.light.libfilesel.bean.FileInfo;
import com.north.light.libfilesel.bean.FileScanInfo;
import com.north.light.libfilesel.bean.FileSelParams;
import com.north.light.libfilesel.thread.FileScanRunnable;
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


    //全盘扫描----------------------------------------------------------
    //计数器--管理线程数量大小
    private static AtomicInteger mThreadCounter = new AtomicInteger(0);
    private static AtomicInteger mTotalCounter = new AtomicInteger(0);
    private static AtomicBoolean mNewThreadTAG = new AtomicBoolean(true);
    private static AtomicInteger mNumCounter = new AtomicInteger(0);
    //一个List的大小--分割list
    private int MAX_LIST_COUNT = 5;
    private final int THREAD_COUNT = 20;
    //全盘扫描----------------------------------------------------------


    //content provider扫描----------------------------------------------------------


    //content provider扫描----------------------------------------------------------


    private static final class SingleHolder {
        static final FileScanManager mInstance = new FileScanManager();
    }

    public static FileScanManager getInstance() {
        return SingleHolder.mInstance;
    }

    /**
     * 递归扫描本地文件
     */
    @Override
    public void scanLocal() {
        String localRootPath = OpenFileUtils.getRootPath();
        if (!TextUtils.isEmpty(localRootPath)) {
            scanStart(localRootPath);
        }
    }

    /**
     * 扫描content provider
     */
    @Override
    public void scanDatabase() {
        FileScanInfo.setMStopTAG(new AtomicBoolean(false));
        FileThreadManager.getInstance().closeAllExecutors();
        FileThreadManager.getInstance().getCacheExecutors("LOCAL_DATABASE").execute(new Runnable() {
            @Override
            public void run() {
                ContentResolver provider = mContext.getContentResolver();
                //通过循环查询数据
                String[] columns = new String[]{MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.DATE_MODIFIED,
                        MediaStore.Files.FileColumns.DATA};
//        String select = "(_data LIKE '%.pdf')";
                Cursor cursor = provider.query(MediaStore.Files.getContentUri("external"),
                        columns, null, null, null);
//        Cursor cursor = provider.query(MediaStore.Files.getContentUri("external"),
//                null, null, null, null);
                int columnIndexOrThrow_DATA = 0;
                if (cursor != null) {
                    columnIndexOrThrow_DATA = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                }
                if (cursor != null) {
                    FileScanInfo.Companion.clearMap("database");
                    try {
                        while (cursor.moveToNext() && !FileScanInfo.getMStopTAG().get()) {
                            String path = cursor.getString(columnIndexOrThrow_DATA);
                            File cacheFile = new File(path);
                            if (!cacheFile.isDirectory()) {
                                FileSelParams params = FileManager.getInstance().getParams();
                                if (params == null) {
                                    continue;
                                }
                                String format = cacheFile.getName().substring(cacheFile.getName().lastIndexOf(".") + 1).toLowerCase();
                                if (!params.getMFormat().contains(format)) {
                                    continue;
                                }
                                //文件大小判断
                                if (params.getMSelMinSize() != 0L && params.getMSelMinSize() > cacheFile.length()) {
                                    continue;
                                }
                                if (params.getMSelMaxSize() != 0L && params.getMSelMaxSize() < cacheFile.length()) {
                                    continue;
                                }
                                //至此，认为符合条件--加入集合
                                FileInfo info = new FileInfo();
                                info.setFileName(cacheFile.getName());
                                info.setFilePath(cacheFile.getAbsolutePath());
                                info.setFileModifyDate(cacheFile.lastModified());
                                info.setFileLength(cacheFile.length());
                                info.setFileParentPath(cacheFile.getParent());
                                FileScanInfo.Companion.getDataMap("database").add(info);
                            }
                        }
                        if (mListener != null) {
                            mListener.scanResult(FileScanInfo.Companion.getDataMap("database"));
                        }
                    } catch (Exception e) {
                        if (mListener != null) {
                            mListener.error(e.getMessage());
                        }
                    }
                }
                cursor.close();
            }
        });
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
     * 全盘扫描：扫描特定目录下的文件
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
            FileScanInfo.Companion.clearMap(path);
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
                                    new FileScanRunnable(finalList.get(mNumCounter.getAndIncrement()), path, new FinishCallback() {
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
