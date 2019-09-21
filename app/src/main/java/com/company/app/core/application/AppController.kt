package com.company.app.core.application

import android.app.Application
import com.company.app.BuildConfig
import es.situm.sdk.SitumSdk
import timber.log.Timber

class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
        initSitum()

    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initSitum() {
        SitumSdk.init(this)
    }


}