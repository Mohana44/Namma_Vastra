package com.example.namma_vastra

import android.app.Application
import com.google.firebase.FirebaseApp

class NammaVastraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
