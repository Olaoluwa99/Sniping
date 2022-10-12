package com.easit.sniping.others

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.easit.sniping.R
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class FloatingButton : Service() {

    private lateinit var floatView: ViewGroup
    private lateinit var floatWindowLayoutParams: WindowManager.LayoutParams
    private var layoutType: Int? = null
    private lateinit var windowManager: WindowManager

    private var floatStart_X = -1f
    private  var floatStart_Y = -1f
    private  var floatEnd_X = -1f
    private var floatEnd_Y = -1f

    private lateinit var fullContainer: ConstraintLayout
    private lateinit var closeLayout: ImageView
    private lateinit var snapshotButton: Button

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatView = inflater.inflate(R.layout.float_button, null) as ViewGroup

        fullContainer = floatView.findViewById(R.id.fullContainer)
        snapshotButton = floatView.findViewById(R.id.snapshotButton)
        closeLayout = floatView.findViewById(R.id.closeOverlay)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        //else LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST
        else layoutType = WindowManager.LayoutParams.TYPE_PHONE

        floatWindowLayoutParams = WindowManager.LayoutParams(
            (width * 1F).toInt(),
            (height * 1F).toInt(),
            layoutType!!,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
            /**To try as transparent later*/
        )
        floatWindowLayoutParams.gravity = Gravity.CENTER
        floatWindowLayoutParams.x = 0
        floatWindowLayoutParams.y = 0

        windowManager.addView(floatView, floatWindowLayoutParams)


        closeLayout.setOnClickListener {
            stopSelf()
            windowManager.removeView(floatView)
        }

        floatView.setOnTouchListener(object : View.OnTouchListener {

            val updatedFloatWindowLayoutParam = floatWindowLayoutParams
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0


            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = updatedFloatWindowLayoutParam.x.toDouble()
                        y = updatedFloatWindowLayoutParam.y.toDouble()

                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()

                        floatStart_X = event.x
                        floatStart_Y = event.y
                    }
                    /*
                    MotionEvent.ACTION_MOVE -> {
                        updatedFloatWindowLayoutParam.x = (x + event.rawX - px).toInt()
                        updatedFloatWindowLayoutParam.y = (x + event.rawY - py).toInt()
                        windowManager.updateViewLayout(floatView, updatedFloatWindowLayoutParam)
                    }*/

                    MotionEvent.ACTION_UP -> {
                        floatEnd_X = event.x
                        floatEnd_Y = event.y
                    }
                }
                return false
            }
        })

        snapshotButton.setOnClickListener {
            buttonScreenshotFalse(snapshotButton)
        }

    }
    /*
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            floatStart_X = event.x
            floatStart_Y = event.y
        }
        if (event.action == MotionEvent.ACTION_UP) {
            floatEnd_X = event.x
            floatEnd_Y = event.y
        }
        return super.onTouchEvent(event)
    }*/

    private fun buttonScreenshotFalse(view: View) {
        if (floatStart_X == -1f || floatStart_Y == -1f || floatEnd_X == -1f || floatEnd_Y == -1f) {
            //textView.text = "Dimensions not captured. \n Try Again."
            return
        }
        //textView.text = "Starting ... "
        val view1: View = view.rootView
        val bitmap =
            Bitmap.createBitmap(view1.width, view1.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view1.draw(canvas)

        val bitmapSnippet: Bitmap = Bitmap.createBitmap(
            bitmap,
            (floatStart_X).roundToInt(),
            floatStart_Y.roundToInt(),
            (floatEnd_X - floatStart_X).roundToInt(),
            (floatEnd_Y - floatStart_Y).roundToInt()
        )

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Screenshots",
            "${getCurrentTime()}.jpg"
        )
        try {
            FileOutputStream(file).use { out ->
                bitmapSnippet.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e("STORAGE", getCurrentTime())
        Log.e("STORAGE", file.toString())
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    private fun getCurrentTime(): String {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH.mm.ss.SSS")
        return currentTime.format(formatter)
    }

}