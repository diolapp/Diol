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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Locale;

import app.diol.dialer.common.LogUtil;

/**
 * Information about the secondary call.
 */
@AutoValue
public abstract class SecondaryInfo implements Parcelable {
    public static final Creator<SecondaryInfo> CREATOR =
            new Creator<SecondaryInfo>() {
                @Override
                public SecondaryInfo createFromParcel(Parcel in) {
                    return builder()
                            .setShouldShow(in.readByte() != 0)
                            .setName(in.readString())
                            .setNameIsNumber(in.readByte() != 0)
                            .setLabel(in.readString())
                            .setProviderLabel(in.readString())
                            .setIsConference(in.readByte() != 0)
                            .setIsVideoCall(in.readByte() != 0)
                            .setIsFullscreen(in.readByte() != 0)
                            .build();
                }

                @Override
                public SecondaryInfo[] newArray(int size) {
                    return new SecondaryInfo[size];
                }
            };

    public static Builder builder() {
        return new AutoValue_SecondaryInfo.Builder()
                .setShouldShow(false)
                .setNameIsNumber(false)
                .setIsConference(false)
                .setIsVideoCall(false)
                .setIsFullscreen(false);
    }

    public abstract boolean shouldShow();

    @Nullable
    public abstract String name();

    public abstract boolean nameIsNumber();

    @Nullable
    public abstract String label();

    @Nullable
    public abstract String providerLabel();

    public abstract boolean isConference();

    public abstract boolean isVideoCall();

    public abstract boolean isFullscreen();

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "SecondaryInfo, show: %b, name: %s, label: %s, " + "providerLabel: %s",
                shouldShow(),
                LogUtil.sanitizePii(name()),
                label(),
                providerLabel());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (shouldShow() ? 1 : 0));
        dest.writeString(name());
        dest.writeByte((byte) (nameIsNumber() ? 1 : 0));
        dest.writeString(label());
        dest.writeString(providerLabel());
        dest.writeByte((byte) (isConference() ? 1 : 0));
        dest.writeByte((byte) (isVideoCall() ? 1 : 0));
        dest.writeByte((byte) (isFullscreen() ? 1 : 0));
    }

    /**
     * Builder class for secondary info.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setShouldShow(boolean shouldShow);

        public abstract Builder setName(String name);

        public abstract Builder setNameIsNumber(boolean nameIsNumber);

        public abstract Builder setLabel(String label);

        public abstract Builder setProviderLabel(String providerLabel);

        public abstract Builder setIsConference(boolean isConference);

        public abstract Builder setIsVideoCall(boolean isVideoCall);

        public abstract Builder setIsFullscreen(boolean isFullscreen);

        public abstract SecondaryInfo build();
    }
}
