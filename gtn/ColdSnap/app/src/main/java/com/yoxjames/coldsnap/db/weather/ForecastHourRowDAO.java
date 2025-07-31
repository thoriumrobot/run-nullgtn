package com.yoxjames.coldsnap.db.weather;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;
import javax.annotation.Nullable;

@Dao
public interface ForecastHourRowDAO {

    @Insert
    void addForecastHours(@Nullable() List<ForecastHourRow> rows);

    @Query("DELETE FROM forecast_hour")
    void clearForecastHourRows();

    @Query("SELECT * FROM forecast_hour WHERE lat = :lat AND lon = :lon")
    List<ForecastHourRow> getForecastHours(@Nullable() double lat, @Nullable() double lon);
}
