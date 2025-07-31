package de.zuellich.meal_planner;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Taken from:
 * https://stackoverflow.com/questions/23504819/how-to-disable-ssl-certificate-checking-with-spring-resttemplate?noredirect=1&lq=1
 * Helps with self signed certificates and unit tests.
 */
public final class SSLUtil {

  private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER =
      new TrustManager[] {
        new X509TrustManager() {
          @Override
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          @Override
          public void checkClientTrusted(final X509Certificate[] certs, final String authType) {}

          @Override
          public void checkServerTrusted(final X509Certificate[] certs, final String authType) {}
        }
      };

  private SSLUtil() {
    throw new UnsupportedOperationException("Do not instantiate libraries.");
  }

  public static void turnOffSslChecking() throws NoSuchAlgorithmException, KeyManagementException {
    // Install the all-trusting trust manager
    final SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, SSLUtil.UNQUESTIONING_TRUST_MANAGER, null);
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
  }

  public static void turnOnSslChecking() throws KeyManagementException, NoSuchAlgorithmException {
    // Return it to the initial state (discovered by reflection, now hardcoded)
    SSLContext.getInstance("SSL").init(null, null, null);
  }
}
