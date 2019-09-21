package com.company.app.core.application

import android.app.Application
import com.company.app.BuildConfig
import timber.log.Timber

class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()

    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }


}