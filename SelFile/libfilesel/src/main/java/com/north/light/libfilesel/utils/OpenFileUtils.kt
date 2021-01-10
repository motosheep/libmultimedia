package com.north.light.libfilesel.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.UserHandle
import android.text.TextUtils
import java.io.File

object OpenFileUtils {


    /**
     * 获取根目录
     */
    val rootPath: String
        get() = "/sdcard"
}