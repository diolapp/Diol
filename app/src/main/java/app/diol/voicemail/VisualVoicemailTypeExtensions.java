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

/**
 * Extended types used by {@link android.provider.VoicemailContract.Status#SOURCE_TYPE} not defined
 * in {@link android.telephony.TelephonyManager}. {@link
 * android.telephony.TelephonyManager#VVM_TYPE_OMTP} and {@link
 * android.telephony.TelephonyManager#VVM_TYPE_CVVM} are already defined.
 */
public class VisualVoicemailTypeExtensions {

    // Protocol used by Verizon wireless
    public static final String VVM_TYPE_VVM3 = "vvm_type_vvm3";
}
