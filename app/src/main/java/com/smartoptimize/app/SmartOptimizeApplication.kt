package com.smartoptimize.app

import android.app.Application

class SmartOptimizeApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
