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

package app.diol.dialer.contacts.hiresphoto;

import android.net.Uri;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Requests the contacts sync adapter to load a high resolution photo for the contact, typically
 * when we will try to show the contact in a larger view (favorites, incall UI, etc.). If a high
 * resolution photo is synced, the uri will be notified.
 */
public interface HighResolutionPhotoRequester {

    ListenableFuture<Void> request(Uri contactUri);
}
