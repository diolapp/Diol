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

package app.diol.dialer.constants;

/**
 * Registry of tags for {@link android.net.TrafficStats#setThreadStatsTag(int)}
 */
public class TrafficStatsTags {
    public static final int CONTACT_PHOTO_DOWNLOAD_TAG = 0x00000001;
    public static final int NEARBY_PLACES_TAG = 0x00000002;
    public static final int REVERSE_LOOKUP_CONTACT_TAG = 0x00000003;
    public static final int REVERSE_LOOKUP_IMAGE_TAG = 0x00000004;
    public static final int DOWNLOAD_LOCATION_MAP_TAG = 0x00000005;
    public static final int REVERSE_GEOCODE_TAG = 0x00000006;
    public static final int VISUAL_VOICEMAIL_TAG = 0x00000007;

    // 0xfffffe00 to 0xffffff00 reserved for proprietary extensions to the dialer app.

    // 0xffffff00 to 0xffffffff reserved by the system (see TrafficStats#getAndSetThreadStatsTag)

}
