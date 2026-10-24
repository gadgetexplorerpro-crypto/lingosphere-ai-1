package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class LingoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    try {
      if (FirebaseApp.getApps(this).isEmpty()) {
        FirebaseApp.initializeApp(this)
        Log.i("LingoApplication", "FirebaseApp initialized in Application.onCreate")
      }
    } catch (e: Exception) {
      Log.e("LingoApplication", "Error during FirebaseApp initialization", e)
    }
  }
}
