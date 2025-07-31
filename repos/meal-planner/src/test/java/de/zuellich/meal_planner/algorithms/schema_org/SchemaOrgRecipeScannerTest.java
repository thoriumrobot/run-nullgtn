package de.zuellich.meal_planner.algorithms.schema_org;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.RecipeScanner;
import de.zuellich.meal_planner.datatypes.Recipe;
import org.junit.Test;

/** */
public class SchemaOrgRecipeScannerTest extends FixtureBasedTest {

  @Test
  public void testCanRetrieveBasicInformationFromRecipe() {
    String fixture = getResource("/fixtures/ingredientScanner/recipes/schema-org-01.html");
    RecipeScanner instance = new SchemaOrgRecipeScanner();
    Recipe recipe = instance.parse(fixture);

    assertEquals(
        "The scanner returns the name of the recipe.",
        "Quick Teriyaki Chicken Rice Bowls",
        recipe.getName());
    assertTrue("The scanner does not provide the ingredients.", recipe.getIngredients().isEmpty());
  }

  /**
   * Test that the recipe scanner returns the authors URL if the recipe itself does not carry the
   * itemprop url.
   */
  @Test
  public void testReturnEmptyURLIfNotIncludedInSite() {
    String fixture = getResource("/fixtures/ingredientScanner/recipes/schema-org-01.html");
    RecipeScanner instance = new SchemaOrgRecipeScanner();
    Recipe recipe = instance.parse(fixture);

    assertEquals("The scanner provides an empty string as URL.", "", recipe.getSource());
  }
}
