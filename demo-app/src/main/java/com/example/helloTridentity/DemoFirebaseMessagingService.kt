package com.example.helloTridentity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wibmo.tridentity.sdk.TridentitySDK
import com.wibmo.tridentity_headless.di.FCMRegistrationCallback
import com.wibmo.tridentity_headless.di.UpdateTransactionCallback
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

class DemoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed FCM token: $token")
        saveFcmToken(token)
        registerFcmWithSdk(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}, data: ${remoteMessage.data}")
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification title: ${it.title}, body: ${it.body}")
        }
        if (remoteMessage.data.isNotEmpty()) {
            handleDataPayload(remoteMessage.data)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
            // Show custom notification, launch DemoActivity on tap
            showNotificationAndLaunchActivity(data)

    }

    private fun showNotificationAndLaunchActivity(data: Map<String, String>) {
        val id = notificationId.incrementAndGet()
        val notificationIntent = Intent(this, DemoActivity::class.java).apply {
            putExtra(EXTRA_ACTION, data["action"])
            putExtra(EXTRA_TXN_ID, data["txnId"])
            putExtra(EXTRA_TITLE, data["title"])
            putExtra(EXTRA_DETAIL, data["detail"])
            putExtra(EXTRA_MERCHANT_NAME, data["merchantName"])
            putExtra(EXTRA_AMOUNT, data["amount"])
            putExtra(EXTRA_AUTH_TYPE, data["authType"])
            putExtra(EXTRA_TXN_TIME, data["txnTime"])
            putExtra(EXTRA_EXPIRE_TIME, data["expireTime"])
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            id,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = "tridentity_demo_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(data["title"] ?: "Swipe To Pay")
            .setContentText(data["detail"] ?: "")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data["detail"] ?: ""))
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tridentity Demo",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(id, notificationBuilder.build())
    }



    private fun registerFcmWithSdk(token: String) {
        TridentitySDK.getInstance().registerFCM(
            applicationContext,
            token,
            object : FCMRegistrationCallback {
                override fun onSuccess(event: JSONObject) {
                    Log.d(TAG, "registerFCM onSuccess: $event")
                }
                override fun onSuccess(event: String) {
                    Log.d(TAG, "registerFCM onSuccess: $event")
                }
                override fun onError(code: Int, error: String) {
                    Log.e(TAG, "registerFCM onError: $code - $error")
                }
            }
        )
    }

    private fun saveFcmToken(token: String) {
        getPrefs().edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    private fun getPrefs(): SharedPreferences {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "DemoFCM"
        private const val PREFS_NAME = "demo_app_prefs"
        const val KEY_FCM_TOKEN = "fcm_token"

        private val notificationId = AtomicInteger(0)

        const val EXTRA_ACTION = "action"
        const val EXTRA_TXN_ID = "txnId"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DETAIL = "detail"
        const val EXTRA_MERCHANT_NAME = "merchantName"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_AUTH_TYPE = "authType"
        const val EXTRA_TXN_TIME = "txnTime"
        const val EXTRA_EXPIRE_TIME = "expireTime"

    }
}
