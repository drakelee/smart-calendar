package com.lee.drake.smartcalendar

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.CalendarContract
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val TAG: String = "MainActivity"
    private val DRAW_OVER_OTHER_APP_PERMISSION = 123
    private lateinit var mFloatingWidgetService: FloatingWidgetService
    private lateinit var mDrawerLayout: DrawerLayout

    private var mBound: Boolean = false
    private val MY_PERMISSION_REQUEST = 333

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FloatingWidgetService.LocalBinder
            mFloatingWidgetService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askForSystemOverlayPermission()
        askForCalendarReadWritePermission()

        mDrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        val bubbleButton: Button = findViewById(R.id.bubble_button)
        if (mBound) {
            bubbleButton.setText(R.string.bubble_button_stop)
        } else {
            bubbleButton.setText(R.string.bubble_button_start)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBubbleClick(view: View) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            val bubbleButton: Button = findViewById(R.id.bubble_button)
            val startBubble = bubbleButton.text == getString(R.string.bubble_button_start)

            if (startBubble) {
                bubbleButton.setText(R.string.bubble_button_stop)
                Intent(this, FloatingWidgetService::class.java).also { intent ->
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
                }
            } else {
                bubbleButton.setText(R.string.bubble_button_start)
//                stopService(Intent(this, FloatingWidgetService::class.java))
                unbindService(mConnection)
                mBound = false
            }
        }
    }

    private fun askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION)
        }
    }

    private fun askForCalendarReadWritePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
                MY_PERMISSION_REQUEST)
        }
    }

    private val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT
    )

    private val PROJECTION_ID_INDEX: Int = 0
    private val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
    private val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
    private val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

    fun onCalendarClick(view: View) {
        getCalendars()
    }

    private fun getCalendars() {
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        try {
            val cur: Cursor = contentResolver.query(uri, EVENT_PROJECTION, null, null, null)
            while (cur.moveToNext()) {
                val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                Log.i(TAG, "ACCOUNT NAME: " + accountName)
            }
        } catch (ex: SecurityException) {
            throw ex
        }
    }
}
