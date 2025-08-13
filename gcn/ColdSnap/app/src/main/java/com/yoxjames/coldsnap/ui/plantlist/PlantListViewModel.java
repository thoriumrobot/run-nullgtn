package com.yoxjames.coldsnap.ui.plantlist;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class PlantListViewModel {

    public static PlantListViewModel EMPTY = create(Collections.emptyList());

    abstract List<PlantListItemViewModel> getPlantItemViewModels();

    public static PlantListViewModel create(List<PlantListItemViewModel> plantListItemViewModels) {
        return new AutoValue_PlantListViewModel(plantListItemViewModels);
    }
}
