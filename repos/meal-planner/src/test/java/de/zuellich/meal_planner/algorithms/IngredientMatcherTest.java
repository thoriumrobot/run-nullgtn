package de.zuellich.meal_planner.algorithms;

import static org.junit.Assert.*;

import de.zuellich.meal_planner.datatypes.IngredientUnit;
import org.junit.Before;
import org.junit.Test;

/** */
public class IngredientMatcherTest {

  private IngredientMatcher matcher;

  @Before
  public void setUp() throws Exception {
    matcher = new IngredientMatcher(IngredientUnitLookup.getInstance());
  }

  @Test
  public void canMatchWellFormedIngredient() {
    IngredientMatcher.IngredientMatcherResult matcherResult = matcher.match("100g steak");

    assertTrue(matcherResult.isMatching());
    assertEquals("100", matcherResult.getAmount());
    assertEquals(IngredientUnit.G, matcherResult.getUnit());
    assertEquals("steak", matcherResult.getName());
  }

  @Test
  public void canMatchWithoutUnit() {
    IngredientMatcher.IngredientMatcherResult matcherResult = matcher.match("1 egg");

    assertTrue("The matcher should match the given description.", matcherResult.isMatching());
    assertEquals("1", matcherResult.getAmount());
    assertEquals(IngredientUnit.NULL, matcherResult.getUnit());
    assertEquals("egg", matcherResult.getName());
  }

  @Test
  public void canMatchIngredientWithNotes() {
    IngredientMatcher.IngredientMatcherResult matcherResult = matcher.match("1 egg (hard)");

    assertTrue("The matcher should match the given description.", matcherResult.isMatching());
    assertEquals("1", matcherResult.getAmount());
    assertEquals(IngredientUnit.NULL, matcherResult.getUnit());
    assertEquals("egg (hard)", matcherResult.getName());

    matcherResult = matcher.match("100g steak (or more)");
    assertTrue("The matcher should match the given description.", matcherResult.isMatching());
    assertEquals("100", matcherResult.getAmount());
    assertEquals(IngredientUnit.G, matcherResult.getUnit());
    assertEquals("steak (or more)", matcherResult.getName());
  }

  @Test
  public void canMatchFractionIngredient() {
    IngredientMatcher.IngredientMatcherResult matcherResult = matcher.match("1/2 cup water");

    assertTrue("The matcher should match the given subscription.", matcherResult.isMatching());
    assertEquals("1/2", matcherResult.getAmount());
    assertEquals(IngredientUnit.CUP, matcherResult.getUnit());
    assertEquals("water", matcherResult.getName());

    matcherResult = matcher.match("1 1/2 cup water");

    assertEquals("1 1/2", matcherResult.getAmount());
    assertEquals(IngredientUnit.CUP, matcherResult.getUnit());
    assertEquals("water", matcherResult.getName());

    matcherResult = matcher.match("½ cup Dijon mustard");

    assertEquals("½", matcherResult.getAmount());
    assertEquals(IngredientUnit.CUP, matcherResult.getUnit());
    assertEquals("Dijon mustard", matcherResult.getName());
  }

  @Test
  public void returnEmptyResultOnMismatch() {
    IngredientMatcher.IngredientMatcherResult matcherResult = matcher.match("");

    assertFalse("The matcher should not match an empty string", matcherResult.isMatching());
    assertEquals("", matcherResult.getAmount());
    assertEquals(IngredientUnit.NULL, matcherResult.getUnit());
    assertEquals("", matcherResult.getName());
  }

  @Test
  public void canMatchWithoutAmountAndUnit() {
    IngredientMatcher.IngredientMatcherResult matcherResult = matcher.match("Salt & pepper");
    assertEquals("", matcherResult.getAmount());
    assertEquals(IngredientUnit.NULL, matcherResult.getUnit());
    assertEquals("Salt & pepper", matcherResult.getName());
  }
}
