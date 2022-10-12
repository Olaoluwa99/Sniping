package com.easit.sniping

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.FloatingWindow
import androidx.navigation.ui.AppBarConfiguration
import com.easit.sniping.databinding.ActivityMainBinding
import com.easit.sniping.others.FloatingButton
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var textView: TextView
    private lateinit var button: Button
    private lateinit var mainContainer: ConstraintLayout
    private lateinit var showService: Button


    private val STORAGE_PERMISSION = 0
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084
    private val REQUEST_SCREENSHOT = 59706
    var DeX = false
    private var mgr: MediaProjectionManager? = null


    //private var textView: TextView? = null
    private lateinit var dialog: AlertDialog
    private var floatStart_X = -1f
    private  var floatStart_Y = -1f
    private  var floatEnd_X = -1f
    private var floatEnd_Y = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setSupportActionBar(binding.toolbar)

        textView = binding.textView
        button = binding.button
        mainContainer = binding.mainContainer
        showService = binding.showService

        if (isServiceRunning()){
            stopService(Intent(this, FloatingButton::class.java))
        }

        /*
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)*/


        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )

        showService.setOnClickListener {
            if (checkOverlayPermission()){
                startService(Intent(this, FloatingButton::class.java))
                finish()
            }else{
                requestFloatingWindowPermission()
            }
        }

        button.setOnClickListener {
            buttonScreenshotFalse(button)
        }

        /*
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()
        }*/

        //TODO START
        /*
        val folderMain = "Screenshots"

        val f = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            folderMain
        )
        if (!f.exists()) {
            f.mkdirs()
        }

        val config = resources.configuration
        try {
            val configClass: Class<*> = config.javaClass
            if (configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass) ==
                configClass.getField("semDesktopModeEnabled").getInt(config)
            ) {
                DeX = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager


        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Toast here means there is no permission for storage",
                Toast.LENGTH_LONG
            ).show()
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION)
        } else {
            checkOverlayPermission()
            Toast.makeText(
                this,
                "Storage permission available",
                Toast.LENGTH_LONG
            ).show()
        }*/

    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/


    //TODO START
    /*
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
        } else startService()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startService()
            } else {
                Toast.makeText(
                    this,
                    "Overlay permission is needed to take ScreenShots",
                    Toast.LENGTH_LONG
                ).show()
                finishAndRemoveTask()
            }
        }
        if (requestCode == REQUEST_SCREENSHOT) {
            if (resultCode == RESULT_OK) {
                val i = Intent(this, ScreenshotService::class.java)
                    .putExtra(ScreenshotService().EXTRA_RESULT_CODE, resultCode)
                    .putExtra(ScreenshotService().EXTRA_RESULT_INTENT, data)
                finishAndRemoveTask()
                startService(i)
            } else {
                Toast.makeText(this, "Permission is needed to take ScreenShots", Toast.LENGTH_LONG)
                    .show()
                finishAndRemoveTask()
            }
        }
    }

    private fun startService() {
        startActivityForResult(mgr!!.createScreenCaptureIntent(), REQUEST_SCREENSHOT)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                //checkOverlayPermission()
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkOverlayPermission()
                    return
                } else {
                    //TODO figure out why permission was not granted
                    Toast.makeText(this, "Storage permission is needed to store the ScreenShots", Toast.LENGTH_LONG).show()
                    finishAndRemoveTask()
                }
                return
            }
        }
    }
    */
    //TODO END


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
    }

    private fun buttonScreenshotFalse(view: View) {
        if (floatStart_X == -1f || floatStart_Y == -1f || floatEnd_X == -1f || floatEnd_Y == -1f) {
            textView.text = "Dimensions not captured. \n Try Again."
            return
        }
        textView.text = "Starting ... "
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

    private fun isServiceRunning(): Boolean{
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)){
            if (FloatingWindow::class.java.name == service.service.className){
                return true
            }
        }
        return false
    }

    private fun requestFloatingWindowPermission(){
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Screen overlay permission needed")
        builder.setMessage("Enable 'Display over App from settings")
        builder.setPositiveButton("Open setting", DialogInterface.OnClickListener { dialogInterface, which ->
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivityForResult(intent, RESULT_OK)
        })
        dialog = builder.create()
        dialog.show()
    }

    private fun checkOverlayPermission(): Boolean{
        return if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            Settings.canDrawOverlays(this)
        }
        else return true
    }

}