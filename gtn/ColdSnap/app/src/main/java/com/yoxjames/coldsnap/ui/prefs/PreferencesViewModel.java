package com.yoxjames.coldsnap.ui.prefs;

import com.google.auto.value.AutoValue;
import com.yoxjames.coldsnap.ui.controls.temperaturepicker.TemperaturePickerViewModel;
import javax.annotation.Nullable;

@AutoValue
public abstract class PreferencesViewModel {

    public abstract TemperaturePickerViewModel getThresholdViewModel();

    public abstract TemperaturePickerViewModel getFuzzViewModel();

    public abstract String getThresholdSummary();

    public abstract String getFuzzSummary();

    public abstract String getTemperatureScale();

    public abstract String getColdAlarmTime();

    public static Builder builder() {
        return new AutoValue_PreferencesViewModel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setThresholdViewModel(@Nullable() TemperaturePickerViewModel vm);

        public abstract Builder setFuzzViewModel(@Nullable() TemperaturePickerViewModel vm);

        public abstract Builder setThresholdSummary(@Nullable() String thresholdSummary);

        public abstract Builder setFuzzSummary(@Nullable() String fuzzSummary);

        public abstract Builder setTemperatureScale(@Nullable() String temperatureScale);

        public abstract Builder setColdAlarmTime(@Nullable() String coldAlarmTime);

        public abstract PreferencesViewModel build();
    }
}
