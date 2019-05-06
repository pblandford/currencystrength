package org.philblandford.currencystrengthindex

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.jetbrains.anko.*


class StyledAdapter(context: Context, items: List<String>,
                    val theme:Int = R.style.SpinnerColor) : ArrayAdapter<String>(context,
    android.R.layout.simple_list_item_1, items) {

  override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
    return doGetView(position)
  }

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
    return doGetView(position)
  }

  private fun doGetView(position: Int): View {
    return context.UI {
      val text = getItem(position)
      themedTextView(text, theme) {
        padding = 10
      }
    }.view
  }
}

private fun ViewManager.styledSpinner(id:Int, theme:Int = R.style.SpinnerColor,
                                      strings:List<String>,
                                      onChanged:()->Unit)  = spinner {
  this.id = id
  adapter = StyledAdapter(context, strings, theme)
  background = border()
  padding = 0

  onItemSelectedListener = object :AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
      onChanged()
    }
  }
}

fun ViewManager.periodSpinner(theme:Int = R.style.SpinnerColor,
                              onChanged:()->Unit ) = styledSpinner(R.id.periods,
    theme, periodStrings, onChanged)


fun ViewManager.sampleSpinner(theme:Int = R.style.SpinnerColor,
                              onChanged:()->Unit ) = styledSpinner(R.id.samples,
    theme, samples, onChanged)

fun border(): Drawable = GradientDrawable().apply {
  shape = GradientDrawable.RECTANGLE
}
