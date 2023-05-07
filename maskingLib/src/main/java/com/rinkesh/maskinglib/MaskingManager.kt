package com.rinkesh.maskinglib

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.rinkesh.maskinglib.utils.ImageUtils
import java.io.File
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object MaskTest {
    val docList = listOf("Aadhaar", "PAN","DL")
}
class MaskingManager {
    companion object {
        suspend fun processImageForMasking(context: Context, imageUri: Uri): File {
            val firebaseVisionImage =
                InputImage.fromFilePath(context, imageUri)
            val textRecognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            Log.d("MASKING","IMAGE UNDER PROCESS")
            val maskedIndex = getProcessImage(textRecognizer, firebaseVisionImage)
            Log.d("MASKING","IMAGE PROCESS COMPLETE")
            return ImageUtils.startMaskImage(
                context,
                imageUri,
                maskedIndex
            )
        }

        private suspend fun getProcessImage(
            textRecognizer: TextRecognizer,
            firebaseVisionImage: InputImage
        ): ArrayList<Rect> =
            suspendCoroutine { cont ->
                val aadhaarNumberIndex = ArrayList<Rect>()
                textRecognizer.process(firebaseVisionImage)
                    .addOnSuccessListener {
                        it.textBlocks.forEach {
                            it.lines.forEach { line ->
                                val maskRect = ImageUtils.getAadhaarNumber(line)
                                if (maskRect != null) {
                                    aadhaarNumberIndex.add(maskRect)
                                }
                            }
                        }
                        cont.resume(aadhaarNumberIndex)
                    }
            }

    }
}