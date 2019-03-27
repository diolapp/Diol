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

package app.diol.bubble;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;

import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.List;

/**
 * Info for displaying a {@link Bubble}
 */
@AutoValue
public abstract class BubbleInfo {
    public static Builder builder() {
        return new AutoValue_BubbleInfo.Builder().setActions(Collections.emptyList());
    }

    public static Builder from(@NonNull BubbleInfo bubbleInfo) {
        return builder()
                .setPrimaryColor(bubbleInfo.getPrimaryColor())
                .setPrimaryIcon(bubbleInfo.getPrimaryIcon())
                .setStartingYPosition(bubbleInfo.getStartingYPosition())
                .setActions(bubbleInfo.getActions())
                .setAvatar(bubbleInfo.getAvatar());
    }

    @ColorInt
    public abstract int getPrimaryColor();

    public abstract Icon getPrimaryIcon();

    @Nullable
    public abstract Drawable getAvatar();

    @Px
    public abstract int getStartingYPosition();

    @NonNull
    public abstract List<Action> getActions();

    /**
     * Builder for {@link BubbleInfo}
     */
    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setPrimaryColor(@ColorInt int primaryColor);

        public abstract Builder setPrimaryIcon(@NonNull Icon primaryIcon);

        public abstract Builder setAvatar(@Nullable Drawable avatar);

        public abstract Builder setStartingYPosition(@Px int startingYPosition);

        public abstract Builder setActions(List<Action> actions);

        public abstract BubbleInfo build();
    }

    /**
     * Represents actions to be shown in the bubble when expanded
     */
    @AutoValue
    public abstract static class Action {

        public static Builder builder() {
            return new AutoValue_BubbleInfo_Action.Builder().setCheckable(true).setChecked(false);
        }

        public static Builder from(@NonNull Action action) {
            return builder()
                    .setIntent(action.getIntent())
                    .setChecked(action.isChecked())
                    .setCheckable(action.isCheckable())
                    .setName(action.getName())
                    .setIconDrawable(action.getIconDrawable())
                    .setSecondaryIconDrawable(action.getSecondaryIconDrawable());
        }

        public abstract Drawable getIconDrawable();

        @Nullable
        public abstract Drawable getSecondaryIconDrawable();

        @NonNull
        public abstract CharSequence getName();

        @NonNull
        public abstract PendingIntent getIntent();

        public abstract boolean isCheckable();

        public abstract boolean isChecked();

        /**
         * Builder for {@link Action}
         */
        @AutoValue.Builder
        public abstract static class Builder {

            public abstract Builder setIconDrawable(Drawable iconDrawable);

            public abstract Builder setSecondaryIconDrawable(@Nullable Drawable secondaryIconDrawable);

            public abstract Builder setName(@NonNull CharSequence name);

            public abstract Builder setIntent(@NonNull PendingIntent intent);

            public abstract Builder setCheckable(boolean enabled);

            public abstract Builder setChecked(boolean checked);

            public abstract Action build();
        }
    }
}
