package de.zuellich.meal_planner.algorithms.schema_org;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.FormatDetector;
import org.junit.Test;

/** */
public class SchemaOrgQuirksModeFormatDetectorTest extends FixtureBasedTest {

  @Test
  public void recognizesQuirkySchemaOrgFormat() {
    final SchemaOrgQuirksModeParser parser = mock(SchemaOrgQuirksModeParser.class);
    final FormatDetector formatDetector = new SchemaOrgQuirksModeFormatDetector(parser);

    String recipeSource = getResource("/fixtures/ingredientScanner/recipes/schema-org-03.html");
    int isSupported = formatDetector.isSupported(recipeSource);
    assertWithMessage("Does recognize quirky schema.org").that(isSupported).isAtLeast(2);

    recipeSource = getResource("/fixtures/ingredientScanner/recipes/schema-org-01.html");
    isSupported = formatDetector.isSupported(recipeSource);
    assertWithMessage("Should not recognize standard schema.org as quirky")
        .that(isSupported)
        .isAtMost(0);
  }
}
