package com.yoxjames.coldsnap.ui.feed;

import com.yoxjames.coldsnap.core.ActivityView;
import javax.annotation.Nullable;

public interface FeedMvpView extends ActivityView {

    void bindView(@Nullable() FeedViewModel viewModel);
}
