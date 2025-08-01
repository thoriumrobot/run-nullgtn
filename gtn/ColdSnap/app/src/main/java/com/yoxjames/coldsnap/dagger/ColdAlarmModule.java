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
package com.yoxjames.coldsnap.dagger;

import android.content.SharedPreferences;
import com.yoxjames.coldsnap.job.ColdAlarmPresenter;
import com.yoxjames.coldsnap.job.ColdAlarmView;
import com.yoxjames.coldsnap.model.TemperatureFormatter;
import com.yoxjames.coldsnap.service.plant.PlantService;
import com.yoxjames.coldsnap.service.weather.WeatherService;
import dagger.Module;
import dagger.Provides;
import javax.annotation.Nullable;

@Module
public class ColdAlarmModule {

    private final ColdAlarmView view;

    public ColdAlarmModule(ColdAlarmView view) {
        this.view = view;
    }

    @Provides
    ColdAlarmView provideColdAlarmView() {
        return view;
    }

    @Provides
    static ColdAlarmPresenter provideColdAlarmPresenter(ColdAlarmView view, WeatherService weatherService, PlantService plantService, SharedPreferences sharedPreferences, TemperatureFormatter formatter) {
        return new ColdAlarmPresenter(view, weatherService, plantService, sharedPreferences, formatter);
    }
}
