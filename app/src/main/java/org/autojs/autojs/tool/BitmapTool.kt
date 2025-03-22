package org.autojs.autojs.tool

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Created by Stardust on 2017/4/22.
 * Modified by wilinz on 2022/5/23
 */
object BitmapTool {
    @JvmStatic
    fun scaleBitmap(origin: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return origin.scale(newWidth, newHeight, false)
    }

    @JvmStatic
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        return drawable.toBitmap()
    }

    @JvmStatic
    fun createImageWithInitials(initials: String): ImageBitmap {
        // 设置图像大小
        val size = 100
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 设置背景颜色
        val backgroundPaint = Paint().apply {
            color = Color.LTGRAY // 你可以在这里选择背景颜色
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), backgroundPaint)

        // 设置文字样式
        val textPaint = Paint().apply {
            color = Color.WHITE // 文字颜色
            textSize = 50f // 文字大小
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // 计算文字的位置
        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f - (textPaint.descent() + textPaint.ascent()) / 2)

        // 绘制文字
        canvas.drawText(initials, xPos, yPos, textPaint)

        // 转换为 ImageBitmap 并返回
        return bitmap.asImageBitmap()
    }
}

fun Bitmap.writeTo(file: File) {
    file.outputStream().use { out ->
        compress(Bitmap.CompressFormat.PNG, 100, out)
    }
}

suspend fun saveIcon(context: Context, uri: Uri, file: File): File? {
    return withContext(Dispatchers.IO) {
        file.parentFile?.let { if (!it.exists()) it.mkdirs() }
        try {
            uri.copyTo(context, file)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
