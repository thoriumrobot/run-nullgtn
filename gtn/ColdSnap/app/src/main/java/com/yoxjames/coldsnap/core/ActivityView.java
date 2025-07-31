package com.yoxjames.coldsnap.core;

import io.reactivex.Observable;
import javax.annotation.Nullable;

public interface ActivityView extends MvpView {

    Observable<Integer> onBottomNavigation();

    void openSettings();

    void openPlants();

    void openFeed();
}
