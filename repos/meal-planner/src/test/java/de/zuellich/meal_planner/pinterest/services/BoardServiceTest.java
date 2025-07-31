package de.zuellich.meal_planner.pinterest.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import de.zuellich.meal_planner.FixtureBasedTest;
import de.zuellich.meal_planner.pinterest.datatypes.Board;
import de.zuellich.meal_planner.pinterest.datatypes.BoardListing;
import de.zuellich.meal_planner.pinterest.datatypes.Pin;
import java.util.List;
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
public class BoardServiceTest extends FixtureBasedTest {

  private static final String EXAMPLE_BOARD_ID = "111111111111111111";
  private static final String ACCESS_TOKEN = "abcdef";

  /** This url is expected to be called to retrieve the pins for a board. */
  private static final String EXPECTED_URL_FOR_PIN_REQUEST =
      "https://api.pinterest.com/v1/boards/111111111111111111/pins/?fields=id,original_link,note,metadata";

  /** @return Construct an instance of OAuth2RestTemplate with an access token. */
  private OAuth2RestTemplate getRestTemplate() {
    OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(ACCESS_TOKEN);
    DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();
    clientContext.setAccessToken(accessToken);

    return new OAuth2RestTemplate(new AuthorizationCodeResourceDetails(), clientContext);
  }

  /**
   * Get a ready set up instance of the BoardService.
   *
   * @param restTemplate The RestTemplate instance to inject.
   * @return The service instance.
   */
  private IBoardService getBoardService(OAuth2RestTemplate restTemplate) {
    return new BoardService(restTemplate);
  }

  @Test
  public void returnsUsersBoards() {
    final String responseJSON = getResource("/fixtures/pinterest/responses/v1/me_boards.json");

    OAuth2RestTemplate restTemplate = getRestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(requestTo("https://api.pinterest.com/v1/me/boards"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "bearer " + ACCESS_TOKEN))
        .andRespond(withSuccess(responseJSON, MediaType.APPLICATION_JSON));

    IBoardService service = getBoardService(restTemplate);
    List<Board> boards = service.getBoards();

    server.verify();
    assertEquals("Two boards are returned.", 2, boards.size());

    Board board = boards.get(0);
    assertEquals("https://www.pinterest.com/auser/board-name1/", board.getUrl());
    assertEquals("1", board.getId());
    assertEquals("Board Name 1", board.getName());

    board = boards.get(1);
    assertEquals("https://www.pinterest.com/auser/board2/", board.getUrl());
    assertEquals("2", board.getId());
    assertEquals("Board2", board.getName());
  }

  @Test
  public void returnsBoardsPins() {
    final String responseJSON = getResource("/fixtures/pinterest/responses/v1/board_pins.json");

    OAuth2RestTemplate restTemplate = getRestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(requestTo(EXPECTED_URL_FOR_PIN_REQUEST))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "bearer " + ACCESS_TOKEN))
        .andRespond(withSuccess(responseJSON, MediaType.APPLICATION_JSON));

    IBoardService service = getBoardService(restTemplate);
    List<Pin> pins = service.getPins(EXAMPLE_BOARD_ID);

    server.verify();
    assertEquals("5 pins are returned", 5, pins.size());
    assertPinsNotEmptyOrNull(pins);
  }

  /**
   * Verify that all pins in the list have an id and link that is not empty or null.
   *
   * @param pins The list of pins to verify. Make sure the list is not empty.
   */
  private void assertPinsNotEmptyOrNull(List<Pin> pins) {
    for (Pin pin : pins) {
      assertFalse("Pin's id should be set.", pin.getId().isEmpty());
      assertFalse("Pin's link should be set.", pin.getOriginalLink().isEmpty());
    }
  }

  @Test
  public void returnsABoardWithItsPins() {
    final String boardResponseJSON = getResource("/fixtures/pinterest/responses/v1/get_board.json");
    final String pinResponseJSON = getResource("/fixtures/pinterest/responses/v1/board_pins.json");

    OAuth2RestTemplate restTemplate = getRestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    // first mock the board retrieval
    server
        .expect(
            requestTo(
                "https://api.pinterest.com/v1/boards/" + EXAMPLE_BOARD_ID + "/?fields=id,name,url"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "bearer " + ACCESS_TOKEN))
        .andRespond(withSuccess(boardResponseJSON, MediaType.APPLICATION_JSON));

    // now also mock the pin retrieval
    server
        .expect(requestTo(EXPECTED_URL_FOR_PIN_REQUEST))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "bearer " + ACCESS_TOKEN))
        .andRespond(withSuccess(pinResponseJSON, MediaType.APPLICATION_JSON));

    IBoardService service = getBoardService(restTemplate);
    BoardListing result = service.getBoardListing(EXAMPLE_BOARD_ID);

    Board resultBoard = result.getBoard();
    assertEquals("The board name is returned.", "Board name", resultBoard.getName());
    assertEquals(
        "The board link is returned.",
        "https://www.pinterest.com/user/board-name/",
        resultBoard.getUrl());
    assertEquals("The board id is returned.", "1111111111111111111", resultBoard.getId());

    assertEquals("5 Pins are returned.", 5, result.getPins().size());
    assertPinsNotEmptyOrNull(result.getPins());

    server.verify();
  }
}
