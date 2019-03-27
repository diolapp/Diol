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

package app.diol.voicemail.impl.configui;

import android.content.Intent;
import android.preference.PreferenceActivity;
import android.provider.VoicemailContract;

import java.util.List;

/**
 * Activity launched by simulator->voicemail, provides debug features.
 */
@SuppressWarnings("FragmentInjection") // not exported
public class VoicemailSecretCodeActivity extends PreferenceActivity {

    private Header syncHeader;

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        syncHeader = new Header();
        syncHeader.title = "Sync";
        target.add(syncHeader);

        Header configOverride = new Header();
        configOverride.fragment = ConfigOverrideFragment.class.getName();
        configOverride.title = "VVM config override";
        target.add(configOverride);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        if (header == syncHeader) {
            Intent intent = new Intent(VoicemailContract.ACTION_SYNC_VOICEMAIL);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
            return;
        }
        super.onHeaderClick(header, position);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
