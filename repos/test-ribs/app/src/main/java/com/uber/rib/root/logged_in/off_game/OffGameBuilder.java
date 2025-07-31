package com.uber.rib.root.logged_in.off_game;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.uber.rib.root.UserName;
import com.uber.rib.root.logged_in.ScoreStream;

import com.uber.rib.core.InteractorBaseComponent;
import com.uber.rib.core.ViewBuilder;
import com.uber.rib.app.R;
import java.lang.annotation.Retention;

import java.util.List;

import javax.inject.Named;
import javax.inject.Scope;
import javax.inject.Qualifier;

import dagger.Provides;
import dagger.Binds;
import dagger.BindsInstance;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Builder for the {@link OffGameScope}.
 *
 * TODO describe this scope's responsibility as a whole.
 */
public class OffGameBuilder
    extends ViewBuilder<OffGameView, OffGameRouter, OffGameBuilder.ParentComponent> {

  public OffGameBuilder(ParentComponent dependency) {
    super(dependency);
  }

  /**
   * Builds a new {@link OffGameRouter}.
   *
   * @param parentViewGroup parent view group that this router's view will be added to.
   * @return a new {@link OffGameRouter}.
   */
  public OffGameRouter build(ViewGroup parentViewGroup) {
    OffGameView view = createView(parentViewGroup);
    OffGameInteractor interactor = new OffGameInteractor();
    Component component = DaggerOffGameBuilder_Component.builder()
        .parentComponent(getDependency())
        .view(view)
        .interactor(interactor)
        .build();
    return component.offgameRouter();
  }

  @Override
  protected OffGameView inflateView(LayoutInflater inflater, ViewGroup parentViewGroup) {
    return (OffGameView) inflater.inflate(R.layout.off_game_rib, parentViewGroup, false);
  }

  public interface ParentComponent {
    @Named("player_one") UserName playerOne();
    @Named("player_two") UserName playerTwo();
    OffGameInteractor.Listener listener();
    ScoreStream scoreStream();
  }

  @dagger.Module
  public abstract static class Module {

    @OffGameScope
    @Binds
    abstract OffGameInteractor.OffGamePresenter presenter(OffGameView view);

    @OffGameScope
    @Provides
    static OffGameRouter router(
      Component component,
      OffGameView view,
      OffGameInteractor interactor) {
      return new OffGameRouter(view, interactor, component);
    }

    // TODO: Create provider methods for dependencies created by this Rib. These should be static.
  }

  @OffGameScope
  @dagger.Component(modules = Module.class,
       dependencies = ParentComponent.class)
  interface Component extends InteractorBaseComponent<OffGameInteractor>, BuilderComponent {

    @dagger.Component.Builder
    interface Builder {
      @BindsInstance
      Builder interactor(OffGameInteractor interactor);
      @BindsInstance
      Builder view(OffGameView view);
      Builder parentComponent(ParentComponent component);
      Component build();
    }
  }

  interface BuilderComponent  {
    OffGameRouter offgameRouter();
  }

  @Scope
  @Retention(CLASS)
  @interface OffGameScope { }

  @Qualifier
  @Retention(CLASS)
  @interface OffGameInternal { }
}
