package com.uber.rib.root.logged_out;

import android.support.annotation.NonNull;
import android.view.View;

import com.uber.rib.core.ViewRouter;

/**
 * Adds and removes children of {@link LoggedOutBuilder.LoggedOutScope}.
 *
 * TODO describe the possible child configurations of this scope.
 */
public class LoggedOutRouter extends
    ViewRouter<LoggedOutView, LoggedOutInteractor, LoggedOutBuilder.Component> {

  public LoggedOutRouter(
      LoggedOutView view,
      LoggedOutInteractor interactor,
      LoggedOutBuilder.Component component) {
    super(view, interactor, component);
  }
}
