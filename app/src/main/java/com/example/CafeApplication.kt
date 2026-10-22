package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class CafeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app == null) {
                    initExplicit()
                } else {
                    Log.i("CafeApp", "FirebaseApp initialized from default config")
                }
            } else {
                Log.i("CafeApp", "FirebaseApp already initialized: ${FirebaseApp.getInstance().name}")
            }
        } catch (e: Exception) {
            Log.w("CafeApp", "Default FirebaseApp init failed, trying explicit options: ${e.message}")
            initExplicit()
        }
    }

    private fun initExplicit() {
        try {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:686032896219:android:df35736a41046ff9520cfe")
                .setApiKey("AIzaSyAfudWfJUQBYlONgyVs50rRAEadPB4h1E8")
                .setProjectId("cafe00000-dee29")
                .setStorageBucket("cafe00000-dee29.firebasestorage.app")
                .setGcmSenderId("686032896219")
                .build()
            FirebaseApp.initializeApp(this, options)
            Log.i("CafeApp", "FirebaseApp initialized with explicit credentials")
        } catch (e: Exception) {
            Log.e("CafeApp", "Failed to initialize FirebaseApp with explicit options", e)
        }
    }
}
