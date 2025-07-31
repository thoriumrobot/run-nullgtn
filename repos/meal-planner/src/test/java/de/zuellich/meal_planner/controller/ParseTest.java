package de.zuellich.meal_planner.controller;

import static org.assertj.core.api.BDDAssertions.then;

import de.zuellich.meal_planner.MealPlanner;
import de.zuellich.meal_planner.SSLUtil;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/** */
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = MealPlanner.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ParseTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate testRestTemplate;

  @Before
  public void disableSSLCertificateValidation()
      throws KeyManagementException, NoSuchAlgorithmException {
    SSLUtil.turnOffSslChecking();
  }

  @Test
  public void acceptsAURLParameter() {
    String url = this.getURL("/parse");
    ResponseEntity<Map> entity =
        this.testRestTemplate.withBasicAuth("test", "test").getForEntity(url, Map.class);
    then(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    url = this.getURL("/parse?url=http%3A%2F%2Fexample.com");
    entity = this.testRestTemplate.getForEntity(url, Map.class);
    then(entity.getStatusCode().is2xxSuccessful()).isFalse();
  }

  @Test
  public void respondsWithErrorWhenNotAValidURL() {
    final String url = this.getURL("/parse?url=test");
    final ResponseEntity<Map> entity =
        this.testRestTemplate.withBasicAuth("test", "test").getForEntity(url, Map.class);
    then(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  /**
   * Create a URL by appending the given parameter to the base URL.
   *
   * @param append The part to append. Note that the base URL does not contain a trailing slash.
   * @return The constructed URL.
   */
  private String getURL(final String append) {
    return "https://localhost:" + this.port + append;
  }
}
