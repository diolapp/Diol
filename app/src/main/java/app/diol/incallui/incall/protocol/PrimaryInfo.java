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

package app.diol.incallui.incall.protocol;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Locale;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.multimedia.MultimediaData;

/**
 * Information about the primary call.
 */
@AutoValue
public abstract class PrimaryInfo {
    public static Builder builder() {
        return new AutoValue_PrimaryInfo.Builder();
    }

    public static PrimaryInfo empty() {
        return PrimaryInfo.builder()
                .setNameIsNumber(false)
                .setPhotoType(ContactPhotoType.DEFAULT_PLACEHOLDER)
                .setIsSipCall(false)
                .setIsContactPhotoShown(false)
                .setIsWorkCall(false)
                .setIsSpam(false)
                .setIsLocalContact(false)
                .setAnsweringDisconnectsOngoingCall(false)
                .setShouldShowLocation(false)
                .setShowInCallButtonGrid(true)
                .setNumberPresentation(-1)
                .build();
    }

    @Nullable
    public abstract String number();

    @Nullable
    public abstract String name();

    public abstract boolean nameIsNumber();

    // This is from contacts and shows the type of number. For example, "Mobile".
    @Nullable
    public abstract String label();

    @Nullable
    public abstract String location();

    @Nullable
    public abstract Drawable photo();

    @Nullable
    public abstract Uri photoUri();

    @ContactPhotoType
    public abstract int photoType();

    public abstract boolean isSipCall();

    public abstract boolean isContactPhotoShown();

    public abstract boolean isWorkCall();

    public abstract boolean isSpam();

    public abstract boolean isLocalContact();

    public abstract boolean answeringDisconnectsOngoingCall();

    public abstract boolean shouldShowLocation();

    // Used for consistent LetterTile coloring.
    @Nullable
    public abstract String contactInfoLookupKey();

    @Nullable
    public abstract MultimediaData multimediaData();

    public abstract boolean showInCallButtonGrid();

    public abstract int numberPresentation();

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "PrimaryInfo, number: %s, name: %s, location: %s, label: %s, "
                        + "photo: %s, photoType: %d, isPhotoVisible: %b, MultimediaData: %s",
                LogUtil.sanitizePhoneNumber(number()),
                LogUtil.sanitizePii(name()),
                LogUtil.sanitizePii(location()),
                label(),
                photo(),
                photoType(),
                isContactPhotoShown(),
                multimediaData());
    }

    /**
     * Builder class for primary call info.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setNumber(String number);

        public abstract Builder setName(String name);

        public abstract Builder setNameIsNumber(boolean nameIsNumber);

        public abstract Builder setLabel(String label);

        public abstract Builder setLocation(String location);

        public abstract Builder setPhoto(Drawable photo);

        public abstract Builder setPhotoUri(Uri photoUri);

        public abstract Builder setPhotoType(@ContactPhotoType int photoType);

        public abstract Builder setIsSipCall(boolean isSipCall);

        public abstract Builder setIsContactPhotoShown(boolean isContactPhotoShown);

        public abstract Builder setIsWorkCall(boolean isWorkCall);

        public abstract Builder setIsSpam(boolean isSpam);

        public abstract Builder setIsLocalContact(boolean isLocalContact);

        public abstract Builder setAnsweringDisconnectsOngoingCall(
                boolean answeringDisconnectsOngoingCall);

        public abstract Builder setShouldShowLocation(boolean shouldShowLocation);

        public abstract Builder setContactInfoLookupKey(String contactInfoLookupKey);

        public abstract Builder setMultimediaData(MultimediaData multimediaData);

        public abstract Builder setShowInCallButtonGrid(boolean showInCallButtonGrid);

        public abstract Builder setNumberPresentation(int numberPresentation);

        public abstract PrimaryInfo build();
    }
}
