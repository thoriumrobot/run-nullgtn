package de.zuellich.meal_planner.algorithms;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth8.assertThat;

import de.zuellich.meal_planner.datatypes.IngredientUnit;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

/** */
public class IngredientUnitLookupTest {

  private IngredientUnitLookup instance;

  @Before
  public void setUp() {
    instance = IngredientUnitLookup.getInstance();
  }

  @Test
  public void canLookupByUnitShorthand() {
    final Optional<IngredientUnit> cupUnit =
        instance.byShorthand(IngredientUnit.CUP.getShorthand());
    assertThat(cupUnit).hasValue(IngredientUnit.CUP);
  }

  @Test
  public void canLookupByUnitPlural() {
    final Optional<IngredientUnit> cloveUnit = instance.byPlural(IngredientUnit.CLOVE.getPlural());
    assertThat(cloveUnit).hasValue(IngredientUnit.CLOVE);
  }

  @Test
  public void canLookupByUnitSingular() {
    final Optional<IngredientUnit> teaspoonUnit =
        instance.bySingular(IngredientUnit.TSP.getSingular());
    assertThat(teaspoonUnit).hasValue(IngredientUnit.TSP);
  }

  @Test
  public void canLookupChained() {
    Optional<IngredientUnit> unit = instance.lookup(IngredientUnit.CLOVE.getShorthand());
    assertWithMessage("Search for the shorthand for an ingredient")
        .that(com.google.common.base.Optional.fromJavaUtil(unit))
        .hasValue(IngredientUnit.CLOVE);

    unit = instance.lookup(IngredientUnit.CLOVE.getPlural());
    assertWithMessage("Search the plural for an ingredient")
        .that(com.google.common.base.Optional.fromJavaUtil(unit))
        .hasValue(IngredientUnit.CLOVE);

    unit = instance.lookup(IngredientUnit.TSP.getSingular());
    assertWithMessage("Search the singular for an ingredient unit")
        .that(com.google.common.base.Optional.fromJavaUtil(unit))
        .hasValue(IngredientUnit.TSP);
  }
}
