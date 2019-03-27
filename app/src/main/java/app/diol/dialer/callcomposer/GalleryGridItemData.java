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

package app.diol.dialer.callcomposer;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.util.Objects;

import app.diol.dialer.common.Assert;

/**
 * Provides data for GalleryGridItemView
 */
public final class GalleryGridItemData implements Parcelable {
    public static final String[] IMAGE_PROJECTION =
            new String[]{Media._ID, Media.DATA, Media.MIME_TYPE, Media.DATE_MODIFIED};
    public static final Creator<GalleryGridItemData> CREATOR =
            new Creator<GalleryGridItemData>() {
                @Override
                public GalleryGridItemData createFromParcel(Parcel in) {
                    return new GalleryGridItemData(in);
                }

                @Override
                public GalleryGridItemData[] newArray(int size) {
                    return new GalleryGridItemData[size];
                }
            };
    private static final int INDEX_DATA_PATH = 1;
    private static final int INDEX_MIME_TYPE = 2;
    private static final int INDEX_DATE_MODIFIED = 3;
    private String filePath;
    private String mimeType;
    private long dateModifiedSeconds;

    public GalleryGridItemData() {
    }

    public GalleryGridItemData(GalleryGridItemData copyData) {
        filePath = Assert.isNotNull(copyData.getFilePath());
        mimeType = Assert.isNotNull(copyData.getMimeType());
        dateModifiedSeconds = Assert.isNotNull(copyData.getDateModifiedSeconds());
    }

    public GalleryGridItemData(Cursor cursor) {
        bind(cursor);
    }

    private GalleryGridItemData(Parcel in) {
        filePath = in.readString();
        mimeType = in.readString();
        dateModifiedSeconds = in.readLong();
    }

    public void bind(Cursor cursor) {
        mimeType = Assert.isNotNull(cursor.getString(INDEX_MIME_TYPE));
        String dateModified = Assert.isNotNull(cursor.getString(INDEX_DATE_MODIFIED));
        dateModifiedSeconds = !TextUtils.isEmpty(dateModified) ? Long.parseLong(dateModified) : -1;
        filePath = Assert.isNotNull(cursor.getString(INDEX_DATA_PATH));
    }

    @Nullable
    public String getFilePath() {
        return filePath;
    }

    @Nullable
    public Uri getFileUri() {
        return TextUtils.isEmpty(filePath) ? null : Uri.fromFile(new File(filePath));
    }

    /**
     * @return The date in seconds. This can be negative if we could not retrieve date info
     */
    public long getDateModifiedSeconds() {
        return dateModifiedSeconds;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GalleryGridItemData
                && Objects.equals(mimeType, ((GalleryGridItemData) obj).mimeType)
                && Objects.equals(filePath, ((GalleryGridItemData) obj).filePath)
                && ((GalleryGridItemData) obj).dateModifiedSeconds == dateModifiedSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, mimeType, dateModifiedSeconds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(mimeType);
        dest.writeLong(dateModifiedSeconds);
    }
}
