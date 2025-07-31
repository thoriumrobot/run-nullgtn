package com.yoxjames.coldsnap.ui.prefs;

import com.yoxjames.coldsnap.core.MvpView;
import io.reactivex.Observable;
import javax.annotation.Nullable;

public interface CSPreferencesView extends MvpView {

    void bindView(@Nullable() PreferencesViewModel viewModel);

    Observable<Integer> thresholdChanges();

    Observable<Integer> fuzzChanges();
}
