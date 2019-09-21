package com.company.app.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.company.app.ui.dialog.ErrorDialog


abstract class BaseActivity : AppCompatActivity() {

    fun showErrorDialog(message: String, onConfirm: (() -> Unit)? = null) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val fragment = fm.findFragmentByTag(ErrorDialog.TAG)
        if (fragment == null) {
            ft.addToBackStack(null)
            val dialog = ErrorDialog.newInstance(message = message, onConfirm = onConfirm)
            dialog.show(ft, ErrorDialog.TAG)
        }
    }
}