package com.pvarki.deployapp

import android.app.Application
import android.content.SharedPreferences
import timber.log.Timber
import com.pvarki.deployapp.utils.PreferenceHelper

class App : Application() {
    companion object {
        lateinit var AppPrefs: SharedPreferences
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppPrefs = PreferenceHelper.customPreference(this, this.packageName)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

        }
    }
}