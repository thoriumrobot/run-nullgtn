package com.yoxjames.coldsnap.ui.detail;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class PlantDetailViewUpdate {

    public abstract String getName();

    public abstract String getScientificName();

    public abstract int getTempVal();

    public static Builder builder() {
        return new AutoValue_PlantDetailViewUpdate.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {

        abstract Builder name(@Nullable() String name);

        abstract Builder scientificName(@Nullable() String scientificName);

        abstract Builder tempVal(@Nullable() int tempVal);

        abstract PlantDetailViewUpdate build();
    }
}
