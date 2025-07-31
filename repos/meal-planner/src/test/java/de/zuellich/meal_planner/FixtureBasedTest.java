package de.zuellich.meal_planner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/** A simple base class that allows us to easily parse our expectations. */
public class FixtureBasedTest {

  /**
   * Load a fixture from the resources.
   *
   * @param path Supply a path that can be resolved from the resources. Example
   *     "/folder-in-resources/file".
   * @return The parsed file or an empty string.
   */
  public String getResource(final String path) {

    try (final InputStream in = FixtureBasedTest.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new RuntimeException(String.format("Could not find resource with path %s", path));
      }

      final Scanner scanner = new Scanner(in, "UTF-8").useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
