package com.wintttr.albumapplication

import android.app.Application

class AlbumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AlbumRepository.initialize(this)
    }
}