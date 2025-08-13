package com.yoxjames.coldsnap.ui.cards.graph;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class GraphConfig {

    public abstract double getRangeLowerBound();

    public abstract double getRangeUpperBound();

    public abstract double getDomainLowerBound();

    public abstract double getDomainUpperBound();

    public abstract double getRangeStep();

    public abstract double getDomainStep();

    public static Builder builder() {
        return new AutoValue_GraphConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder rangeLowerBound(@Nullable() double rangeLowerBound);

        public abstract Builder rangeUpperBound(@Nullable() double rangeUpperBound);

        public abstract Builder domainLowerBound(@Nullable() double domainLowerBound);

        public abstract Builder domainUpperBound(@Nullable() double domainUpperBound);

        public abstract Builder rangeStep(@Nullable() double rangeStep);

        public abstract Builder domainStep(@Nullable() double domainStep);

        public abstract GraphConfig build();
    }
}
