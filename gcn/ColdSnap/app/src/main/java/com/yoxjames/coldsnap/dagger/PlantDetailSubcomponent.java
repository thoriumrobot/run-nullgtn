package com.yoxjames.coldsnap.dagger;

import com.yoxjames.coldsnap.ui.detail.PlantDetailActivity;
import dagger.Subcomponent;
import javax.annotation.Nullable;

@ActivityScope
@Subcomponent(modules = { PlantDetailModule.class })
public interface PlantDetailSubcomponent {

    void inject(@Nullable() PlantDetailActivity activity);
}
