package com.uber.okbuck.example.javalib;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class DummyTestJavaClass {

  @Test
  public void testAssertFalse() {
    assertFalse("failure - should be false", false);
  }
}
