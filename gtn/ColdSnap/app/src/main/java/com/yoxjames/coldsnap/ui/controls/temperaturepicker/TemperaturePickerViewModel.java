package com.yoxjames.coldsnap.ui.controls.temperaturepicker;

import com.google.auto.value.AutoValue;
import com.yoxjames.coldsnap.service.preferences.CSPreferencesService.TemperatureFormat;
import static com.yoxjames.coldsnap.service.preferences.CSPreferencesService.CELSIUS;
import javax.annotation.Nullable;

@AutoValue
public abstract class TemperaturePickerViewModel {

    public static TemperaturePickerViewModel EMPTY = builder().setFormat(CELSIUS).setMaxValue(100).setMinValue(0).setValue(0).build();

    public abstract int getValue();

    public abstract int getMinValue();

    public abstract int getMaxValue();

    @TemperatureFormat
    public abstract int getFormat();

    public static Builder builder() {
        return new AutoValue_TemperaturePickerViewModel.Builder();
    }

    public abstract Builder toBuilder();

    @Nullable()
    public TemperaturePickerViewModel withValue(int value) {
        return toBuilder().setValue(value).build();
    }

    @AutoValue.Builder
    public static abstract class Builder {

        @Nullable()
        public abstract Builder setValue(@Nullable() int value);

        @Nullable()
        public abstract Builder setMinValue(@Nullable() int minValue);

        public abstract Builder setMaxValue(@Nullable() int maxValue);

        @Nullable()
        public abstract Builder setFormat(@TemperatureFormat @Nullable() int format);

        @Nullable()
        public abstract TemperaturePickerViewModel build();
    }
}
