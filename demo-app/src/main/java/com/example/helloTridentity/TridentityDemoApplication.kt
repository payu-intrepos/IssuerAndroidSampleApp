package com.example.helloTridentity

import android.app.Application
import com.google.firebase.FirebaseApp
import com.wibmo.tridentity.sdk.TridentitySDK

/**
 * Application class for Tridentity SDK Demo.
 * Single, clean demo app for client demonstration - no flavors.
 * Initializes Firebase for push notifications.
 */
class TridentityDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        TridentitySDK.getInstance()
    }
}
