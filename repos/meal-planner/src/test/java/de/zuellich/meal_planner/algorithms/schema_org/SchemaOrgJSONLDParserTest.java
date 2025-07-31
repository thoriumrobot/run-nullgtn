package de.zuellich.meal_planner.algorithms.schema_org;

import static com.google.common.truth.Truth.assertThat;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.AmountParser;
import de.zuellich.meal_planner.algorithms.IngredientUnitLookup;
import de.zuellich.meal_planner.algorithms.PlainTextIngredientScanner;
import de.zuellich.meal_planner.algorithms.RecipeParser;
import de.zuellich.meal_planner.datatypes.Ingredient;
import de.zuellich.meal_planner.datatypes.IngredientUnit;
import de.zuellich.meal_planner.datatypes.Recipe;
import org.junit.Test;

public class SchemaOrgJSONLDParserTest extends FixtureBasedTest {

  @Test
  public void canParseRecipe() {
    final String resource =
        this.getResource("/fixtures/ingredientScanner/recipes/schema-org-json-ld-01.html");

    final IngredientUnitLookup ingredientUnitLookup = IngredientUnitLookup.getInstance();
    final RecipeParser parser =
        new SchemaOrgJSONLDParser(
            new PlainTextIngredientScanner(new AmountParser(), ingredientUnitLookup));
    final Recipe recipe = parser.parse(resource);

    assertThat(recipe.getName())
        .isEqualTo("Easy Tomato & Basil Quinoa Risotto \u2013 GF, Vegetarian/Vegan Option");
    assertThat(recipe.getSource()).isEmpty();
    assertThat(recipe.getIngredients())
        .containsExactly(
            new Ingredient("cooked quinoa", 1, IngredientUnit.CUP),
            new Ingredient("fresh tomatoes, small to medium-sized", 4, IngredientUnit.NULL),
            new Ingredient("sun dried tomatoes, soaked for 30min", 3, IngredientUnit.NULL),
            new Ingredient("olive oil", 1, IngredientUnit.TBSP),
            new Ingredient("small zucchini or 1/2 large", 1, IngredientUnit.NULL),
            new Ingredient("handful basil", 0, IngredientUnit.NULL),
            new Ingredient("handful arugula", 0, IngredientUnit.NULL),
            new Ingredient("garlic clove", 1, IngredientUnit.NULL),
            new Ingredient("Optional", 0, IngredientUnit.NULL),
            new Ingredient("some mozzarella", 0, IngredientUnit.NULL),
            new Ingredient("some parmesan", 0, IngredientUnit.NULL));
  }
}
