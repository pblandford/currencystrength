package org.philblandford.currencystrengthindex

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.billingclient.api.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

object Billing {

  private val TAG = "BILLING"
  private var onComplete: () -> Unit = {}

  private lateinit var billingClient: BillingClient

  fun init(context: Context) {
    Log.d(TAG, "Init billing")

    billingClient = BillingClient.newBuilder(context).setListener(updatedListener).build()
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
        if (billingResponseCode != BillingClient.BillingResponse.OK) {
          Log.e(TAG, "failed $billingResponseCode")
        } else {
          Log.d(TAG, "Setup finished $billingResponseCode")
          query(context)
        }
      }

      override fun onBillingServiceDisconnected() {
        Log.e(TAG, "Service disconnected")
      }
    })
  }


  private fun productId(context: Context): String {
    return if (false) "android.test.purchased" else "donation"
  }

  fun query(context: Context) {
    val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
    val purchasesList = result?.purchasesList
    if (result?.responseCode != 0) {
      Log.e(TAG, "Query purchase failed $0")
    } else if (purchasesList != null && purchasesList.isNotEmpty()) {
      for (purchase in purchasesList) {
        if (purchase?.sku == productId(context)) {
          Log.d(TAG, "Found product $purchase ${purchase.sku}")
          consumePurchase(purchase.purchaseToken)
        } else{
          Log.d(TAG, "No products found")
        }
      }
    }
  }

  private fun consumePurchase(token:String) {
    billingClient.consumeAsync(token) { responseCode, purchaseToken ->
      Log.d(TAG, "Nom nom nom $purchaseToken $responseCode")
    }
  }

  fun purchase(activity: Activity, onComplete: () -> Unit, onFail: (String) -> Unit) {
    val flowParams = BillingFlowParams.newBuilder()
        .setSku(productId(activity))
        .setType(BillingClient.SkuType.INAPP)// SkuType.SUB for subscription
        .build()
    val responseCode = billingClient.launchBillingFlow(activity, flowParams)
    this.onComplete = onComplete
    if (responseCode != BillingClient.BillingResponse.OK) {
      onFail("Got error $responseCode")
    } else {
      Log.d(TAG, "Response OK")
    }
  }


  private val updatedListener = PurchasesUpdatedListener { responseCode, purchases ->
    if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
      for (purchase in purchases) {
        consumePurchase(purchase.purchaseToken)
        onComplete()
      }
    } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
      // Handle an error caused by a user cancelling the purchase flow.
    } else {
      // Handle any other error codes.
    }
  }
}