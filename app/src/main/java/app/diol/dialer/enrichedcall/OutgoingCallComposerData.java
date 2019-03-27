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

package app.diol.dialer.enrichedcall;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import app.diol.dialer.common.Assert;

/**
 * Value type holding references to all data that could be provided for the call composer.
 *
 * <p>Note: Either the subject, the image data, or both must be specified, e.g.
 *
 * <pre>
 *   OutgoingCallComposerData.builder.build(); // throws exception, no data set
 *   OutgoingCallComposerData.builder
 *       .setText(subject)
 *       .build(); // Success
 *   OutgoingCallComposerData.builder
 *       .setImageData(uri, contentType)
 *       .build(); // Success
 *   OutgoingCallComposerData.builder
 *      .setText(subject)
 *      .setImageData(uri, contentType)
 *      .build(); // Success
 * </pre>
 */
@AutoValue
public abstract class OutgoingCallComposerData {

    public static Builder builder() {
        return new AutoValue_OutgoingCallComposerData.Builder();
    }

    public boolean hasImageData() {
        return getImageUri() != null && getImageContentType() != null;
    }

    @Nullable
    public abstract String getSubject();

    @Nullable
    public abstract Uri getImageUri();

    @Nullable
    public abstract String getImageContentType();

    /**
     * Builds instances of {@link OutgoingCallComposerData}.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setSubject(String subject);

        public Builder setImageData(@NonNull Uri imageUri, @NonNull String imageContentType) {
            setImageUri(Assert.isNotNull(imageUri));
            setImageContentType(Assert.isNotNull(imageContentType));
            return this;
        }

        abstract Builder setImageUri(Uri imageUri);

        abstract Builder setImageContentType(String imageContentType);

        abstract OutgoingCallComposerData autoBuild();

        /**
         * Returns the OutgoingCallComposerData from this builder.
         *
         * @return the OutgoingCallComposerData.
         * @throws IllegalStateException if neither {@link #setSubject(String)} nor {@link
         *                               #setImageData(Uri, String)} were called.
         */
        public OutgoingCallComposerData build() {
            OutgoingCallComposerData data = autoBuild();
            Assert.checkState(data.getSubject() != null || data.hasImageData());
            return data;
        }
    }
}
