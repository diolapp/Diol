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

package app.diol.contacts.common.model.dataitem;

import android.content.ContentValues;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts.Photo;

/**
 * Represents a photo data item, wrapping the columns in {@link ContactsContract.Contacts.Photo}.
 */
public class PhotoDataItem extends DataItem {

    /* package */ PhotoDataItem(ContentValues values) {
        super(values);
    }

    public Long getPhotoFileId() {
        return getContentValues().getAsLong(Photo.PHOTO_FILE_ID);
    }

    public byte[] getPhoto() {
        return getContentValues().getAsByteArray(Photo.PHOTO);
    }
}
