package com.yoxjames.coldsnap.core.view;

import javax.annotation.Nullable;

public interface BaseColdsnapView<VM> {

    void bindView(@Nullable() VM vm);
}
