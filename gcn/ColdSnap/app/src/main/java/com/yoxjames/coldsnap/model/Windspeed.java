package com.yoxjames.coldsnap.model;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class Windspeed {

    public abstract double getMetersPerSecond();

    public static Windspeed fromMetersPerSecond(double metersPerSecond) {
        return new AutoValue_Windspeed(metersPerSecond);
    }
}
