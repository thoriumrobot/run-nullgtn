/*
 * Copyright (c) 2017 James Yox
 *
 * This file is part of ColdSnap.
 *
 * ColdSnap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ColdSnap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ColdSnap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.yoxjames.coldsnap.ui.plantlist;

import android.support.annotation.IntDef;
import com.google.auto.value.AutoValue;
import java.lang.annotation.Retention;
import java.util.UUID;
import static com.yoxjames.coldsnap.util.CSUtils.EMPTY_UUID;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import javax.annotation.Nullable;

/**
 * Created by yoxjames on 10/15/17.
 */
@AutoValue
public abstract class PlantListItemViewModel {

    @Retention(SOURCE)
    @IntDef({ ERROR, PENDING, HAPPY, NEUTRAL, SAD, DEAD })
    public @interface PlantStatus {
    }

    public static final int ERROR = -1;

    public static final int PENDING = 0;

    public static final int HAPPY = 1;

    public static final int NEUTRAL = 2;

    public static final int SAD = 3;

    public static final int DEAD = 4;

    public static PlantListItemViewModel EMPTY = builder().plantName("").plantScientificName("").plantStatus(PENDING).uuid(EMPTY_UUID).imageFileName("").build();

    public abstract String getPlantName();

    public abstract String getPlantScientificName();

    @PlantStatus
    public abstract int getPlantStatus();

    public abstract UUID getUuid();

    public abstract String getImageFileName();

    public static Builder builder() {
        return new AutoValue_PlantListItemViewModel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        @Nullable()
        public abstract Builder plantName(@Nullable() String plantName);

        public abstract Builder plantScientificName(@Nullable() String scientificName);

        public abstract Builder plantStatus(@PlantStatus @Nullable() int status);

        @Nullable()
        public abstract Builder uuid(@Nullable() UUID uuid);

        @Nullable()
        public abstract Builder imageFileName(@Nullable() String fileName);

        @Nullable()
        public abstract PlantListItemViewModel build();
    }
}
