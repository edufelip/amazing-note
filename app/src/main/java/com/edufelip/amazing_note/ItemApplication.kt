package com.edufelip.amazing_note

import android.app.Application
import com.edufelip.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class ItemApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ItemApplication)
        }
    }
}
