package org.philblandford.currencystrengthindex

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log

import java.util.ArrayList
import java.util.Date

/**
 * Created by philb on 23/09/14.
 */

class AlertDBObject {
  var period: Period? = null
  var sample: Int = 0
  var threshold: Double = 0.toDouble()
  var lastpair: String? = null
  var lastUpdate: Date? = null
  var lastAlert: Date? = null
  var id: Long = 0

  override fun toString(): String {
    return StringBuilder(period!!.toString()).append(" ").append(sample).append(" ").append(threshold).append(" (").append(lastpair).append(")").toString()
  }

  fun toAlert():Alert {
    return Alert(period!!, sample, threshold, lastUpdate?.time ?: 0, lastpair ?: "")
  }
}


fun getAlerts(ctx:Context):Iterable<Alert> {
  val ad = AlertDAO(ctx)
  val res = ad.alerts.map{it.toAlert()}
  ad.close()
  return res
}

fun storeAlert(ctx: Context, alert: Alert) {
  val ad = AlertDAO(ctx)
  ad.addAlert(alert.period, alert.sample, alert.threshold)
  ad.close()
}

fun deleteAlert(ctx: Context, alert: Alert) {
  val ad = AlertDAO(ctx)
  val victim = ad.alerts.find {it.toAlert().match(alert) }
  victim?.let {
    ad.deleteAlert(it.id)
  }
  ad.close()
}

class AlertDAO(context: Context) {
  private var mDatabase: SQLiteDatabase? = null
  private val mHelper: AlertDBHelper = AlertDBHelper(context)

  val alerts: ArrayList<AlertDBObject>
    get() {
      return getAlertList()
    }

  init {
    mDatabase = mHelper.writableDatabase
  }

  fun close() {
    mHelper.close()
    mDatabase?.close()
  }

  private fun getAlertList():ArrayList<AlertDBObject> {
    val alerts = ArrayList<AlertDBObject>()

    val cursor = mDatabase!!.query(AlertDBHelper.TABLE_ALERTS,
        ALL_COLUMNS, null, null, null, null, null)

    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
      val alertDBObject = cursorToAlert(cursor)
      alerts.add(alertDBObject)
      cursor.moveToNext()
    }
    cursor.close()
    return alerts
  }

  fun addAlert(period: Period, sample: Int, threshold: Double) {
    val contentValues = ContentValues()
    contentValues.put(AlertDBHelper.COLUMN_PERIOD, period.toString())
    contentValues.put(AlertDBHelper.COLUMN_SAMPLE, sample)
    contentValues.put(AlertDBHelper.COLUMN_THRESHOLD, threshold)

    val id = mDatabase!!.insert(AlertDBHelper.TABLE_ALERTS, null, contentValues)

    Log.d(TAG, "New alert inserted with id$id $period $sample $threshold")
    Log.d(TAG, "Now ${getAlertList().size} alerts")
  }

  fun getAlert(period: Period, sample: Int, threshold: Double): AlertDBObject? {
    val contentValues = ContentValues()
    contentValues.put(AlertDBHelper.COLUMN_PERIOD, period.toString())
    contentValues.put(AlertDBHelper.COLUMN_SAMPLE, sample)
    contentValues.put(AlertDBHelper.COLUMN_THRESHOLD, threshold)

    val cursor = mDatabase!!.query(AlertDBHelper.TABLE_ALERTS, ALL_COLUMNS,
        AlertDBHelper.COLUMN_PERIOD + " = ? and " +
            AlertDBHelper.COLUMN_SAMPLE + " = ? and " +
            AlertDBHelper.COLUMN_THRESHOLD + " = ?",
        arrayOf(period.toString(), sample.toString(), threshold.toString()), null, null, null)

    if (cursor.count != 1) {
      Log.e(TAG, "Could not find unique instance with period " + period + " sample " + sample +
          " threshold " + threshold + " (found " + cursor.count + ")")
      return null
    }
    cursor.moveToFirst()

    val alertDBObject = cursorToAlert(cursor)
    cursor.close()

    return alertDBObject
  }


  fun deleteAlert(id: Long) {
    mDatabase!!.delete(AlertDBHelper.TABLE_ALERTS, AlertDBHelper.COLUMN_ID + " = " + id, null)
  }


  private fun cursorToAlert(cursor: Cursor): AlertDBObject {
    val alertDBObject = AlertDBObject()
    alertDBObject.period = Period.valueOf(cursor.getString(0))
    alertDBObject.sample = cursor.getInt(1)
    alertDBObject.threshold = cursor.getDouble(2)
    alertDBObject.lastpair = cursor.getString(3)
    alertDBObject.lastUpdate = Date(cursor.getLong(4))
    alertDBObject.lastAlert = Date(cursor.getLong(5))
    alertDBObject.id = cursor.getLong(6)
    return alertDBObject
  }

  fun setLastPair(id: Long, pair: String) {
    val contentValues = ContentValues()
    contentValues.put(AlertDBHelper.COLUMN_LASTPAIR, pair)
    mDatabase!!.update(AlertDBHelper.TABLE_ALERTS, contentValues,
        AlertDBHelper.COLUMN_ID + "=" + id, null)
  }

  fun setLastUpdate(id: Long, lastUpdate: Date) {
    val contentValues = ContentValues()

    contentValues.put(AlertDBHelper.COLUMN_LASTUPDATE, lastUpdate.time)
    mDatabase!!.update(AlertDBHelper.TABLE_ALERTS, contentValues,
        AlertDBHelper.COLUMN_ID + "=" + id, null)
  }

  fun setLastAlert(id: Long, lastAlert: Date) {
    val contentValues = ContentValues()

    contentValues.put(AlertDBHelper.COLUMN_LASTALERT, lastAlert.time)
    mDatabase!!.update(AlertDBHelper.TABLE_ALERTS, contentValues,
        AlertDBHelper.COLUMN_ID + "=" + id, null)
  }

  companion object {
    private val TAG = "AlertDAO"
    private val ALL_COLUMNS = arrayOf(AlertDBHelper.COLUMN_PERIOD,
        AlertDBHelper.COLUMN_SAMPLE, AlertDBHelper.COLUMN_THRESHOLD,
        AlertDBHelper.COLUMN_LASTPAIR, AlertDBHelper.COLUMN_LASTUPDATE,
        AlertDBHelper.COLUMN_LASTALERT, AlertDBHelper.COLUMN_ID)
  }
}
