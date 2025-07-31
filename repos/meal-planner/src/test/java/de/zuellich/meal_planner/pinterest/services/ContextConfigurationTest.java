package de.zuellich.meal_planner.pinterest.services;

import static org.mockito.Mockito.mock;

import de.zuellich.meal_planner.MealPlanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2RestOperations;

/** */
@Import(MealPlanner.class)
@Configuration
public class ContextConfigurationTest {

  @Bean("pinterestRestTemplate")
  public OAuth2RestOperations getOAuth2RestOperations() {
    return mock(OAuth2RestOperations.class);
  }
}
