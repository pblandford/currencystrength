package org.philblandford.currencystrengthindex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Created by philb on 23/09/14.
 */
class AlertDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  override fun onCreate(database: SQLiteDatabase) {
    database.execSQL(DATABASE_CREATE)
  }

  override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
        + newVersion + ", which will destroy all old data")
    database.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTS")
    onCreate(database)
  }

  companion object {
    private val TAG = "AlertDBHelper"

    val TABLE_ALERTS = "alerts"
    val COLUMN_PERIOD = "period"
    val COLUMN_ID = "_id"
    val COLUMN_SAMPLE = "sample"
    val COLUMN_THRESHOLD = "threshold"
    val COLUMN_LASTPAIR = "lastpair"
    val COLUMN_LASTUPDATE = "lastupdate"
    val COLUMN_LASTALERT = "lastalert"


    private val DATABASE_NAME = "alerts.db"
    private val DATABASE_VERSION = 4

    private val DATABASE_CREATE = ("create table "
        + TABLE_ALERTS + "(" + COLUMN_ID
        + " integer primary key autoincrement, " + COLUMN_PERIOD
        + " text not null, " + COLUMN_SAMPLE + " int not null, " +
        COLUMN_THRESHOLD + " double not null, " + COLUMN_LASTPAIR + " text, "
        + COLUMN_LASTUPDATE + " long, " + COLUMN_LASTALERT + " long);")
  }
}
