package com.yoxjames.coldsnap.prefs;

import io.reactivex.Completable;
import io.reactivex.Observable;
import javax.annotation.Nullable;

public interface CSRxPreferences {

    Observable<PreferenceModel> getPreferences();

    @Nullable()
    Completable savePreferences(PreferenceModel preferenceModel);
}
