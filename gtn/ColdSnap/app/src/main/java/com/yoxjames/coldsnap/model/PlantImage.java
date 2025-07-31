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
package com.yoxjames.coldsnap.model;

import com.google.auto.value.AutoValue;
import com.yoxjames.coldsnap.ui.plantimage.PlantImageSaveRequest;
import org.threeten.bp.Instant;
import java.util.UUID;
import static com.yoxjames.coldsnap.util.CSUtils.EMPTY_UUID;
import javax.annotation.Nullable;

/**
 * Created by yoxjames on 10/14/17.
 */
@AutoValue
public abstract class PlantImage {

    public static PlantImage NO_IMAGE = builder().setTitle("").setImageDate(Instant.MIN).setFileName("").setImageUUID(EMPTY_UUID).setPlantUUID(EMPTY_UUID).build();

    @Nullable()
    public abstract String getTitle();

    @Nullable()
    public abstract Instant getImageDate();

    @Nullable()
    public abstract String getFileName();

    @Nullable()
    public abstract UUID getImageUUID();

    @Nullable()
    public abstract UUID getPlantUUID();

    public static Builder builder() {
        return new AutoValue_PlantImage.Builder();
    }

    public static PlantImage fromPlantImageSaveRequest(PlantImageSaveRequest plantImageSaveRequest) {
        return builder().setTitle("").setImageDate(plantImageSaveRequest.getPhotoTime()).setFileName(plantImageSaveRequest.getFileName()).setImageUUID(UUID.randomUUID()).setPlantUUID(plantImageSaveRequest.getPlantUUID()).build();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        @Nullable()
        public abstract Builder setTitle(@Nullable() String title);

        @Nullable()
        public abstract Builder setImageDate(@Nullable() Instant imageDate);

        @Nullable()
        public abstract Builder setFileName(@Nullable() String fileName);

        @Nullable()
        public abstract Builder setImageUUID(@Nullable() UUID imageUUID);

        @Nullable()
        public abstract Builder setPlantUUID(@Nullable() UUID plantUUID);

        @Nullable()
        public abstract PlantImage build();
    }
}
