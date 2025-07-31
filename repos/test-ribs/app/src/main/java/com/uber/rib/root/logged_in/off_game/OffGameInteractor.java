package com.uber.rib.root.logged_in.off_game;

import android.support.annotation.Nullable;
import com.google.common.collect.ImmutableMap;
import com.uber.autodispose.ObservableScoper;

import com.uber.rib.core.Bundle;
import com.uber.rib.core.Interactor;
import com.uber.rib.core.RibInteractor;

import com.uber.rib.root.UserName;
import com.uber.rib.root.logged_in.ScoreStream;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Coordinates Business Logic for {@link OffGameScope}.
 *
 * TODO describe the logic of this scope.
 */
@RibInteractor
public class OffGameInteractor
    extends Interactor<OffGameInteractor.OffGamePresenter, OffGameRouter> {

  @Inject @Named("player_one") UserName playerOne;
  @Inject @Named("player_two") UserName playerTwo;
  @Inject Listener listener;
  @Inject OffGamePresenter presenter;
  @Inject ScoreStream scoreStream;

  @Override
  protected void didBecomeActive(@Nullable Bundle savedInstanceState) {
    super.didBecomeActive(savedInstanceState);

    presenter.setPlayerNames(playerOne.getUserName(), playerTwo.getUserName());
    presenter
      .startGameRequest()
      .subscribe(new Consumer<Object>() {
        @Override
        public void accept(Object object) throws Exception {
          listener.onStartGame();
        }
      });

    scoreStream.scores()
      .to(new ObservableScoper<ImmutableMap<UserName, Integer>>(this))
      .subscribe(new Consumer<ImmutableMap<UserName,Integer>>() {
        @Override
        public void accept(ImmutableMap<UserName, Integer> scores)
            throws Exception {
          Integer playerOneScore = scores.get(playerOne);
          Integer playerTwoScore = scores.get(playerTwo);
          presenter.setScores(playerOneScore, playerTwoScore);
        }
      });
  }

  public interface Listener {
    void onStartGame();
  }

  /**
   * Presenter interface implemented by this RIB's view.
   */
  interface OffGamePresenter {
    void setPlayerNames(String playerOne, String playerTwo);
    void setScores(@Nullable Integer playerOneScore, @Nullable Integer playerTwoScore);
    Observable<Object> startGameRequest();
  }
}
