package com.uber.rib.root.logged_in;

import android.support.annotation.Nullable;

import android.view.ViewGroup;
import com.uber.rib.core.Router;
import com.uber.rib.root.logged_in.off_game.OffGameBuilder;
import com.uber.rib.root.logged_in.off_game.OffGameRouter;
import com.uber.rib.root.logged_in.tic_tac_toe.TicTacToeBuilder;
import com.uber.rib.root.logged_in.tic_tac_toe.TicTacToeRouter;

/**
 * Adds and removes children of {@link LoggedInBuilder.LoggedInScope}.
 *
 * TODO describe the possible child configurations of this scope.
 */
public class LoggedInRouter
    extends Router<LoggedInInteractor, LoggedInBuilder.Component> {

  private final ViewGroup parentView;
  private final OffGameBuilder offGameBuilder;
  private final TicTacToeBuilder ticTacToeBuilder;
  @Nullable private OffGameRouter offGameRouter;
  @Nullable private TicTacToeRouter ticTacToeRouter;

  public LoggedInRouter(
      LoggedInInteractor interactor,
      LoggedInBuilder.Component component,
      ViewGroup parentView,
      OffGameBuilder offGameBuilder,
      TicTacToeBuilder ticTacToeBuilder) {
    super(interactor, component);
    this.parentView = parentView;
    this.offGameBuilder = offGameBuilder;
    this.ticTacToeBuilder = ticTacToeBuilder;
  }

  @Override
  protected void willDetach() {
    super.willDetach();
    detachOffGame();
    detachTicTacToe();
  }

  void attachOffGame() {
    offGameRouter = offGameBuilder.build(parentView);
    attachChild(offGameRouter);
    parentView.addView(offGameRouter.getView());
  }

  void detachOffGame() {
    if (offGameRouter != null) {
      detachChild(offGameRouter);
      parentView.removeView(offGameRouter.getView());
      offGameRouter = null;
    }
  }

  void attachTicTacToe() {
    ticTacToeRouter = ticTacToeBuilder.build(parentView);
    attachChild(ticTacToeRouter);
    parentView.addView(ticTacToeRouter.getView());
  }

  void detachTicTacToe() {
    if (ticTacToeRouter != null) {
      detachChild(ticTacToeRouter);
      parentView.removeView(ticTacToeRouter.getView());
      ticTacToeRouter = null;
    }
  }

}
