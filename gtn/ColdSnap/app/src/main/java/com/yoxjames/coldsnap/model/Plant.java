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
import java.util.UUID;
import javax.annotation.Nullable;

@AutoValue
public abstract class Plant implements Comparable<Plant> {

    public abstract String getName();

    @Nullable()
    public abstract String getScientificName();

    @Nullable()
    public abstract Temperature getMinimumTolerance();

    public abstract UUID getUuid();

    public static Builder builder() {
        return new AutoValue_Plant.Builder();
    }

    @Override
    public int compareTo(Plant plant) {
        return plant.getName().compareTo(this.getName());
    }

    @AutoValue.Builder
    public static abstract class Builder {

        @Nullable()
        public abstract Builder name(@Nullable() String name);

        @Nullable()
        public abstract Builder scientificName(@Nullable() String scientificName);

        @Nullable()
        public abstract Builder minimumTolerance(@Nullable() Temperature minimumTolerance);

        @Nullable()
        public abstract Builder uuid(@Nullable() UUID uuid);

        @Nullable()
        public abstract Plant build();
    }
}
