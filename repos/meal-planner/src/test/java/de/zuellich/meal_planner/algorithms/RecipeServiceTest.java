package de.zuellich.meal_planner.algorithms;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.algorithms.schema_org.SchemaOrgParser;
import de.zuellich.meal_planner.datatypes.Recipe;
import de.zuellich.meal_planner.exception.RecipeParseException;
import de.zuellich.meal_planner.expectations.SchemaOrgExpectations;
import java.io.IOException;
import org.junit.Test;

/** */
public class RecipeServiceTest extends FixtureBasedTest {

  @Test
  public void returnsRecipeForSource() throws IOException {
    String url = "";
    String fixture = getResource("/fixtures/ingredientScanner/recipes/schema-org-01.html");

    Recipe expected = SchemaOrgExpectations.getSchemaOrg01();
    RecipeParserFactory mockedParserFactory = getMockedParserFactory(expected);

    RecipeFetcherService mockedFetcherService = mock(RecipeFetcherService.class);
    when(mockedFetcherService.fetchByURL(anyString())).thenReturn(fixture);

    RecipeService service = new RecipeService(mockedParserFactory, mockedFetcherService);
    Recipe result = service.fromURL(url);

    assertEquals(expected, result);
  }

  @Test(expected = RecipeParseException.class)
  public void throwsExceptionOnError() throws IOException {
    // Create a fetcher service that throws an IOException
    RecipeFetcherService mockedFetcherService = mock(RecipeFetcherService.class);
    when(mockedFetcherService.fetchByURL(anyString())).thenThrow(new IOException());

    RecipeParserFactory factory = getMockedParserFactory(null);
    RecipeService service = new RecipeService(factory, mockedFetcherService);

    String url = "";
    service.fromURL(url);
  }

  /**
   * Get a mocked parser factory that resolves the expected recipe.
   *
   * @param expected
   * @return
   */
  private RecipeParserFactory getMockedParserFactory(Recipe expected) {
    RecipeParser schemaOrgParser = mock(SchemaOrgParser.class);
    when(schemaOrgParser.parse(anyString())).thenReturn(expected);

    RecipeParserFactory parserFactory = mock(RecipeParserFactory.class);
    when(parserFactory.getParser(anyString())).thenReturn(schemaOrgParser);

    return parserFactory;
  }
}
