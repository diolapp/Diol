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

package app.diol.dialer.shortcuts;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.WorkerThread;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.InputStream;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.util.DrawableConverter;

/**
 * Constructs the icons for dialer shortcuts.
 */
class IconFactory {

    private final Context context;

    IconFactory(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Creates an icon for the provided {@link DialerShortcut}.
     *
     * <p>The icon is a circle which contains a photo of the contact associated with the shortcut, if
     * available. If a photo is not available, a circular colored icon with a single letter is instead
     * created, where the letter is the first letter of the contact's name. If the contact has no
     * name, a default colored "anonymous" avatar is used.
     *
     * <p>These icons should match exactly the favorites tiles in the starred tab of the dialer
     * application, except that they are circular instead of rectangular.
     */
    @WorkerThread
    @NonNull
    public Icon create(@NonNull DialerShortcut shortcut) {
        Assert.isWorkerThread();

        return create(shortcut.getLookupUri(), shortcut.getDisplayName(), shortcut.getLookupKey());
    }

    /**
     * Same as {@link #create(DialerShortcut)}, but accepts a {@link ShortcutInfo}.
     */
    @WorkerThread
    @NonNull
    public Icon create(@NonNull ShortcutInfo shortcutInfo) {
        Assert.isWorkerThread();
        return create(
                DialerShortcut.getLookupUriFromShortcutInfo(shortcutInfo),
                DialerShortcut.getDisplayNameFromShortcutInfo(shortcutInfo),
                DialerShortcut.getLookupKeyFromShortcutInfo(shortcutInfo));
    }

    @WorkerThread
    @NonNull
    private Icon create(
            @NonNull Uri lookupUri, @NonNull String displayName, @NonNull String lookupKey) {
        Assert.isWorkerThread();

        // In testing, there was no difference between high-res and thumbnail.
        InputStream inputStream =
                ContactsContract.Contacts.openContactPhotoInputStream(
                        context.getContentResolver(), lookupUri, false /* preferHighres */);

        return VERSION.SDK_INT >= VERSION_CODES.O
                ? createAdaptiveIcon(displayName, lookupKey, inputStream)
                : createFlatIcon(displayName, lookupKey, inputStream);
    }

    @RequiresApi(VERSION_CODES.O)
    private Icon createAdaptiveIcon(
            @NonNull String displayName, @NonNull String lookupKey, @Nullable InputStream inputStream) {
        if (inputStream == null) {
            LetterTileDrawable letterTileDrawable = new LetterTileDrawable(context.getResources());
            // The adaptive icons clip the drawable to a safe area inside the drawable. Scale the letter
            // so it fits inside the safe area.
            letterTileDrawable.setScale(1f / (1f + AdaptiveIconDrawable.getExtraInsetFraction()));
            letterTileDrawable.setCanonicalDialerLetterTileDetails(
                    displayName,
                    lookupKey,
                    LetterTileDrawable.SHAPE_RECTANGLE,
                    LetterTileDrawable.TYPE_DEFAULT);

            int iconSize =
                    context
                            .getResources()
                            .getDimensionPixelSize(R.dimen.launcher_shortcut_adaptive_icon_size);
            return Icon.createWithAdaptiveBitmap(
                    DrawableConverter.drawableToBitmap(letterTileDrawable, iconSize, iconSize));
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return Icon.createWithAdaptiveBitmap(bitmap);
    }

    private Icon createFlatIcon(
            @NonNull String displayName, @NonNull String lookupKey, @Nullable InputStream inputStream) {
        Drawable drawable;
        if (inputStream == null) {
            // No photo for contact; use a letter tile.
            LetterTileDrawable letterTileDrawable = new LetterTileDrawable(context.getResources());
            letterTileDrawable.setCanonicalDialerLetterTileDetails(
                    displayName, lookupKey, LetterTileDrawable.SHAPE_CIRCLE, LetterTileDrawable.TYPE_DEFAULT);
            drawable = letterTileDrawable;
        } else {
            // There's a photo, create a circular drawable from it.
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            drawable = createCircularDrawable(bitmap);
        }
        int iconSize =
                context.getResources().getDimensionPixelSize(R.dimen.launcher_shortcut_icon_size);
        return Icon.createWithBitmap(
                DrawableConverter.drawableToBitmap(drawable, iconSize /* width */, iconSize /* height */));
    }

    @NonNull
    private Drawable createCircularDrawable(@NonNull Bitmap bitmap) {
        RoundedBitmapDrawable roundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        roundedBitmapDrawable.setAntiAlias(true);
        return roundedBitmapDrawable;
    }
}
