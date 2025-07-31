package de.zuellich.meal_planner.pinterest.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.zuellich.meal_planner.pinterest.datatypes.Board;
import de.zuellich.meal_planner.pinterest.datatypes.BoardList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/** Verify the caching behaviour for the BoardService */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {ContextConfigurationTest.class})
@ActiveProfiles("testing")
public class BoardServiceCachingTest {

  @Autowired private OAuth2RestOperations restTemplateToMock;

  @Autowired private IBoardService boardService;

  @Before
  public void setUp() {
    BoardList boardList = new BoardList();
    boardList.setBoards(Collections.emptyList());

    final ResponseEntity mockedResponse = mock(ResponseEntity.class);
    when(mockedResponse.getBody()).thenReturn(boardList);

    when(restTemplateToMock.getForEntity(eq(BoardService.USERS_BOARDS), any()))
        .thenReturn(mockedResponse);
  }

  @Test
  public void cachesRequestsForGetBoards() {
    List<Board> boards = boardService.getBoards();
    assertTrue(
        "Make sure the rest template first returns an empty list of boards", boards.isEmpty());

    boardService.getBoards();
    verify(restTemplateToMock).getForEntity(eq(BoardService.USERS_BOARDS), any());
    verifyNoMoreInteractions(restTemplateToMock);
  }
}
