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

package app.diol.dialer.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Main activity intents.
 *
 * <p>TODO(calderwoodra): Move this elsewhere.
 */
public class MainComponent {

    public static final String EXTRA_CLEAR_NEW_VOICEMAILS = "EXTRA_CLEAR_NEW_VOICEMAILS";

    /**
     * @param context Context of the application package implementing MainActivity class.
     * @return intent for MainActivity.class
     */
    public static Intent getIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, getComponentName()));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getShowCallLogIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, getComponentName()));
        intent.setAction("ACTION_SHOW_TAB");
        intent.putExtra("EXTRA_SHOW_TAB", 1);
        return intent;
    }

    public static Intent getShowVoicemailIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, getComponentName()));
        intent.setAction("ACTION_SHOW_TAB");
        intent.putExtra("EXTRA_SHOW_TAB", 3);
        intent.putExtra(EXTRA_CLEAR_NEW_VOICEMAILS, true);
        return intent;
    }

    private static String getComponentName() {
        return "app.diol.dialer.app.DialtactsActivity";
    }
}
