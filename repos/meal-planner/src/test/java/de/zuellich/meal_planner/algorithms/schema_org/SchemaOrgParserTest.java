package de.zuellich.meal_planner.algorithms.schema_org;

import static org.junit.Assert.assertEquals;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.AmountParser;
import de.zuellich.meal_planner.algorithms.IngredientUnitLookup;
import de.zuellich.meal_planner.algorithms.RecipeParser;
import de.zuellich.meal_planner.datatypes.Recipe;
import de.zuellich.meal_planner.expectations.SchemaOrgExpectations;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** */
@RunWith(Parameterized.class)
public class SchemaOrgParserTest extends FixtureBasedTest {

  /** The base path to the recipe fixtures. */
  private static final String recipeFixtureBasePath = "/fixtures/ingredientScanner/recipes";
  /** Parameter with the path to the recipe to load. */
  @Parameterized.Parameter public String inputRecipePath;
  /** Parameter with a configured instance of Recipe that should match. */
  @Parameterized.Parameter(1)
  public Recipe expectedRecipe;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {recipeFixtureBasePath + "/schema-org-01.html", SchemaOrgExpectations.getSchemaOrg01()},
          {recipeFixtureBasePath + "/schema-org-02.html", SchemaOrgExpectations.getSchemaOrg02()}
        });
  }

  @Test
  public void testCanReturnAProperRecipeInstance() {
    String source = getResource(inputRecipePath);

    SchemaOrgRecipeScanner recipeScanner = new SchemaOrgRecipeScanner();
    SchemaOrgIngredientScanner ingredientScanner =
        new SchemaOrgIngredientScanner(new AmountParser(), IngredientUnitLookup.getInstance());
    RecipeParser parser = new SchemaOrgParser(recipeScanner, ingredientScanner);
    Recipe recipe = parser.parse(source);

    assertEquals(expectedRecipe, recipe);
  }
}
