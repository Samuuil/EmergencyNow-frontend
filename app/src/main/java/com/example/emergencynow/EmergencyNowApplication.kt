package com.example.emergencynow

import android.app.Application
import com.example.emergencynow.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class EmergencyNowApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@EmergencyNowApplication)
            modules(appModule)
        }
    }
}
