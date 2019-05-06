package org.philblandford.currencystrengthindex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.jetbrains.anko.*
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.gms.common.GooglePlayServicesUtil
import org.jetbrains.anko.sdk27.listeners.onClick


class MainActivity : AppCompatActivity() {

  private val TAG = "MAIN"
  private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000


  private enum class Layout {
    CHART, ALERT
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BaseLayout<MainActivity>().setContentView(this)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    supportActionBar?.setIcon(R.drawable.ic_launcher_round)
    registerGCM()
    Billing.init(this)
    loadLayout(Layout.CHART)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.menu_help -> {
        startActivity(Intent(this, HelpActivity::class.java))
      }
      R.id.menu_chart -> {
        loadLayout(Layout.CHART)
      }
      R.id.menu_alerts -> {
        loadLayout(Layout.ALERT)
      }
      else -> return false
    }
    return true
  }

  private fun registerGCM() {
    try {
      RegisterClient.registerInBackground(this, null)
    } catch (e: Exception) {
      Log.e(TAG, "Could not register GCM")
      GooglePlayServicesUtil.getErrorDialog(0, this,
          PLAY_SERVICES_RESOLUTION_REQUEST).show()
    }

  }

  private fun loadLayout(layout: Layout) {
    val error:(String) -> Unit = {
      alert {
        customView {
          frameLayout {
            padding = 20
            textView(it) {
              textSize = dimen(R.dimen.title_text_size).toFloat()
            }.lparams { gravity = Gravity.CENTER }
          }
        }
      }.show()
    }
    val component = when (layout) {
      Layout.CHART -> ChartLayout<Context>(error)
      Layout.ALERT -> AlertLayout<Context>()
    }
    val view = component.createView(AnkoContext.createReusable(this))
    val frame = find<FrameLayout>(R.id.main_frame)
    frame.removeAllViews()
    frame.addView(view)
  }

  inner class BaseLayout<T> : AnkoComponent<T> {
    override fun createView(ui: AnkoContext<T>): View = with(ui) {

      linearLayout {
        orientation = LinearLayout.VERTICAL
        frameLayout {
          id = R.id.main_frame
        }.lparams(matchParent, 0, 5f)
        bottomRow().lparams(matchParent, 0, 1f)
      }
    }

    private fun ViewManager.bottomRow() = relativeLayout {
      button(R.string.donate) {
        onClick {
          purchase()
        }
      }.lparams { alignParentRight() }
    }
  }

  private fun purchase() {
    /* Till I can get these sodding things to use the style I sodding well tell them to use */
    Billing.purchase(this, {
      alert {
        customView {
          frameLayout {
            padding = 20
            textView(R.string.thankyou) {
              textSize = dimen(R.dimen.title_text_size).toFloat()
            }.lparams { gravity = Gravity.CENTER }
          }
        }
      }.show()
    }, {
      alert {
        customView {
          frameLayout {
            padding = 20
            textView(it) {
              textSize = dimen(R.dimen.title_text_size).toFloat()
            }.lparams { gravity = Gravity.CENTER }
          }
        }
      }.show()
    })
  }
}