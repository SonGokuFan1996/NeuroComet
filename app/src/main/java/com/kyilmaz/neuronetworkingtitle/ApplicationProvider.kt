package com.kyilmaz.neuronetworkingtitle

import android.app.Application

/**
 * Lightweight way to provide an Application reference to non-AndroidX-created classes.
 * Call [init] from your Application subclass if you add one.
 */
object ApplicationProvider {
    @Volatile
    var app: Application? = null
        private set

    fun init(application: Application) {
        app = application
    }
}

