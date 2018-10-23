package org.philblandford.currencystrengthindex

import android.graphics.Color

enum class Period {
  M1, M5, M15, M30, H1, H4, D1, W1, MN1
}

enum class Currency {
  USD, EUR, GBP, AUD, NZD, JPY, CHF, CAD


}

val PERIOD_KEY = "period"
val SAMPLE_KEY = "sample"

val currencyColors = hashMapOf(
    Currency.USD to Color.GREEN,
    Currency.EUR to Color.CYAN,
    Currency.GBP to Color.RED,
    Currency.AUD to Color.BLUE,
    Currency.NZD to Color.GRAY,
    Currency.JPY to Color.WHITE,
    Currency.CHF to Color.YELLOW,
    Currency.CAD to Color.MAGENTA
    )

val periodStrings = Period.values().map{it.toString()}
val samples = (10..100 step 10).toList().map{it.toString()}

data class PercentSet(var currency: Currency, var percentages: Iterable<Double>) {
  override fun toString(): String {
    return "$currency + ${percentages.map{"$it;"}}"
  }
}
