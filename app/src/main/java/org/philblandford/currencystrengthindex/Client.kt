package org.philblandford.currencystrengthindex

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.Volley

import org.json.JSONArray
import org.json.JSONException

import java.util.ArrayList
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest



/**
 * Created by philb on 22/09/14.
 */
object Client {
  private val TAG = "CLIENT"

  internal fun doGetPercentSets(context: Context,
                                period: Period, sample: Int,
                                onComplete:(Iterable<PercentSet>) -> Unit,
                                onError:(String) -> Unit) {
    val serverUrl = context.getString(R.string.default_server)
    val url = "$serverUrl/percentages/${period}/${sample}"
    Log.v(TAG, url)

    val queue = Volley.newRequestQueue(context)
    val stringRequest = StringRequest(Request.Method.GET, url,
        Response.Listener<String> {
          try {
            val sets = getPercentSets(it, sample)
            onComplete(sets)
          } catch (e:Exception) {
            Log.e(TAG, "Failed getting percentages", e)
            onError(e.message ?: "Unknown error")
          }
        },
        Response.ErrorListener {
          if (it is TimeoutError) {
            onError(context.getString(R.string.timeout))
          } else {
            Log.e(TAG, "Failed getting percentages ${it.message}")
            onError(it.message ?: it.toString())
          }
        })
    stringRequest.setShouldCache(false)
    queue.add(stringRequest)
  }

  private fun getPercentSets(json:String, sample:Int):Iterable<PercentSet> {
    val jsonArray = JSONArray(json)
    if (jsonArray.length() != Currency.values().size) {
      throw JSONException("Expected ${Currency.values().size} percent sets")
    }

    return (0 until jsonArray.length()).map {
      val jsonObject = jsonArray.getJSONObject(it)

      val percentagesJson = jsonObject.getJSONArray("percentages")
      if (percentagesJson.length() != sample) {
        throw JSONException("Expected ${sample} quotes, received ${percentagesJson.length()}")
      }
      val percentages = (0 until percentagesJson.length()).map {
        percentagesJson.getDouble(it)
      }
      PercentSet(Currency.valueOf(jsonObject.getString("currency")), percentages)
    }
  }

}
