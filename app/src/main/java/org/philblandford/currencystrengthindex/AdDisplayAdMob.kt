package org.philblandford.currencystrengthindex

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*


/**
 * Created by philb on 03/11/14.
 */
object AdDisplayAdMob {
  private val TAG = "ADS"

  fun init(context: Context) {
    val idRes = if (BuildConfig.DEBUG) {
      R.string.testad_id
    } else {
      R.string.admob_id
    }
    MobileAds.initialize(context, context.getString(idRes));

  }

  fun showAd(activity: Activity, adFrame: ViewGroup) {
    val adView = AdView(activity)
    adView.adListener = object : AdListener() {
      override fun onAdFailedToLoad(res: Int) {
        Log.e(TAG, "Failed to load ad $res")
        super.onAdFailedToLoad(res)
      }
    }
    adView.adSize = AdSize.BANNER

    val adRequest = if (BuildConfig.DEBUG) {
      adView.adUnitId = activity.getString(R.string.testad_id)
      AdRequest.Builder().addTestDevice("266113852E8032437CB40B115DCE33AC").build()
    } else{
      adView.adUnitId = activity.getString(R.string.admob_id)
      AdRequest.Builder().build()
    }

    adView.loadAd(adRequest)
    adFrame.removeAllViews()
    adFrame.addView(adView)
  }
}
