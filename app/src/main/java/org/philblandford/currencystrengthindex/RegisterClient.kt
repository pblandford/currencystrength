package org.philblandford.currencystrengthindex

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.jetbrains.anko.custom.async

import org.json.JSONArray
import org.json.JSONObject

import java.io.IOException

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by philb on 18/10/14.
 */


object RegisterClient {
    private val TAG = "FBClient"

    fun getRegistrationId(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val registrationId = prefs.getString(context.getString(R.string.pref_regid), "")
        Log.e(TAG, "${context.getString(R.string.pref_regid)} Found registration ID $registrationId")
        return registrationId
    }

    fun getGCMRegistrationId(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val registrationId = prefs.getString(context.getString(R.string.pref_gcm_regid), "")
        Log.e(TAG, "${context.getString(R.string.pref_regid)} Found registration ID $registrationId")
        return if (registrationId.isEmpty()) null else registrationId
    }

    fun storeRegistrationId(context: Context, regid: String?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(context.getString(R.string.pref_regid), regid).apply()
    }

    fun registerInBackground(context: Context, oldRegid: String?) {
        context.async {
            uiThread {
                checkIn(context)
            }
        }
    }

    fun checkIn(context: Context) {
        val regid = getRegistrationId(context)
        if (regid.isEmpty()) {
            Log.e(TAG, "Regid is empty!")
        } else {
            context.doAsync {
                doCheckIn(context, regid, null, listOf())
            }
        }
    }

    private fun doRegister(ctx: Context, oldRegid: String?) {

        try {
            val gcm = GoogleCloudMessaging.getInstance(ctx)
            val regid = gcm?.register(ctx.getString(R.string.gcm_sender_id))
            storeRegistrationId(ctx, regid)
            Log.d(TAG, "Registered with $regid")
        } catch (ex: IOException) {
            Log.e(TAG, "GCM registration Error: ${ex.message}")
        }
    }

    private fun alertsToJSON(alerts: Iterable<Alert>): String {
        val jsonArray = JSONArray()
        for (a in alerts) {
            jsonArray.put(a.toJson())
        }
        return jsonArray.toString()
    }

    /* Check into server with GCM regid */
    private fun doCheckIn(ctx: Context, regid: String, oldRegid: String?,
                          alerts: Iterable<Alert>) {
        val queue = Volley.newRequestQueue(ctx)

        Log.d(TAG, "Check in..")
        val serverUrl = "${ctx.getString(R.string.default_server)}/checkin"

        val postRequest = object : StringRequest(Request.Method.POST, serverUrl,
                Response.Listener<String> { response ->
                    Log.d(TAG, "Response $response")
                },
                Response.ErrorListener {
                    Log.e(TAG, "Error.Response $it", it.cause)
                }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["regid"] = regid
                oldRegid?.let {
                    params["oldregid"] = it
                }
                params["alerts"] = alertsToJSON(alerts)
                return params
            }
        }
        queue.add(postRequest)
    }


}
