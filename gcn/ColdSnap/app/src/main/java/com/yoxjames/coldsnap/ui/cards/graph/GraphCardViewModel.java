package com.yoxjames.coldsnap.ui.cards.graph;

import com.google.auto.value.AutoValue;
import com.yoxjames.coldsnap.ui.cards.FeedCardViewModel;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;

@AutoValue
public abstract class GraphCardViewModel implements FeedCardViewModel {

    public abstract String getTitle();

    public abstract GraphConfig getGraphConfig();

    public abstract List<GraphLine> getRangeLines();

    public abstract List<DataSeries> getDataSeries();

    public abstract Function<Double, String> getXFormatter();

    public abstract Function<Double, String> getYFormatter();

    public static Builder builder() {
        return new AutoValue_GraphCardViewModel.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {

        public abstract Builder title(@Nullable() String title);

        public abstract Builder graphConfig(@Nullable() GraphConfig graphConfig);

        public abstract Builder rangeLines(@Nullable() List<GraphLine> rangeLines);

        public abstract Builder dataSeries(@Nullable() List<DataSeries> dataSeries);

        public abstract Builder XFormatter(@Nullable() Function<Double, String> xFormatter);

        public abstract Builder YFormatter(@Nullable() Function<Double, String> yFormatter);

        public abstract GraphCardViewModel build();
    }
}
