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


/**
 * Created by philb on 20/10/14.
 */
class AlertBroadcastReceiver : BroadcastReceiver() {
  private var mAlertDAO: AlertDAO? = null

  override fun onReceive(context: Context, intent: Intent) {

    mAlertDAO = AlertDAO(context)

    Log.d(TAG, intent.getStringExtra("pair") + " " + intent.getStringExtra("period"))

    sendAlert(context, intent)

    mAlertDAO?.close()
  }


  private fun sendAlert(context: Context, fromIntent: Intent) {
    val period = Period.valueOf(fromIntent.getStringExtra(
        context.getString(R.string.alert_intent_period)))
    val sample = Integer.parseInt(fromIntent.getStringExtra(
        context.getString(R.string.alert_intent_sample)))
    val threshold = java.lang.Double.parseDouble(fromIntent.getStringExtra(
        context.getString(R.string.alert_intent_threshold)))
    val pair = fromIntent.getStringExtra(context.getString(R.string.alert_intent_pair))

    val alertDBObject = mAlertDAO?.getAlert(period, sample, threshold)
    if (alertDBObject == null) {
      Log.e(TAG, "Received unknown alert! ")
      return
    }

    Log.d(TAG, "Received alert id: " + alertDBObject.id)

    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra(PERIOD_KEY, period)
    intent.putExtra(SAMPLE_KEY, sample)

    val pendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_CANCEL_CURRENT)

    val notificationBuilder = NotificationCompat.Builder(context, "notify_001")
    notificationBuilder.
        setContentTitle("Currency").
        setContentText("$pair $period $sample").
        setSmallIcon(R.drawable.ic_launcher).
        setContentIntent(pendingIntent).
        setAutoCancel(true)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel("notify_001",
          "Currency", NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(channel)
    }

    notificationManager.notify(alertDBObject.id.toInt(), notificationBuilder.build())

    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val r = RingtoneManager.getRingtone(context, notification)
    r.play()

    mAlertDAO?.setLastAlert(alertDBObject.id, Date())
    mAlertDAO?.setLastPair(alertDBObject.id, pair)

  }

  companion object {
    private val TAG = "AlertBroadcastReceiver"
  }
}
