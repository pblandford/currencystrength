package org.philblandford.currencystrengthindex

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Point
import android.support.annotation.IdRes
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.*
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.themedAlertDialogLayout
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener


class AlertLayout<T> : AnkoComponent<T> {

  private var periodSpinner: Spinner? = null
  private var sampleSpinner: Spinner? = null
  private var seekBar: SeekBar? = null
  private var listView: ListView? = null
  private var cWidth = 0
  private val TAG = "ALERT"

  override fun createView(ui: AnkoContext<T>): View = with(ui) {

    cWidth = dimen(R.dimen.listColumnWidth)
    linearLayout {
      orientation = LinearLayout.VERTICAL
      padding = 10
      topRow().lparams(matchParent, wrapContent)
      headers().lparams { margin = dimen(R.dimen.listMargin) }
      listView = alertListView().lparams(matchParent, matchParent) { leftMargin = dimen(R.dimen.listMargin) }
    }
  }

  private fun requestAlert(context: Context, onComplete: () -> Unit) {
    val period = Period.valueOf(periodSpinner?.selectedItem as String)
    val sample = (sampleSpinner?.selectedItem as String).toInt()
    val threshold = ((seekBar?.progress)?.toDouble() ?: 100.0) / 10
    AlertPoster.requestAlert(context, Action.ADD, Alert(period, sample, threshold),
        {
          onComplete()
        },
        {
          context.alert(it) { }.show()
        })
  }

  private fun ViewManager.topRow() = relativeLayout {
    textView(R.string.title_alerts) { textSize = dimen(R.dimen.title_text_size).toFloat(); }
    imageView(R.drawable.ic_menu_add) {
      onClick {
        lateinit var dialog: DialogInterface
        dialog = context.alert {
          customView {
            alertAddDialog() { dialog.dismiss(); refresh(context) }
          }
        }.show()

      }
    }.lparams(dimen(R.dimen.icon_height),
        dimen(R.dimen.icon_height), init = { alignParentEnd() })
  }

  private fun ViewManager.headers() = linearLayout {
    listOf(R.string.period_desc, R.string.sample_desc, R.string.threshold_desc).map {
      textView(it).lparams(cWidth, wrapContent)
    }
  }

  private fun ViewManager.alertListView() = listView {
    adapter = getAdapter(context)
  }

  private fun refresh(context: Context) {
    val adapter = getAdapter(context)
    listView?.adapter = adapter
    adapter.notifyDataSetChanged()
  }

  private fun getAdapter(context: Context): AlertAdapter {
    return AlertAdapter(context, getAlerts(context).toList().toTypedArray())
  }

  private inner class AlertAdapter(ctx: Context, alerts: Array<Alert>) : ArrayAdapter<Alert>(ctx,
      android.R.layout.simple_list_item_1, alerts) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
      val alert = getItem(position)
      return context.UI {
        frameLayout {
          topPadding = dimen(R.dimen.listMargin)
          relativeLayout {
            linearLayout {
              listRowText(alert?.period.toString()).lparams(cWidth)
              listRowText(alert?.sample.toString()).lparams(cWidth)
              listRowText(alert?.threshold.toString()).lparams(cWidth)
            }
            imageView(R.drawable.ic_menu_delete) {
              onClick {
                context.alert {
                  customView {
                    frameLayout {
                      textView(R.string.alert_delete_confirm) {
                        textSize = dimen(R.dimen.title_text_size).toFloat()
                      }.lparams{gravity = Gravity.CENTER}
                    }
                  }
                  yesButton {
                    AlertPoster.requestAlert(context, Action.DELETE, alert, {
                      refresh(ctx)
                    }, {
                      Log.e(TAG, "Failed deleting alert $it")
                      deleteAlert(ctx, alert)
                      refresh(context)
                    })
                  }
                  noButton { }
                }.show()
              }
            }.lparams(dimen(R.dimen.icon_height),
                dimen(R.dimen.icon_height)) {
              alignParentRight()
            }
          }.lparams(matchParent, wrapContent)
        }
      }.view
    }

    private fun ViewManager.listRowText(str: String) = textView(str) {
      textSize = dimen(R.dimen.listRowTextSize).toFloat()
    }
  }

  private fun ViewManager.alertAddDialog(onComplete: () -> Unit) = frameLayout {
    val lineGap = 60
    themedRelativeLayout(R.style.DialogTheme) {
      padding = 10
      labelText(R.string.period_desc, R.id.period_text)
      periodSpinner = periodSpinner(R.style.DialogSpinner) {}.lparams { alignParentEnd(); alignTop(R.id.period_text) }
      labelText(R.string.sample_desc, R.id.sample_text).lparams { below(R.id.periods); topMargin = lineGap }
      sampleSpinner = sampleSpinner(R.style.DialogSpinner) {}.lparams { alignStart(R.id.periods); alignTop(R.id.sample_text) }
      labelText(R.string.threshold_desc, R.id.threshold_text).lparams { below(R.id.samples); topMargin = lineGap }
      val lt = labelText("1.0").lparams { alignStart(R.id.samples); alignTop(R.id.threshold_text) }
      seekBar = seekBar {
        id = R.id.progress_horizontal
        progress = 10
        onSeekBarChangeListener {
          onProgressChanged { _, i, _ ->
            lt.text = getThreshold(i).toString()
          }
        }
      }.lparams(matchParent) { below(R.id.threshold_text); topMargin = lineGap }
      button(R.string.add) {
        onClick { requestAlert(context) { onComplete() } }
      }.lparams { below(R.id.progress_horizontal);alignParentEnd() }
      sampleSpinner?.setSelection(4)
    }.lparams((getDialogWidth(context) * 0.7).toInt())
  }

  private fun getThreshold(percent: Int): Float {
    return percent.toFloat() / 10
  }

  private fun getDialogWidth(ctx: Context): Int {
    val display = ctx.windowManager.getDefaultDisplay()
    val size = Point()
    display.getSize(size)
    return size.x
  }

  private fun ViewManager.labelText(string: String) = textView(string) {
    this.id = id
    textSize = dimen(R.dimen.title_text_size).toFloat()
  }

  private fun ViewManager.labelText(res: Int, id: Int) = textView(res) {
    this.id = id
    textSize = dimen(R.dimen.title_text_size).toFloat()
  }

  private fun RelativeLayout.LayoutParams.alignTop(@IdRes id: Int): Unit = addRule(RelativeLayout.ALIGN_TOP, id)

}





