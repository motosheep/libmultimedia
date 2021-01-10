package com.north.light.libfilesel.thread

import android.content.ContentResolver
import android.provider.MediaStore
import com.north.light.libfilesel.FileManager
import com.north.light.libfilesel.api.FinishCallback
import com.north.light.libfilesel.bean.FileInfo
import com.north.light.libfilesel.bean.FileScanInfo
import com.north.light.libfilesel.utils.FileScanManager
import java.io.File

/**
 * author:li
 * date:2021/1/10
 * desc:ContentProvider scan runnable
 */
class FileDatabaseScanRunnable : Runnable {
    private val TAG_DATA_BASE_NAME = "TAG_DATA_BASE_NAME"

    var mListener: FinishCallback? = null

    constructor(listener: FinishCallback) {
        this.mListener = listener
        mListener?.init()
    }

    override fun run() {
        try {
            val provider: ContentResolver? =
                FileScanManager.instance.context?.getContentResolver()
            //通过循环查询数据
            //通过循环查询数据
            val columns = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.DATA
            )
//        String select = "(_data LIKE '%.pdf')";
            //        String select = "(_data LIKE '%.pdf')";
            val cursor = provider?.query(
                MediaStore.Files.getContentUri("external"),
                columns, null, null, null
            )
//        Cursor cursor = provider.query(MediaStore.Files.getContentUri("external"),
//                null, null, null, null);
            //        Cursor cursor = provider.query(MediaStore.Files.getContentUri("external"),
//                null, null, null, null);
            var columnIndexOrThrow_DATA = 0
            if (cursor != null) {
                columnIndexOrThrow_DATA =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            }
            if (cursor != null) {
                FileScanInfo.clearMap(TAG_DATA_BASE_NAME)
                while (cursor.moveToNext() && ! FileScanInfo.mStopTAG.get()) {
                    val path = cursor.getString(columnIndexOrThrow_DATA)
                    val cacheFile = File(path)
                    if (!cacheFile.isDirectory) {
                        val params = FileManager.getInstance().getParams() ?: continue
                        val format =
                            cacheFile.name.substring(cacheFile.name.lastIndexOf(".") + 1)
                                .toLowerCase()
                        if (!params.mFormat.contains(format)) {
                            continue
                        }
                        //文件大小判断
                        if (params.mSelMinSize != 0L && params.mSelMinSize > cacheFile.length()) {
                            continue
                        }
                        if (params.mSelMaxSize != 0L && params.mSelMaxSize < cacheFile.length()) {
                            continue
                        }
                        //至此，认为符合条件--加入集合
                        val info = FileInfo()
                        info.fileName = cacheFile.name
                        info.filePath = cacheFile.absolutePath
                        info.fileModifyDate = cacheFile.lastModified()
                        info.fileLength = cacheFile.length()
                        info.fileParentPath = cacheFile.parent
                        FileScanInfo.getDataBaseList().add(info)
                    }
                }
            }
            cursor!!.close()
            mListener?.finish()
        } catch (e: Exception) {
            mListener?.error(e.message)
        }
        mListener = null
    }
}