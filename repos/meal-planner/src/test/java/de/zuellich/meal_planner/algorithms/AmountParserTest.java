package de.zuellich.meal_planner.algorithms;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

/** */
public class AmountParserTest {

  private AmountParser instance;

  @Before
  public void setUp() {
    instance = new AmountParser();
  }

  @Test
  public void canHandleRanges() {
    assertThat(instance.parseAmount("1-2")).isEqualTo(1f);
    assertThat(instance.parseAmount("15-20")).isEqualTo(15f);
  }

  @Test
  public void canParseNormalNumber() {
    assertThat(instance.parseAmount("1")).isEqualTo(1f);
    assertThat(instance.parseAmount("2")).isEqualTo(2f);
  }

  @Test
  public void canHandleNegativeNumbers() {
    assertThat(instance.parseAmount("-1")).isEqualTo(-1f);
    assertThat(instance.parseAmount("-10")).isEqualTo(-10f);
  }

  @Test
  public void canHandleBasicFractions() {
    assertThat(instance.parseAmount("1/8")).isEqualTo(0.125f);
    assertThat(instance.parseAmount("1/4")).isEqualTo(0.25f);
    assertThat(instance.parseAmount("1/2")).isEqualTo(0.5f);
    assertThat(instance.parseAmount("3/4")).isEqualTo(0.75f);
  }

  @Test
  public void returnsZeroIfEmptyStringOrNull() {
    assertThat(instance.parseAmount(null)).isEqualTo(0f);
    assertThat(instance.parseAmount("")).isEqualTo(0f);
  }

  @Test
  public void canHandleUnicodeVulgarFractions() {
    assertThat(instance.parseAmount("½")).isEqualTo(0.5f);
    assertThat(instance.parseAmount("¼")).isEqualTo(0.25f);
    assertThat(instance.parseAmount("¾")).isEqualTo(0.75f);
  }

  @Test
  public void canHandleMixedUnicodeFractions() {
    assertThat(instance.parseAmount("3 ½")).isEqualTo(3.5f);
  }

  @Test
  public void gracefullyHandleNumberFormatException() {
    assertThat(instance.parseAmount("Not a number.")).isEqualTo(0f);
  }
}
