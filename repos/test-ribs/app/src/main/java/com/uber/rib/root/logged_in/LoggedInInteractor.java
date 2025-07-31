package com.uber.rib.root.logged_in;

import android.support.annotation.Nullable;

import com.uber.rib.core.Bundle;
import com.uber.rib.core.EmptyPresenter;
import com.uber.rib.core.Interactor;
import com.uber.rib.core.RibInteractor;

import com.uber.rib.root.UserName;
import com.uber.rib.root.logged_in.off_game.OffGameInteractor;
import com.uber.rib.root.logged_in.off_game.OffGameInteractor;
import com.uber.rib.root.logged_in.LoggedInBuilder.LoggedInInternal;
import com.uber.rib.root.logged_in.tic_tac_toe.TicTacToeInteractor;

import java.util.List;
import javax.inject.Inject;

/**
 * Coordinates Business Logic for {@link LoggedInScope}.
 *
 * TODO describe the logic of this scope.
 */
@RibInteractor
public class LoggedInInteractor extends Interactor<EmptyPresenter, LoggedInRouter> {

  @Inject @LoggedInInternal MutableScoreStream scoreStream;

  @Override
  protected void didBecomeActive(@Nullable Bundle savedInstanceState) {
    super.didBecomeActive(savedInstanceState);

    // when first logging in we should be in the OffGame state
    getRouter().attachOffGame();
  }

  class OffGameListener implements OffGameInteractor.Listener {

    @Override
    public void onStartGame() {
      getRouter().detachOffGame();
      getRouter().attachTicTacToe();
    }
  }

  class TicTacToeListener implements TicTacToeInteractor.Listener {

    @Override
    public void gameWon(@Nullable UserName winner) {
      if (winner != null) {
        scoreStream.addVictory(winner);
      }

      getRouter().detachTicTacToe();
      getRouter().attachOffGame();
    }
  }

}
