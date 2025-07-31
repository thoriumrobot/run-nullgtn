package com.yoxjames.coldsnap.reducer;

import com.yoxjames.coldsnap.model.Plant;
import com.yoxjames.coldsnap.model.PlantImage;
import com.yoxjames.coldsnap.ui.detail.PlantDetailViewModel;
import javax.inject.Inject;
import javax.annotation.Nullable;

public class PlantDetailReducer {

    private final PlantDetailTemperaturePickerReducer plantDetailTemperaturePickerReducer;

    private final PlantProfileImageReducer plantProfileImageReducer;

    @Inject
    public PlantDetailReducer(PlantDetailTemperaturePickerReducer plantDetailTemperaturePickerReducer, PlantProfileImageReducer plantProfileImageReducer) {
        this.plantDetailTemperaturePickerReducer = plantDetailTemperaturePickerReducer;
        this.plantProfileImageReducer = plantProfileImageReducer;
    }

    @Nullable()
    public PlantDetailViewModel reduce(Plant plant, PlantImage plantImage) {
        return PlantDetailViewModel.builder().name(plant.getName()).scientificName(plant.getScientificName()).temperaturePickerViewModel(plantDetailTemperaturePickerReducer.reduce(plant)).plantProfileImageViewModel(plantProfileImageReducer.reduce(plantImage)).build();
    }
}
