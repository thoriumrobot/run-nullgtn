package de.zuellich.meal_planner.algorithms.schema_org;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.FormatDetector;
import org.junit.Test;

/** Test that we can detect recipes that are annotated in Schema.org style. */
public class SchemaOrgFormatDetectorTest extends FixtureBasedTest {

  @Test
  public void testCanDetectSchemaOrg() {
    final SchemaOrgParser parser = mock(SchemaOrgParser.class);
    final FormatDetector detector = new SchemaOrgFormatDetector(parser);

    String source = getResource("/fixtures/ingredientScanner/recipes/schema-org-01.html");
    int isSchemaOrgFormatted = detector.isSupported(source);
    assertWithMessage("Should recognize standard schema.org format and return int > 0")
        .that(isSchemaOrgFormatted)
        .isGreaterThan(0);

    source = getResource("/fixtures/ingredientScanner/recipes/schema-org-03.html");
    isSchemaOrgFormatted = detector.isSupported(source);
    assertWithMessage("Should recognize quirky schema.org format and return int < 1")
        .that(isSchemaOrgFormatted)
        .isLessThan(1);
  }
}
