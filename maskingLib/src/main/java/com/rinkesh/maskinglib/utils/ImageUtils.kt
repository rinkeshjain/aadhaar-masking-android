package com.rinkesh.maskinglib.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.mlkit.vision.text.Text
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.regex.Pattern

class ImageUtils {
    companion object {
        fun getAadhaarNumber(line: Text.Line): Rect? {
            val regex =
                Pattern.compile("^([0-9]{3,4}).([0-9]{3,4}).([0-9]{3,4})", Pattern.MULTILINE)
            val n = regex.matcher(line.text.trim())
            while (n.find()) {
                return getMaskRect(line)
            }
            return null
        }

        private fun getMaskRect(line: Text.Line): Rect? {
            if (line.elements.size > 2) {
                val left = line.elements[0].boundingBox?.left!!
                val top = line.elements[0].boundingBox?.top!!
                val right = line.elements[1].boundingBox?.right!!
                val bottom = line.elements[1].boundingBox?.bottom!!
                return Rect(left, top, right, bottom)
            }
            return null
        }

        fun startMaskImage(
            context: Context?,
            fileUri: Uri?,
            maskRectangle: ArrayList<Rect>?
        ): File {
            Log.d("MASKING","IMAGE MASKING START")
            val tempImage = getBitmapFromUri(context, fileUri!!)
            val result = Bitmap.createBitmap(tempImage.width, tempImage.height, tempImage.config)
            val tempCanvas = Canvas(result)
            tempCanvas.drawBitmap(
                tempImage,
                null,
                Rect(0, 0, tempImage.width, tempImage.height),
                null
            )

            val paint = Paint()
            paint.color = Color.BLACK
            paint.isAntiAlias = true
            paint.isUnderlineText = false
            maskRectangle?.forEach {
                tempCanvas.drawRect(it, paint)
            }
            val mFolder = getParentFolder(context)
            val filename = System.currentTimeMillis()
            Log.d("MASKING","IMAGE FILE CREATE START")
            val imgFile = File(mFolder.absolutePath + "/$filename.png")
            val fOut = FileOutputStream(imgFile)
            result.compress(Bitmap.CompressFormat.JPEG, 70, fOut)
            Log.d("MASKING","IMAGE FILE CREATE COMPLETE")
            fOut.flush() // Not really required
            fOut.close()
            Log.d("MASKING","IMAGE MASKING COMPLETE")
            return imgFile
        }

        private fun getParentFolder(context: Context?): File {
            val mFolder = File("${context?.filesDir}/masking")
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }
            return mFolder
        }


        private fun getBitmapFromUri(context: Context?, selectedPhotoUri: Uri): Bitmap {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context?.contentResolver!!, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGB_565, false)
            } else {
                MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedPhotoUri,)
            }
        }
    }

}