package com.uber.rib.root.logged_out;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import android.widget.TextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.uber.rib.app.R;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Top level view for {@link LoggedOutBuilder.LoggedOutScope}.
 */
class LoggedOutView extends LinearLayout implements LoggedOutInteractor.LoggedOutPresenter {

  public LoggedOutView(Context context) {
    this(context, null);
  }

  public LoggedOutView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LoggedOutView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public Observable<Pair<String, String>> loginName() {
    return RxView.clicks(findViewById(R.id.login_button))
      .map(new Function<Object, Pair<String, String>>() {
        @Override
        public Pair<String, String> apply(Object o) throws Exception {
          TextView playerNameOne = (TextView) findViewById(R.id.player_name_1);
          TextView playerNameTwo = (TextView) findViewById(R.id.player_name_2);
          return Pair.create(playerNameOne.getText().toString(), playerNameTwo.getText().toString());
        }
      });
  }
}
