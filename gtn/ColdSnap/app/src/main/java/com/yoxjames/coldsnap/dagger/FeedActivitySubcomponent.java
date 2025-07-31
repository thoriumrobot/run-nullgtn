package com.yoxjames.coldsnap.dagger;

import com.yoxjames.coldsnap.ui.feed.ActivityFeed;
import dagger.Subcomponent;
import javax.annotation.Nullable;

@Subcomponent(modules = { FeedModule.class })
@ActivityScope
public interface FeedActivitySubcomponent {

    void inject(@Nullable() ActivityFeed activityFeed);
}
