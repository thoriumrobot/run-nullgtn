package com.yoxjames.coldsnap.model;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nullable;

public interface TemperatureComparator {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ LESSER, MAYBE_LESSER, TRUE_EQUAL, MAYBE_GREATER, GREATER })
    @interface TemperatureComparison {
    }

    int LESSER = -2;

    int MAYBE_LESSER = -1;

    int TRUE_EQUAL = 0;

    int MAYBE_GREATER = 1;

    int GREATER = 2;

    @TemperatureComparison
    int compare(@Nullable() Temperature t1, @Nullable() Temperature t2);
}
