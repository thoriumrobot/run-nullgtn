package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.FixtureBasedTest;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** This test requires PowerMock in order to mock {@link Jsoup::connect}. */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
public class RecipeFetcherServiceTest extends FixtureBasedTest {

  @Test
  public void canFetchByURL() throws IOException {
    String fixture = getResource("/fixtures/ingredientScanner/recipes/schema-org-01.html");
    String expected = Jsoup.parse(fixture).html();

    // We just want to verify that we get the document as string back. This might not be the best
    // method yet.
    Connection connection = Mockito.mock(Connection.class);
    Mockito.when(connection.get()).thenReturn(Jsoup.parse(fixture));
    PowerMockito.mockStatic(Jsoup.class);
    PowerMockito.when(Jsoup.connect(Mockito.anyString())).thenReturn(connection);

    String url = "";
    RecipeFetcherService service = new RecipeFetcherService();
    String result = service.fetchByURL(url);

    Assert.assertEquals(expected, result);
  }
}
