package org.philblandford.currencystrengthindex

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

import java.util.Date
import android.app.NotificationChannel
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


/**
 * Created by philb on 20/10/14.
 */

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String?) {
        Log.e("FBASE", "Received token $token")
        val gcmId = RegisterClient.getGCMRegistrationId(baseContext)
        RegisterClient.storeRegistrationId(baseContext, token)
        RegisterClient.registerInBackground(baseContext, gcmId)
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        Log.e("FBASE", "Got message ${message?.data}")


        val alertDAO = AlertDAO(baseContext)

        message?.data?.get("period")?.let { period ->
            message.data.get("threshold")?.let { threshold ->
                message.data.get("sample")?.let { sample ->
                    message.data.get("lastPair")?.let { pair ->
                        sendAlert(baseContext, Period.valueOf(period),
                                sample.toInt(), threshold.toDouble(), pair, alertDAO)
                    }
                }
            }
        }
        alertDAO.close()
    }


    private fun sendAlert(context: Context, period:Period, sample:Int, threshold:Double, pair:String, alertDAO: AlertDAO) {

        val alertDBObject = alertDAO.getAlert(period, sample, threshold)
        if (alertDBObject == null) {
            Log.e(TAG, "Received unknown alert! ")
     //       return
        }

     //   Log.d(TAG, "Received alert id: " + alertDBObject.id)

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(PERIOD_KEY, period)
        intent.putExtra(SAMPLE_KEY, sample)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(context, "notify_001")
        notificationBuilder.setContentTitle("Currency").setContentText("$pair $period $sample").setSmallIcon(R.drawable.ic_launcher).setContentIntent(pendingIntent).setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("notify_001",
                    "Currency", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

       // notificationManager.notify(alertDBObject.id.toInt(), notificationBuilder.build())
        notificationManager.notify(0, notificationBuilder.build())

      //  alertDAO.setLastAlert(alertDBObject.id, Date())
      //  alertDAO.setLastPair(alertDBObject.id, pair)

    }

    companion object {
        private val TAG = "AlertBroadcastReceiver"
    }

}