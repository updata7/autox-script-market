package org.autojs.autojs.tool

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.os.Environment
import com.stardust.app.GlobalAppContext
import org.autojs.autojs.App
import org.autojs.autoxjs.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object FileUtils {
    private var context: Application = App.app
    fun copyAssets(srcDir: String, destDir: String) {
        val assetManager: AssetManager = context.assets
        try {
            val files = assetManager.list(srcDir) ?: return
            val outDir = File(destDir)
            if (!outDir.exists()) {
                outDir.mkdirs()
            }
            for (file in files) {
                val srcFile = if (srcDir.isNotEmpty()) "$srcDir/$file" else file
                val destFile = "$destDir/$file"
                if (assetManager.list(srcFile)?.isNotEmpty() == true) {
                    copyAssets(srcFile, destFile)
                } else {
                    copyAssetFile(srcFile, destFile)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun copyAssetFile(srcFile: String, destFile: String) {
        val assetManager: AssetManager = context.assets
        try {
            val inStream: InputStream = assetManager.open(srcFile)
            val outFile = File(destFile)
            if (!outFile.exists()) {
                outFile.createNewFile()
            }
            val outStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inStream.read(buffer).also { length = it } > 0) {
                outStream.write(buffer, 0, length)
            }
            inStream.close()
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun copyDirectory(srcDir: String, destDir: String) {
        MyLog.d("复制文件夹 $srcDir -> $destDir")
        val src = File(srcDir)
        val dest = File(destDir)

        if (src.isDirectory) {
            MyLog.d("是文件夹 src $src")
            if (!dest.exists()) {
                dest.mkdirs()
            }

            src.list()?.forEach { fileName ->
                val srcFile = File(src, fileName)
                val destFile = File(dest, fileName)

                // 递归复制文件和文件夹
                copyDirectory(srcDir + "/" + fileName, destFile.absolutePath)
            }
        } else {
            MyLog.d("是文件 src ${src.path}")
            copyFile(src.path, dest.absolutePath)
        }
    }

    @SuppressLint("CheckResult")
    fun copyFile(source: String, target: String) {
        MyLog.d("复制 $source -> $target")
        val assetManager = context.assets
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = assetManager.open(source)
            val destinationFile = File(target)

            // 创建目标文件夹
            destinationFile.parentFile?.mkdirs()

            outputStream = FileOutputStream(destinationFile)
            val buffer = ByteArray(1024)
            var length: Int

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    fun moveFile(srcPath: String, destDir: String) {
        MyLog.d("移动: $srcPath -> $destDir")
        val sourceFile = File(srcPath)
        val destinationDirectory = File(destDir)

        try {
            // 检查目标目录是否存在，不存在则创建
            if (!destinationDirectory.exists()) {
                if (!destinationDirectory.mkdirs()) {
                    MyLog.e("Failed to create destination directory: ${destinationDirectory.absolutePath}")
                    return
                }
            }

            // 定义目标文件路径
            val destinationFile = File(destinationDirectory, sourceFile.name)

            if (sourceFile.exists()) {
                // 复制文件到目标目录
                sourceFile.copyTo(destinationFile, overwrite = true)

                // 删除源文件
                if (sourceFile.delete()) {
                    MyLog.d("File moved successfully: ${destinationFile.absolutePath}")
                } else {
                    MyLog.e("Failed to delete source file: ${sourceFile.absolutePath}")
                }
            } else {
                MyLog.e("Source file does not exist: ${sourceFile.absolutePath}")
            }
        } catch (e: Exception) {
            MyLog.e("Error moving file: ${e.localizedMessage}")
        }
    }

    fun deleteFile(path: String) {
        val deletedPath = File(path)
        deletedPath.delete()
    }

    fun getDefaultScriptPath(): String {
        return Environment.getExternalStorageDirectory().path +
                GlobalAppContext.getString(R.string.default_value_script_dir_path)
    }
}