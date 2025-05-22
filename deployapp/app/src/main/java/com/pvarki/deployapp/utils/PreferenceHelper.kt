package com.pvarki.deployapp.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    const val REST_API_BASE_URL = "REST_API_BASE_URL"
    const val REST_API_BASE_URL_DEFAULT_VALUE = "https://localmaeher.dev.pvarki.fi:4439"

    fun customPreference(context: Context, name: String): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    var SharedPreferences.restApiBaseUrl
        get() = getString(REST_API_BASE_URL, REST_API_BASE_URL_DEFAULT_VALUE)
            ?: "" // can not return null....
        set(value) {
            edit().putString(REST_API_BASE_URL, value).apply()
        }

}