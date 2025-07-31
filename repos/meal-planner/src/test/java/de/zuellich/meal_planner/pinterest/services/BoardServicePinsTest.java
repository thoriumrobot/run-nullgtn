package de.zuellich.meal_planner.pinterest.services;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.pinterest.datatypes.Pin;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;

/** */
public class BoardServicePinsTest extends FixtureBasedTest {

  private IBoardService service;

  /** @return Construct an instance of OAuth2RestTemplate with an access token. */
  private OAuth2RestTemplate getRestTemplate() {
    final OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(ACCESS_TOKEN);
    final DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();
    clientContext.setAccessToken(accessToken);

    return new OAuth2RestTemplate(new AuthorizationCodeResourceDetails(), clientContext);
  }

  /**
   * Get a ready set up instance of the BoardService.
   *
   * @param restTemplate The RestTemplate instance to inject.
   * @return The service instance.
   */
  private IBoardService getBoardService(final OAuth2RestTemplate restTemplate) {
    return new BoardService(restTemplate);
  }

  @Before
  public void setUp() {
    final String pinResponseJSON =
        this.getResource("/fixtures/pinterest/responses/v1/board_pins_with_recipe_name.json");

    final OAuth2RestTemplate restTemplate = this.getRestTemplate();
    final MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    // now also mock the pin retrieval
    server
        .expect(
            requestTo(
                "https://api.pinterest.com/v1/boards/exampleBoard/pins/?fields=id,original_link,note,metadata"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "bearer " + ACCESS_TOKEN))
        .andRespond(withSuccess(pinResponseJSON, MediaType.APPLICATION_JSON));

    this.service = this.getBoardService(restTemplate);
  }

  @Test
  public void returnsRecipeNameFromPin() {
    final List<Pin> pins = this.service.getPins("exampleBoard");
    final Pin pin = pins.get(0);
    assertEquals("Irischer Rindfleischeintopf mit Guinness", pin.getName());
  }

  @Test
  public void returnsOriginalLinkAsLinkFromPin() {
    final List<Pin> pins = this.service.getPins("exampleBoard");
    final Pin pin = pins.get(0);
    assertEquals(
        "https://www.springlane.de/magazin/rezeptideen/irischer-rindfleischeintopf-mit-guinness/?utm_source=pinterest&utm_medium=social&utm_campaign=post",
        pin.getOriginalLink());
  }

  @Test
  public void returnsAllPinsAcrossDifferentPages() {
    final String boardResponsePage1 =
        this.getResource("/fixtures/pinterest/responses/v1/board_pins_with_cursor1.json");
    final String boardResponsePage2 =
        this.getResource("/fixtures/pinterest/responses/v1/board_pins_with_cursor2.json");

    final OAuth2RestTemplate restTemplate = this.getRestTemplate();
    final MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    server
        .expect(
            requestTo(
                "https://api.pinterest.com/v1/boards/exampleBoardId/pins/?fields=id,original_link,note,metadata"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "bearer " + ACCESS_TOKEN))
        .andRespond(withSuccess(boardResponsePage1, MediaType.APPLICATION_JSON));

    server
        .expect(
            requestTo(
                "https://api.pinterest.com/v1/boards/exampleBoardId/pins/?fields=id,original_link,note,metadata&cursor=acursor"))
        .andRespond(withSuccess(boardResponsePage2, MediaType.APPLICATION_JSON));

    final IBoardService boardService = this.getBoardService(restTemplate);
    boardService.getPins("exampleBoardId");

    server.verify();
  }
}
