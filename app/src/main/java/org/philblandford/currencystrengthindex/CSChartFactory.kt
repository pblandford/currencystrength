package org.philblandford.currencystrengthindex

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import org.achartengine.ChartFactory

import org.achartengine.GraphicalView
import org.achartengine.model.XYMultipleSeriesDataset
import org.achartengine.model.XYSeries
import org.achartengine.renderer.XYMultipleSeriesRenderer
import org.achartengine.renderer.XYSeriesRenderer

/**
 * Created by philb on 22/09/14.
 */
object CSChartFactory {
  private val TAG = "CSChartFactory"

  fun getChart(context: Context, percentSets: Iterable<PercentSet>): GraphicalView {
    val renderer = XYMultipleSeriesRenderer()
    val chartView: GraphicalView
    val dataset = XYMultipleSeriesDataset()

    percentSets.forEach{
      val xySeriesRenderer = getXYSeriesRenderer(it, dataset)
      renderer.addSeriesRenderer(xySeriesRenderer)
    }
    renderer.gridColor = Color.DKGRAY

    val metrics = context.resources.displayMetrics
    val `val` = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, metrics)
    renderer.labelsTextSize = `val`
    renderer.labelsColor = Color.GREEN
    renderer.legendTextSize = `val`

    val flattened = percentSets.flatMap { it.percentages }
    val highest = flattened.max() ?: 0.0
    val lowest = flattened.min() ?: 0.0

    renderer.marginsColor = Color.BLACK
    renderer.isApplyBackgroundColor = true
    renderer.setBackgroundColor(Color.BLACK)
    renderer.setPanEnabled(false, false)
    renderer.setYAxisMax(highest)
    renderer.setYAxisMin(lowest)
    renderer.setShowGrid(true) // we show the grid
    chartView = ChartFactory.getLineChartView(context, dataset, renderer)

    return chartView
  }

  private fun getXYSeriesRenderer(percentSet: PercentSet,
                                  dataset: XYMultipleSeriesDataset): XYSeriesRenderer {

    val xySeries = XYSeries(percentSet.currency.toString())

    val i = 0
    for (pc in percentSet.percentages) {
      xySeries.add(i.toDouble(), pc)
    }
    dataset.addSeries(xySeries)
    val xySeriesRenderer = XYSeriesRenderer()

    xySeriesRenderer.setLineWidth(2f)
    xySeriesRenderer.setColor(currencyColors.get(percentSet.currency) ?: Color.WHITE)
    return xySeriesRenderer
  }
}
