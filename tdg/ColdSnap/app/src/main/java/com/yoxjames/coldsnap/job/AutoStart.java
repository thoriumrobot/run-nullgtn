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
package com.yoxjames.coldsnap.job;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.yoxjames.coldsnap.ColdSnapApplication;
import com.yoxjames.coldsnap.prefs.CSPreferences;
import javax.inject.Inject;
import static com.yoxjames.coldsnap.job.ColdAlarm.setAlarm;
import androidx.annotation.Nullable;

/**
 * Class designed to start the ColdAlarm at boot time. This will allow ColdSnap to warn
 * the user of hazards even if the app is never launched.
 */
public class AutoStart extends BroadcastReceiver {

    @Inject
    CSPreferences csPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((ColdSnapApplication) context.getApplicationContext()).getInjector().inject(this);
        @Nullable final String action = (intent.getAction() == null ? "" : intent.getAction());
        if (action.equals(Intent.ACTION_BOOT_COMPLETED))
            setAlarm(context, csPreferences);
    }
}
