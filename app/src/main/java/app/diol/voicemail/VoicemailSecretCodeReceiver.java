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

package app.diol.voicemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.diol.dialer.common.LogUtil;

/**
 * Receives android.provider.Telephony.SECRET_CODE
 */
public class VoicemailSecretCodeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String host = intent.getData().getHost();
        if (!VoicemailClient.VOICEMAIL_SECRET_CODE.equals(host)) {
            return;
        }
        LogUtil.i("VoicemailSecretCodeReceiver.onReceive", "secret code received");
        VoicemailComponent.get(context).getVoicemailClient().showConfigUi(context);
    }
}
