package com.jlj.kuiklyperspectivecropui

import android.app.Application
import com.jlj.kuiklybase.perspectivecrop.PerspectiveCropAndroid

class KRApplication : Application() {

    init {
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        PerspectiveCropAndroid.init(this)
    }

    companion object {
        lateinit var application: Application
    }
}