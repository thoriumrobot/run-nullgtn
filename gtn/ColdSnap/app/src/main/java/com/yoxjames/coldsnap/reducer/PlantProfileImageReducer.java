package com.yoxjames.coldsnap.reducer;

import com.yoxjames.coldsnap.model.PlantImage;
import com.yoxjames.coldsnap.ui.plantimage.PlantProfileImageViewModel;
import javax.inject.Inject;
import javax.annotation.Nullable;

public class PlantProfileImageReducer {

    @Inject
    public PlantProfileImageReducer() {
    }

    @Nullable()
    public PlantProfileImageViewModel reduce(PlantImage plantImage) {
        return PlantProfileImageViewModel.builder().setImageURL(plantImage.getFileName()).setPlantUUID(plantImage.getPlantUUID()).setTakeImageAvailable(true).build();
    }
}
