package de.zuellich.meal_planner.controller;

import de.zuellich.meal_planner.MealPlanner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/** */
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = MealPlanner.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class PinterestExplorerTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate testRestTemplate;

  @Test
  public void returnsAListOfBoards() {}
}
