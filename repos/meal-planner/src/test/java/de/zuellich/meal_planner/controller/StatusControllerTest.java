package de.zuellich.meal_planner.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;

/** */
public class StatusControllerTest {

  private StatusController getStatusController(LocalDate accessTokenExpireDate) {
    final DefaultOAuth2AccessToken tokenFromDate = new DefaultOAuth2AccessToken("");
    final Date date =
        Date.from(accessTokenExpireDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    tokenFromDate.setExpiration(date);

    final DefaultOAuth2ClientContext context = new DefaultOAuth2ClientContext();
    context.setAccessToken(tokenFromDate);

    final OAuth2RestOperations restTemplate = mock(OAuth2RestOperations.class);
    when(restTemplate.getOAuth2ClientContext()).thenReturn(context);

    return new StatusController(restTemplate);
  }

  @Test
  public void returnsTrueIfAccessTokenNotExpired() {
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    final StatusController service = getStatusController(tomorrow);

    final ResponseEntity<Boolean> status = service.getStatus();
    assertTrue(status.getBody());
  }

  @Test
  public void returnsFalseIfAccessTokenExpired() {
    final LocalDate yesterday = LocalDate.now().minusDays(1);
    final StatusController service = getStatusController(yesterday);

    final ResponseEntity<Boolean> status = service.getStatus();
    assertFalse(status.getBody());
  }

  @Test
  public void returnsFalseIfNoAccessTokenAvailable() {
    final OAuth2ClientContext context = new DefaultOAuth2ClientContext();

    final OAuth2RestOperations restTemplate = mock(OAuth2RestOperations.class);
    when(restTemplate.getOAuth2ClientContext()).thenReturn(context);

    final StatusController service = new StatusController(restTemplate);
    final ResponseEntity<Boolean> status = service.getStatus();
    assertFalse(status.getBody());
  }
}
