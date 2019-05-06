package org.philblandford.currencystrengthindex

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.RequestQueue
import org.jetbrains.anko.defaultSharedPreferences
import org.json.JSONArray
import org.json.JSONObject

enum class Action {
  ADD, DELETE
}

data class Alert(val period:Period, val sample:Int, val threshold:Double,
                 val lastAlert:Long = 0L, val lastPair:String? = null) {
  fun toJson():JSONObject {
    val jsonObject = JSONObject()
    jsonObject.put("period", period)
    jsonObject.put("sample", sample)
    jsonObject.put("threshold", threshold)
    return jsonObject
  }

  fun match(other:Alert):Boolean {
    return other.sample == sample && other.period == period && other.threshold == threshold
  }
}

object AlertPoster {
  private val TAG = "Alert"

  fun requestAlert(ctx:Context, action:Action, alert: Alert,
                   onComplete:()->Unit,
                   onError:(String) -> Unit) {

    val registrationId = RegisterClient.getRegistrationId(ctx)
    if (registrationId.isEmpty()) {
      onError("Cannot create alert until registered with Google Cloud Messaging")
    }

    val addUrl = ctx.getString(if (action == Action.ADD) R.string.url_add else R.string.url_delete)
    val queue = Volley.newRequestQueue(ctx)

    val fullUrl = "${ctx.getString(R.string.default_server)}/$addUrl"
    val postRequest = object : StringRequest(Request.Method.POST, fullUrl,
        Response.Listener<String> { response ->
          if (action == Action.ADD) {
            storeAlert(ctx, alert)
          } else {
            deleteAlert(ctx, alert)
          }
          Log.d(TAG,"Response $response")
          onComplete()
        },
        Response.ErrorListener {
          Log.e(TAG,"Error.Response $it", it.cause)
          onError(it.message ?: "Unknown error")
        }
    ) {
      override fun getParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["regid"] = registrationId
        params["period"] = alert.period.toString()
        params["sample"] = alert.sample.toString()
        params["threshold"] = alert.threshold.toString()

        return params
      }
    }
    postRequest.setShouldCache(false)
    queue.add(postRequest)
  }
}
