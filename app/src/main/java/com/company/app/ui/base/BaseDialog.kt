package com.company.app.ui.base

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.view.Display
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

open class BaseDialog : DialogFragment() {

    override fun onStart() {
        super.onStart()
        setDialogWindowWidth(DIALOG_WINDOW_WIDTH)
    }

    fun setDialogWindowWidth(width: Double) {
        val window = dialog?.window
        val size = Point()
        val display: Display
        window?.let {
            display = window.windowManager.defaultDisplay
            display.getSize(size)
            val maxWidth = size.x.toDouble()
            window.setLayout((maxWidth * width).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        }
    }

    companion object {
        private const val DIALOG_WINDOW_WIDTH = 1.00
    }
}
