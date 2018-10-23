package org.philblandford.currencystrengthindex

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import org.jetbrains.anko.*

class HelpActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    HelpLayout<HelpActivity>().setContentView(this)
  }

  private inner class HelpLayout<T> : AnkoComponent<T> {
    override fun createView(ui: AnkoContext<T>): View = with(ui) {
      frameLayout {
        webView {
          loadUrl("file:///android_asset/help.html")
        }
      }
    }
  }
}

