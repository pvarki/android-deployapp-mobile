package com.pvarki.deployapp

import android.app.Application
import android.content.SharedPreferences
import com.pvarki.deployapp.utils.PreferenceHelper
import com.pvarki.deployapp.utils.PreferenceHelper.approveCode
import com.pvarki.deployapp.utils.PreferenceHelper.inviteCode
import timber.log.Timber

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
        AppPrefs.inviteCode = ""
        AppPrefs.approveCode = ""

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}