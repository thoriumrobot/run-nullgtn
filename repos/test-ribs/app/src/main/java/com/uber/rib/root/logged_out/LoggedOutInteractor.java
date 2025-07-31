package com.uber.rib.root.logged_out;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.uber.rib.core.Bundle;
import com.uber.rib.core.Interactor;
import com.uber.rib.core.RibInteractor;
import com.uber.rib.core.Presenter;
import com.uber.rib.core.Router;
import com.uber.rib.root.UserName;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import javax.inject.Inject;

/**
 * Coordinates Business Logic for {@link LoggedOutScope}.
 *
 * TODO describe the logic of this scope.
 */
@RibInteractor
public class LoggedOutInteractor
    extends Interactor<LoggedOutInteractor.LoggedOutPresenter, LoggedOutRouter> {

  @Inject Listener listener;
  @Inject LoggedOutPresenter presenter;

  @Override
  protected void didBecomeActive(@Nullable Bundle savedInstanceState) {
    super.didBecomeActive(savedInstanceState);

    presenter
      .loginName()
      .subscribe(new Consumer<Pair<String, String>>() {
        @Override
        public void accept(Pair<String, String> names) throws Exception {
          if (!isEmpty(names.first) && !isEmpty(names.second)) {
            listener.login(UserName.create(names.first), UserName.create(names.second));
          }
        }
      });
  }

  private boolean isEmpty(@Nullable String string) {
    return string == null || string.length() == 0;
  }

  @Override
  protected void willResignActive() {
    super.willResignActive();

    // TODO: Perform any required clean up here, or delete this method entirely if not needed.
  }


  /**
   * Presenter interface implemented by this RIB's view.
   */
  interface LoggedOutPresenter {
    Observable<Pair<String, String>> loginName();
  }

  public interface Listener {
    void login(UserName playerOne, UserName playerTwo);
  }
}
