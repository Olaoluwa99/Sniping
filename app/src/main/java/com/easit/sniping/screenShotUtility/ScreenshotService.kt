package com.easit.aiscanner.screenShotUtility

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.TextView
import android.widget.Toast
import com.easit.sniping.MainActivity
import com.easit.sniping.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ScreenshotService : Service(){

    private lateinit var openSnapShot: TextView

    val EXTRA_RESULT_CODE = "resultCode"
    val EXTRA_RESULT_INTENT = "resultIntent"
    private val ACTION_RECORD: String = "com.easit.aiscanner.screenShotUtility.RECORD"
    private val ACTION_SHUTDOWN: String = "com.easit.aiscanner.screenShotUtility.SHUTDOWN"
    private val VIRT_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    val handlerThread = HandlerThread(javaClass.simpleName, Process.THREAD_PRIORITY_BACKGROUND)
    var params: WindowManager.LayoutParams? = null
    var ConParams:WindowManager.LayoutParams? = null
    var li: LayoutInflater? = null
    var myView: View? = null
    var cons: View? = null
    var file: File? = null
    var X = 0
    var Y = 0
    var flag = true
    var windowManager: WindowManager? = null
    private  var constr: WindowManager? = null
    var projection: MediaProjection? = null
    var vdisplay: VirtualDisplay? = null
    var handler: Handler? = null
    var mgr: MediaProjectionManager? = null
    var wmgr: WindowManager? = null
    var resultCode = 0
    var resultData: Intent? = null
    /*
     * TRUE - Full Screen
     * FALSE - CLIP SCREEN
     */
    var width = 0
    var height = 0

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        constr = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

        myView = li?.inflate(R.layout.scanner_v1, null)
        cons = li?.inflate(R.layout.float_button, null)

        openSnapShot = cons!!.findViewById(R.id.snapShotText)
        openSnapShot.setOnClickListener {
            ClipC()
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params!!.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params!!.y = 300
        ConParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        ConParams!!.gravity = Gravity.CENTER
        constr!!.addView(cons, ConParams)
        windowManager!!.addView(myView, params)
        cons?.visibility = View.INVISIBLE
        mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
        wmgr = getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        val display: Display = getWindowManager()!!.defaultDisplay
        val size = Point()
        display.getRealSize(size)

        //For Normal Mobile .
        width = size.x
        height = size.y
        if (MainActivity().DeX) {
            //For Dex Mode .
            width = 1920
            height = 1080
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCapture()
        if (myView != null) {
            windowManager!!.removeView(myView)
            constr?.removeView(cons)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    fun FullC(view: View?) {
        myView!!.visibility = View.INVISIBLE
        cons?.visibility = View.INVISIBLE
        Handler().postDelayed({
            startCapture()
            Toast.makeText(applicationContext, "Screenshot Captured", Toast.LENGTH_SHORT)
                .show()
            stopService(
                Intent(
                    applicationContext,
                    ScreenshotService::class.java
                )
            )
        }, 100)
    }

    //view: View?
    fun ClipC() {
        if (cons?.visibility == View.INVISIBLE) {
            cons?.visibility = View.VISIBLE
            Toast.makeText(this, "Select the Area", Toast.LENGTH_SHORT).show()
            return
        }
        flag = false
        val array = IntArray(2)
        cons?.getLocationOnScreen(array)
        X = array[0]
        Y = array[1]
        myView!!.visibility = View.INVISIBLE
        cons?.visibility = View.INVISIBLE
        Handler().postDelayed({
            startCapture()
            Toast.makeText(applicationContext, "Screenshot Captured", Toast.LENGTH_SHORT)
                .show()
            stopService(
                Intent(
                    applicationContext,
                    ScreenshotService::class.java
                )
            )
        }, 100)
    }

    fun CloseC(view: View?) {
        stopService(
            Intent(
                applicationContext,
                ScreenshotService::class.java
            )
        )
    }

    fun lt(view: View) {
        Toast.makeText(applicationContext, "Slide to resize", Toast.LENGTH_SHORT).show()
        view.setOnTouchListener(object : OnTouchListener {
            var array = IntArray(2)
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.getLocationOnScreen(array)
                        X = array[0]
                        Y = array[1]
                        initialX = X
                        initialY = Y
                        initialTouchX = width.toFloat()
                        initialTouchY = height.toFloat()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        ConParams?.width = (initialX
                                + (event.rawX - initialTouchX).toInt())
                        ConParams?.height = (initialY
                                - (event.rawY - initialTouchY).toInt())
                        if (ConParams!!.height >= 0 && ConParams!!.width >= 0 && ConParams!!.height <= height && ConParams!!.width <= width) constr!!.updateViewLayout(
                            cons,
                            ConParams
                        )
                        return true
                    }
                }
                return false
            }
        })
    }

    fun MoveC(view: View) {
        Toast.makeText(applicationContext, "Movement Unlocked", Toast.LENGTH_SHORT).show()
        view.setOnTouchListener(object : OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = ConParams!!.x
                        initialY = ConParams!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        ConParams?.x = (initialX
                                + (event.rawX - initialTouchX).toInt())
                        ConParams?.y = (initialY
                                + (event.rawY - initialTouchY).toInt())
                        constr?.updateViewLayout(cons, ConParams)
                        return true
                    }
                }
                return false
            }
        })
    }


    override fun onStartCommand(i: Intent, flags: Int, startId: Int): Int {
        if (i.action == null) {
            resultCode = i.getIntExtra(EXTRA_RESULT_CODE, 1337)
            resultData = i.getParcelableExtra(EXTRA_RESULT_INTENT)
        } else if (ACTION_RECORD == i.action) {
            if (resultData != null) {
                startCapture()
            } else {
                val ui: Intent = Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(ui)
            }
        } else if (ACTION_SHUTDOWN == i.action) {
            stopForeground(true)
            stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    @JvmName("getWindowManager1")
    fun getWindowManager(): WindowManager? {
        return wmgr
    }

    @JvmName("getHandler1")
    fun getHandler(): Handler? {
        return handler
    }

    private fun stopCapture() {
        if (projection != null) {
            projection!!.stop()
            vdisplay!!.release()
            projection = null
        }
    }

    private fun startCapture() {
        projection = mgr!!.getMediaProjection(resultCode, resultData!!)
        val it = ImageTransmogrifier()
        val cb: MediaProjection.Callback = object : MediaProjection.Callback() {
            override fun onStop() {
                vdisplay!!.release()
            }
        }
        vdisplay = projection!!.createVirtualDisplay(
            "andshooter",
            it.getWidth(), it.getHeight(),
            resources.displayMetrics.densityDpi,
            VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler
        )
        projection!!.registerCallback(cb, handler)
    }


    fun storeScreenshot(bitmap: Bitmap) {
        var bitmap = bitmap
        stopCapture()
        val sdf = SimpleDateFormat("dd.MM.yy '-' HH:mm:ss")
        val currentDateandTime = sdf.format(Date())
        val filename = "ScreenShot - $currentDateandTime"
        file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Screenshots",
            "$filename.jpg"
        )
        if (!flag) bitmap = Bitmap.createBitmap(bitmap, X, Y, cons!!.width, cons!!.getHeight())
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e("STORAGE", file.toString())
        galleryAddPic()
    }

    private fun galleryAddPic() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }
}