package com.example

import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun currencyFormatting_isCorrect() {
    assertEquals("0đ", CurrencyFormatter.formatVnd(0L))
    assertEquals("50.000đ", CurrencyFormatter.formatVnd(50000L))
    assertEquals("100.000đ", CurrencyFormatter.formatVnd(100000L))
    assertEquals("1.000.000đ", CurrencyFormatter.formatVnd(1000000L))
    assertEquals("10.000.000đ", CurrencyFormatter.formatVnd(10000000L))
  }

  @Test
  fun currencyParsing_isCorrect() {
    assertEquals(50000L, CurrencyFormatter.parseAmount("50000"))
    assertEquals(1000000L, CurrencyFormatter.parseAmount("1.000.000"))
  }
}
