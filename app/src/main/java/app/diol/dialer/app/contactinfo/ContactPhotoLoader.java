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

package app.diol.dialer.app.contactinfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.lettertile.LetterTileDrawable;
import app.diol.dialer.location.GeoUtil;
import app.diol.dialer.phonenumbercache.ContactInfo;
import app.diol.dialer.phonenumbercache.ContactInfoHelper;

/**
 * Class to create the appropriate contact icon from a ContactInfo. This class is for synchronous,
 * blocking calls to generate bitmaps, while ContactCommons.ContactPhotoManager is to cache, manage
 * and update a ImageView asynchronously.
 */
public class ContactPhotoLoader {

    private final Context context;
    private final ContactInfo contactInfo;

    public ContactPhotoLoader(Context context, ContactInfo contactInfo) {
        this.context = Objects.requireNonNull(context);
        this.contactInfo = Objects.requireNonNull(contactInfo);
    }

    private static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Create a contact photo icon bitmap appropriate for the ContactInfo.
     */
    public Bitmap loadPhotoIcon() {
        Assert.isWorkerThread();
        int photoSize = context.getResources().getDimensionPixelSize(R.dimen.contact_photo_size);
        return drawableToBitmap(getIcon(), photoSize, photoSize);
    }

    @VisibleForTesting
    Drawable getIcon() {
        Drawable drawable = createPhotoIconDrawable();
        if (drawable == null) {
            drawable = createLetterTileDrawable();
        }
        return drawable;
    }

    /**
     * @return a {@link Drawable} of circular photo icon if the photo can be loaded, {@code null}
     * otherwise.
     */
    @Nullable
    private Drawable createPhotoIconDrawable() {
        if (contactInfo.photoUri == null) {
            return null;
        }
        try {
            InputStream input = context.getContentResolver().openInputStream(contactInfo.photoUri);
            if (input == null) {
                LogUtil.w(
                        "ContactPhotoLoader.createPhotoIconDrawable",
                        "createPhotoIconDrawable: InputStream is null");
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();

            if (bitmap == null) {
                LogUtil.w(
                        "ContactPhotoLoader.createPhotoIconDrawable",
                        "createPhotoIconDrawable: Bitmap is null");
                return null;
            }
            final RoundedBitmapDrawable drawable =
                    RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            drawable.setAntiAlias(true);
            drawable.setCircular(true);
            return drawable;
        } catch (IOException e) {
            LogUtil.e("ContactPhotoLoader.createPhotoIconDrawable", e.toString());
            return null;
        }
    }

    /**
     * @return a {@link LetterTileDrawable} based on the ContactInfo.
     */
    private Drawable createLetterTileDrawable() {
        ContactInfoHelper helper =
                new ContactInfoHelper(context, GeoUtil.getCurrentCountryIso(context));
        LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
        drawable.setCanonicalDialerLetterTileDetails(
                contactInfo.name,
                contactInfo.lookupKey,
                LetterTileDrawable.SHAPE_CIRCLE,
                helper.isBusiness(contactInfo.sourceType)
                        ? LetterTileDrawable.TYPE_BUSINESS
                        : LetterTileDrawable.TYPE_DEFAULT);
        return drawable;
    }
}
