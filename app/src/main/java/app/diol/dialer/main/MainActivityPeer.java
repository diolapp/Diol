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

package app.diol.dialer.main;

import android.content.Intent;
import android.os.Bundle;

/**
 * Interface for peers of MainActivity.
 */
public interface MainActivityPeer {

    void onActivityCreate(Bundle saveInstanceState);

    void onActivityResume();

    void onUserLeaveHint();

    void onActivityPause();

    void onActivityStop();

    void onActivityDestroyed();

    void onNewIntent(Intent intent);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onSaveInstanceState(Bundle bundle);

    boolean onBackPressed();

    /**
     * Supplies the MainActivityPeer
     */
    interface PeerSupplier {

        MainActivityPeer getPeer();
    }
}
