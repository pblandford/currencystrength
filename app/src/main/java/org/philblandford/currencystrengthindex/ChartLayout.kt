package org.philblandford.currencystrengthindex

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.Gravity
import android.widget.FrameLayout


class ChartLayout<T>(val error: (String) -> Unit) : AnkoComponent<T> {
  private var periodSpinner: Spinner? = null
  private var sampleSpinner: Spinner? = null

  override fun createView(ui: AnkoContext<T>): View = with(ui) {

    relativeLayout {
      topRow(this).lparams(matchParent, wrapContent){topMargin=20; leftMargin=20}
      frameLayout {
        id = R.id.chart_frame
      }.lparams(matchParent, matchParent, init = { above(R.id.bottom_row);below(R.id.top_row) })
    }
  }

  private fun ViewManager.topRow(parent:ViewGroup) = relativeLayout {


    id = R.id.top_row
    periodSpinner = periodSpinner{refreshChart(parent, context)}
    sampleSpinner = sampleSpinner{refreshChart(parent, context)}.lparams(init = { rightOf(R.id.periods) })

    sampleSpinner?.setSelection(4)

    button(R.string.refresh) {
      onClick {
        refreshChart(parent, context)
      }
    }.lparams(init = { alignParentRight() })
  }

  private fun refreshChart(viewGroup: ViewGroup, context: Context) {
    val chartFrame = viewGroup.find<FrameLayout>(R.id.chart_frame)
    showProgress(chartFrame, context)

    Client.doGetPercentSets(viewGroup.context, getPeriod(), getSample(),
        {
          val chart = CSChartFactory.getChart(viewGroup.context, it)
          chartFrame.removeAllViews()
          chartFrame.addView(chart)
        },
        {
          chartFrame.removeAllViews()
          error(it)
        })
  }

  private fun showProgress(viewGroup: FrameLayout, context: Context) {
    val pb = ProgressBar(context, null, android.R.attr.progressBarStyleLarge)
    val lp = FrameLayout.LayoutParams(100,100, Gravity.CENTER)
    pb.layoutParams = lp
    viewGroup.removeAllViews()
    viewGroup.addView(pb)
  }

  private fun getPeriod(): Period {
    return Period.valueOf(periodSpinner?.selectedItem as String)
  }

  private fun getSample(): Int {
    return (sampleSpinner?.selectedItem as String?)?.toInt() ?: 0
  }
}

