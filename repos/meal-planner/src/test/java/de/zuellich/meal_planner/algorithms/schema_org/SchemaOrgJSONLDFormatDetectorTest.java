package de.zuellich.meal_planner.algorithms.schema_org;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.FormatDetector;
import org.junit.Test;

/** */
public class SchemaOrgJSONLDFormatDetectorTest extends FixtureBasedTest {

  @Test
  public void canDetectRecipeWithJSONLD() {
    final String recipeSource =
        getResource("/fixtures/ingredientScanner/recipes/schema-org-json-ld-01.html");
    final FormatDetector detector =
        new SchemaOrgJSONLDFormatDetector(mock(SchemaOrgJSONLDParser.class));
    final int supported = detector.isSupported(recipeSource);
    assertWithMessage("JSON+LD format is not detected.").that(supported).isGreaterThan(0);
  }

  @Test
  public void avoidMistakingOtherSchemaOrgDataInJSONLD() {
    final String recipeSource =
        getResource("/fixtures/ingredientScanner/recipes/schema-org-json-ld-no-recipe.html");
    final FormatDetector detector =
        new SchemaOrgJSONLDFormatDetector(mock(SchemaOrgJSONLDParser.class));
    final int supported = detector.isSupported(recipeSource);
    assertWithMessage("Check carefully if the JSON+LD information is about a recipe.")
        .that(supported)
        .isAtMost(0);
  }
}
