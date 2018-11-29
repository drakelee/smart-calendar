package com.lee.drake.smartcalendar

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.support.design.widget.FloatingActionButton
import android.view.*
import org.jetbrains.annotations.Nullable

class FloatingWidgetService: Service() {

    private lateinit var mWindowManager: WindowManager
    private lateinit var mOverlayView: View
    private val mBinder = LocalBinder()

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()

        setTheme(R.style.AppTheme)

        mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

        params.gravity = Gravity.TOP or Gravity.END
        params.x = 0
        params.y = 100

        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager.addView(mOverlayView, params)

        val fab = mOverlayView.findViewById(R.id.overlay_fab) as FloatingActionButton
        fab.setOnTouchListener(object: View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.0F
            private var initialTouchY: Float = 0.0F

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y

                        initialTouchX = event.rawX
                        initialTouchY = event.rawY

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // ADD MORE

                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val xDiff: Float = event.rawX - initialTouchX
                        val yDiff: Float = event.rawY - initialTouchY

                        params.x = initialX - xDiff.toInt()
                        params.y = initialY + yDiff.toInt()

                        mWindowManager.updateViewLayout(mOverlayView, params)

                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOverlayView != null) {
            mWindowManager.removeView(mOverlayView)
        }
    }

    inner class LocalBinder: Binder() {
        fun getService() : FloatingWidgetService = this@FloatingWidgetService
    }
}