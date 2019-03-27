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

package app.diol.dialer.multimedia;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.auto.value.AutoValue;

import app.diol.dialer.common.LogUtil;

/**
 * Holds data associated with a call.
 */
@AutoValue
public abstract class MultimediaData {

    public static final MultimediaData EMPTY = builder().build();

    @NonNull
    public static Builder builder() {
        return new AutoValue_MultimediaData.Builder().setImportant(false);
    }

    /**
     * Returns the text part of this data.
     *
     * <p>This field is used for both the call composer session and the post call note.
     */
    @Nullable
    public abstract String getText();

    /**
     * Returns the location part of this data.
     */
    @Nullable
    public abstract Location getLocation();

    /**
     * Returns {@code true} if this object contains image data.
     */
    public boolean hasImageData() {
        // imageUri and content are always either both null or nonnull
        return getImageUri() != null && getImageContentType() != null;
    }

    /**
     * Returns the image uri part of this object's image.
     */
    @Nullable
    public abstract Uri getImageUri();

    /**
     * Returns the content type part of this object's image, either image/png or image/jpeg.
     */
    @Nullable
    public abstract String getImageContentType();

    /**
     * Returns {@code true} if this data is marked as important.
     */
    public abstract boolean isImportant();

    /**
     * Returns true if this has image, text or location data.
     */
    public boolean hasData() {
        return hasImageData() || !TextUtils.isEmpty(getText()) || getLocation() != null;
    }

    /**
     * Returns the string form of this MultimediaData with no PII.
     */
    @Override
    public String toString() {
        return String.format(
                "MultimediaData{subject: %s, location: %s, imageUrl: %s, imageContentType: %s, "
                        + "important: %b}",
                LogUtil.sanitizePii(getText()),
                LogUtil.sanitizePii(getLocation()),
                LogUtil.sanitizePii(getImageUri()),
                getImageContentType(),
                isImportant());
    }

    /**
     * Creates instances of {@link MultimediaData}.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setText(@NonNull String subject);

        public abstract Builder setLocation(@NonNull Location location);

        public Builder setImage(@NonNull Uri image, @NonNull String imageContentType) {
            setImageUri(image);
            setImageContentType(imageContentType);
            return this;
        }

        abstract Builder setImageUri(@NonNull Uri image);

        abstract Builder setImageContentType(@NonNull String imageContentType);

        public abstract Builder setImportant(boolean isImportant);

        public abstract MultimediaData build();
    }
}
