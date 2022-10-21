package com.example.locationcamera

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*


class Utils {
    fun createBitmapFromLayout(tv: View): Bitmap {
        val spec: Int = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        tv.measure(spec, spec)
        tv.layout(10, 10, tv.measuredWidth, tv.measuredHeight)
        val b = Bitmap.createBitmap(
            tv.measuredWidth, tv.measuredWidth,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        c.translate((-tv.scrollX).toFloat(), (-tv.scrollY).toFloat())
        tv.draw(c)
        return b
    }

    fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap? {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        bmp1.recycle()
        bmp2.recycle()
        return bmOverlay
    }



    fun getImageUri( inImage: Bitmap): Uri {

        val tempFile = File.createTempFile("temprentpk", ".png")
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)
    }

    fun saveImage(finalBitmap: Bitmap, image_name: String) {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root)
        myDir.mkdirs()
        val fname = "Image-$image_name.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        Log.i("LOAD", root + fname)
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bitmapToByte (bitmap: Bitmap) : ByteArray{
        val width = bitmap.getWidth()
        val height = bitmap.getHeight()

        val size: Int = bitmap.getRowBytes() * bitmap.getHeight()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(byteBuffer)

        var byteArray = byteBuffer.array()
        return byteArray
    }

    fun byteToUri(yo : ByteArray) : Uri {
        val buf :ByteArray = yo
        val s  =  "$buf" + "," + "UTF-8"
        val uri : Uri = Uri.parse(s)
        return uri
    }

    fun SaveImage(finalBitmap: Bitmap) {

        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root + "/testappimages")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val OutletFname = "Image-$n.jpg"
        val file = File(myDir, OutletFname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
//            imagePath = file.absolutePath
            out.flush()
            out.close()


        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    fun saveToGallery(context: Context, bitmap: Bitmap, albumName: String) {
        val filename = "${System.currentTimeMillis()}.png"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$albumName")
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }


    fun bitmapOverlayToCenter(bitmap1: Bitmap, overlayBitmap: Bitmap): Bitmap? {
        val bitmap1Width = bitmap1.width
        val bitmap1Height = bitmap1.height
        val bitmap2Width = overlayBitmap.width
        val bitmap2Height = overlayBitmap.height
        val marginLeft = (bitmap1Width * 0.5 - bitmap2Width * 0.5).toFloat()
        val marginTop = (bitmap1Height * 0.5 - bitmap2Height * 0.5).toFloat()
        val finalBitmap = Bitmap.createBitmap(bitmap1Width, bitmap1Height, bitmap1.config)
        val canvas = Canvas(finalBitmap)
        canvas.drawBitmap(bitmap1, Matrix(), null)
        canvas.drawBitmap(overlayBitmap, marginLeft, marginTop, null)
        return finalBitmap
    }

//NOt working
     fun mergeBitmap(src: Bitmap?, watermark: Bitmap): Bitmap? {
        if (src == null) {
            return null
        }
        val w = src.width
        val h = src.height
        val newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cv = Canvas(newb)

        // draw src into canvas
        cv.drawBitmap(src, 0f, 0f, null)

        // draw watermark into
        cv.drawBitmap(watermark, null, Rect(9, 25, 154, 245), null)

        // save all clip
        cv.save()

        // store
        cv.restore()
        return newb
    }


     fun mark(src: Bitmap, watermark: Bitmap): Bitmap {
        val w = src.width
        val h = src.height

        val result = Bitmap.createBitmap(w, h, src.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)
        val paint = Paint()
        paint.color = Color.RED
        paint.textSize = 10f
        paint.isAntiAlias = true
        paint.isUnderlineText = true
//       val yo : Bitmap? =   watermark.getResizedBitmap(watermark,watermark.width*20,watermark.height*20)
        canvas.drawBitmap(watermark, 10f, 2800f, paint)
        return result
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }
    fun Bitmap.getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }
}

