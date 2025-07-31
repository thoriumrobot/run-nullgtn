package de.zuellich.meal_planner.algorithms;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.schema_org.*;
import de.zuellich.meal_planner.datatypes.RecipeFormat;
import org.junit.Test;

/** */
public class RecipeParserFactoryTest extends FixtureBasedTest {

  private RecipeParserFactory getInstance() {
    final SchemaOrgParser schemaOrgParser = mock(SchemaOrgParser.class);
    when(schemaOrgParser.getFormat()).thenCallRealMethod();

    final SchemaOrgQuirksModeParser schemaOrgQuirksModeParser =
        mock(SchemaOrgQuirksModeParser.class);
    when(schemaOrgQuirksModeParser.getFormat()).thenCallRealMethod();

    final SchemaOrgJSONLDParser schemaOrgJSONLDParser = mock(SchemaOrgJSONLDParser.class);
    when(schemaOrgJSONLDParser.getFormat()).thenCallRealMethod();

    final NullParser defaultParser = mock(NullParser.class);
    when(defaultParser.getFormat()).thenCallRealMethod();
    final ImmutableSet<FormatDetector> detectors =
        ImmutableSet.<FormatDetector>builderWithExpectedSize(3)
            .add(new SchemaOrgFormatDetector(schemaOrgParser))
            .add(new SchemaOrgQuirksModeFormatDetector(schemaOrgQuirksModeParser))
            .add(new SchemaOrgJSONLDFormatDetector(schemaOrgJSONLDParser))
            .build();
    return new RecipeParserFactory(detectors, defaultParser);
  }

  @Test
  public void returnsSchemaOrgParser() {
    final String source = getResource("/fixtures/ingredientScanner/recipes/schema-org-01.html");
    final RecipeParserFactory factory = getInstance();
    final RecipeParser actualParser = factory.getParser(source);
    assertThat(actualParser.getFormat()).isEqualTo(RecipeFormat.SCHEMA_ORG);
  }

  @Test
  public void returnsSchemaOrgQuirksModeParser() {
    final String source = getResource("/fixtures/ingredientScanner/recipes/schema-org-03.html");
    final RecipeParserFactory factory = getInstance();
    final RecipeParser actualParser = factory.getParser(source);
    assertThat(actualParser.getFormat()).isEqualTo(RecipeFormat.SCHEMA_ORG_QUIRCKS_MODE);
  }

  @Test
  public void returnsJSONLdParser() {
    final String source =
        getResource("/fixtures/ingredientScanner/recipes/schema-org-json-ld-01.html");
    final RecipeParserFactory instance = getInstance();
    final RecipeParser recipeParser = instance.getParser(source);
    assertThat(recipeParser.getFormat()).isEqualTo(RecipeFormat.SCHEMA_ORG_JSON_LD);
  }

  @Test
  public void returnsNullParser() {
    final RecipeParserFactory factory = getInstance();
    final RecipeParser actualParser = factory.getParser("");
    assertThat(actualParser.getFormat()).isEqualTo(RecipeFormat.UNKNOWN);
  }

  @Test
  public void alwaysPreferJSONLDOverSchemaOrg() {
    final String source = getResource("/fixtures/ingredientScanner/recipes/schema-org-mixed.html");
    final RecipeParserFactory instance = getInstance();
    final RecipeParser parser = instance.getParser(source);

    assertWithMessage("Should prefer JSON+LD over HTML markup if possible for increased quality.")
        .that(parser.getFormat())
        .isEqualTo(RecipeFormat.SCHEMA_ORG_JSON_LD);
  }
}
