package de.zuellich.meal_planner.algorithms.schema_org;

import static org.junit.Assert.assertEquals;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.AmountParser;
import de.zuellich.meal_planner.algorithms.IngredientMatcher;
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
public class SchemaOrgQuirksModeParserTest extends FixtureBasedTest {

  /** The base path to the recipe fixtures. */
  private static final String recipeFixtureBasePath = "/fixtures/ingredientScanner/recipes";

  @Parameterized.Parameter public String recipeSourcePath;

  @Parameterized.Parameter(1)
  public Recipe expectedRecipe;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {recipeFixtureBasePath + "/schema-org-03.html", SchemaOrgExpectations.getSchemaOrg03()},
        });
  }

  @Test
  public void canParseQuirkySchemaOrgRecipes() {
    IngredientUnitLookup ingredientUnitLookup = IngredientUnitLookup.getInstance();

    SchemaOrgRecipeScanner recipeScanner = new SchemaOrgRecipeScanner();
    SchemaOrgQuirksModeIngredientScanner ingredientScanner =
        new SchemaOrgQuirksModeIngredientScanner(
            new AmountParser(), ingredientUnitLookup, new IngredientMatcher(ingredientUnitLookup));

    RecipeParser parser = new SchemaOrgQuirksModeParser(recipeScanner, ingredientScanner);

    String recipeSource = getResource(recipeSourcePath);
    Recipe parsedRecipe = parser.parse(recipeSource);

    assertEquals(expectedRecipe, parsedRecipe);
  }
}
