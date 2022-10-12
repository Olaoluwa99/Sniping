package com.easit.aiscanner.screenShotUtility

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.ImageReader
import android.view.Surface
import com.easit.sniping.MainActivity

class ImageTransmogrifier : ImageReader.OnImageAvailableListener{

    private var width = 0
    private var height = 0
    private var imageReader: ImageReader? = null
    private var svc: ScreenshotService? = null
    private var latestBitmap: Bitmap? = null

    init {
        val display = svc?.getWindowManager()!!.defaultDisplay
        val size = Point()
        display.getRealSize(size)

        //For Normal Mobile .
        var width = size.x
        var height = size.y
        if (MainActivity().DeX) {
            //For Dex Mode .
            width = 1920
            height = 1080
        }
        this.width = width
        this.height = height
        imageReader = ImageReader.newInstance(
            width, height,
            ImageFormat.JPEG, 2
            //PixelFormat.RGBA_8888
        )
        imageReader!!.setOnImageAvailableListener(this, svc!!.getHandler())
    }

    override fun onImageAvailable(reader: ImageReader?) {
        val image = imageReader!!.acquireLatestImage()
        if (image != null) {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            val bitmapWidth = width + rowPadding / pixelStride
            if (latestBitmap == null || latestBitmap!!.width != bitmapWidth || latestBitmap!!.height != height) {
                if (latestBitmap != null) {
                    latestBitmap!!.recycle()
                }
                latestBitmap = Bitmap.createBitmap(
                    bitmapWidth,
                    height, Bitmap.Config.ARGB_8888
                )
            }
            latestBitmap!!.copyPixelsFromBuffer(buffer)
            if (image != null) {
                image.close()
            }
            val cropped = Bitmap.createBitmap(
                latestBitmap!!, 0, 0,
                width, height
            )
            svc!!.storeScreenshot(cropped)
        }
    }

    fun getSurface(): Surface? {
        return imageReader!!.surface
    }

    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }

    fun close() {
        imageReader!!.close()
    }
}