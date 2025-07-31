package de.zuellich.meal_planner.algorithms;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.zuellich.meal_planner.datatypes.Ingredient;
import de.zuellich.meal_planner.datatypes.IngredientUnit;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class PlainTextIngredientScannerTest {

  private PlainTextIngredientScanner ingredientScanner;

  @Before
  public void setUp() {
    final AmountParser amountParser = mock(AmountParser.class);
    when(amountParser.parseAmount("1")).thenReturn(1f);
    when(amountParser.parseAmount("1/2")).thenReturn(0.5f);

    final IngredientUnitLookup ingredientUnitLookup = mock(IngredientUnitLookup.class);
    when(ingredientUnitLookup.lookup("cup")).thenReturn(Optional.of(IngredientUnit.CUP));
    when(ingredientUnitLookup.lookup("tbsp")).thenReturn(Optional.of(IngredientUnit.TBSP));
    when(ingredientUnitLookup.lookup("basil")).thenReturn(Optional.empty());

    ingredientScanner = new PlainTextIngredientScanner(amountParser, ingredientUnitLookup);
  }

  @Test
  public void canParseANormalIngredient() {
    String ingredient = "1 cup quinoa";
    List<Ingredient> result = ingredientScanner.parse(ingredient);
    assertThat(result).containsExactly(new Ingredient("quinoa", 1, IngredientUnit.CUP));

    ingredient = "1 cup brown sugar (optional)";
    result = ingredientScanner.parse(ingredient);
    assertThat(result)
        .containsExactly(new Ingredient("brown sugar (optional)", 1, IngredientUnit.CUP));
  }

  @Test
  public void canParseWithMissingUnit() {
    final String ingredient = "1 basil leaf";
    final List<Ingredient> result = ingredientScanner.parse(ingredient);
    assertThat(result).containsExactly(new Ingredient("basil leaf", 1, IngredientUnit.NULL));
  }

  @Test
  public void canParseWithFractions() {
    final String ingredient = "1/2 tbsp sugar";
    final List<Ingredient> result = ingredientScanner.parse(ingredient);
    assertThat(result).containsExactly(new Ingredient("sugar", 0.5f, IngredientUnit.TBSP));
  }

  @Test
  public void canParseWithMissingParts() {
    String ingredient = "Optional basil";
    List<Ingredient> result = ingredientScanner.parse(ingredient);
    assertThat(result).containsExactly(new Ingredient("Optional basil", 0, IngredientUnit.NULL));

    ingredient = "Optional";
    result = ingredientScanner.parse(ingredient);
    assertThat(result).containsExactly(new Ingredient("Optional", 0, IngredientUnit.NULL));
  }
}
