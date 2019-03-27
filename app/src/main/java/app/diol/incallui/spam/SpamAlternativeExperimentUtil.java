/*
 *  Copyright (C) 2019  The Diol App Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.diol.incallui.spam;

import android.content.Context;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.configprovider.ConfigProviderComponent;

/**
 * Returns resource id based on experiment number.
 */
public final class SpamAlternativeExperimentUtil {

    /**
     * Returns the resource id using a resource name for an experiment where we want to use
     * alternative words for the keyword spam.
     */
    public static int getResourceIdByName(String resourceName, Context context) {
        long experiment =
                ConfigProviderComponent.get(context)
                        .getConfigProvider()
                        .getLong("experiment_for_alternative_spam_word", 230150);
        LogUtil.i(
                "SpamAlternativeExperimentUtil.getResourceIdByName", "using experiment %d", experiment);
        String modifiedResourceName = resourceName;
        if (experiment != 230150) {
            modifiedResourceName = resourceName + "_" + experiment;
        }
        int resourceId =
                context
                        .getResources()
                        .getIdentifier(modifiedResourceName, "string", context.getPackageName());
        if (resourceId == 0) {
            LogUtil.i(
                    "SpamAlternativeExperimentUtil.getResourceIdByName",
                    "not found experiment %d",
                    experiment);
            return context.getResources().getIdentifier(resourceName, "string", context.getPackageName());
        }
        return resourceId;
    }
}
