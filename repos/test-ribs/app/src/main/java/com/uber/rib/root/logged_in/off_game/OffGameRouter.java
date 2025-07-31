package com.uber.rib.root.logged_in.off_game;

import android.support.annotation.NonNull;
import android.view.View;

import com.uber.rib.core.ViewRouter;

/**
 * Adds and removes children of {@link OffGameBuilder.OffGameScope}.
 *
 * TODO describe the possible child configurations of this scope.
 */
public class OffGameRouter extends
    ViewRouter<OffGameView, OffGameInteractor, OffGameBuilder.Component> {

  public OffGameRouter(
      OffGameView view,
      OffGameInteractor interactor,
      OffGameBuilder.Component component) {
    super(view, interactor, component);
  }
}
