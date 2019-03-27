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

package app.diol.dialer.glidephotomanager.impl;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.QuickContactBadge;

import java.util.List;

import javax.inject.Inject;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.glide.GlideApp;
import app.diol.dialer.glide.GlideRequest;
import app.diol.dialer.glide.GlideRequests;
import app.diol.dialer.glidephotomanager.GlidePhotoManager;
import app.diol.dialer.glidephotomanager.PhotoInfo;
import app.diol.dialer.i18n.DialerBidiFormatter;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.lettertile.LetterTileDrawable;

/**
 * Implementation of {@link GlidePhotoManager}
 */
public class GlidePhotoManagerImpl implements GlidePhotoManager {

    private static final int LOOKUP_URI_PATH_SEGMENTS =
            Contacts.CONTENT_LOOKUP_URI.getPathSegments().size();

    private final Context appContext;

    @Inject
    public GlidePhotoManagerImpl(@ApplicationContext Context appContext) {
        this.appContext = appContext;
    }

    @Nullable
    private static Uri parseUri(@Nullable String uri) {
        return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
    }

    /**
     * Return the "lookup key" inside the lookup URI. If the URI does not contain the key (i.e, JSON
     * based prepopulated URIs for non-contact entries), the URI itself is returned.
     *
     * <p>The lookup URI has the format of Contacts.CONTENT_LOOKUP_URI/lookupKey/rowId. For JSON based
     * URI, it would be Contacts.CONTENT_LOOKUP_URI/encoded#JSON
     */
    private static String getIdentifier(String lookupUri) {
        if (!lookupUri.startsWith(Contacts.CONTENT_LOOKUP_URI.toString())) {
            return lookupUri;
        }

        List<String> segments = Uri.parse(lookupUri).getPathSegments();
        if (segments.size() < LOOKUP_URI_PATH_SEGMENTS) {
            return lookupUri;
        }
        String lookupKey = segments.get(LOOKUP_URI_PATH_SEGMENTS);
        if ("encoded".equals(lookupKey)) {
            return lookupUri;
        }
        return lookupKey;
    }

    @MainThread
    @Override
    public void loadQuickContactBadge(QuickContactBadge badge, PhotoInfo photoInfo) {
        Assert.isMainThread();
        badge.assignContactUri(
                TextUtils.isEmpty(photoInfo.getLookupUri())
                        ? DefaultLookupUriGenerator.generateUri(photoInfo)
                        : parseUri(photoInfo.getLookupUri()));
        badge.setOverlay(null);
        loadContactPhoto(badge, photoInfo);
    }

    @MainThread
    @Override
    public void loadContactPhoto(ImageView imageView, PhotoInfo photoInfo) {
        Assert.isMainThread();
        imageView.setContentDescription(
                TextUtils.expandTemplate(
                        appContext.getText(R.string.a11y_glide_photo_manager_contact_photo_description),
                        // The display name in "photoInfo" can be a contact name, a number, or a mixture of text
                        // and a phone number. We use DialerBidiFormatter to wrap the phone number with TTS
                        // span.
                        DialerBidiFormatter.format(photoInfo.getName())));
        GlideRequest<Drawable> request = buildRequest(GlideApp.with(imageView), photoInfo);
        request.into(imageView);
    }

    private GlideRequest<Drawable> buildRequest(GlideRequests requestManager, PhotoInfo photoInfo) {
        // Warning: Glide ignores extra attributes on BitmapDrawable such as tint and draw the bitmap
        // directly so be sure not to set tint in the XML of any drawable referenced below.

        GlideRequest<Drawable> request;
        boolean circleCrop = true; // Photos are cropped to a circle by default.

        if (photoInfo.getIsBlocked()) {
            // Whether the number is blocked takes precedence over the spam status.
            request = requestManager.load(R.drawable.ic_block_grey_48dp);

        } else if (photoInfo.getIsSpam()) {
            request = requestManager.load(R.drawable.quantum_ic_report_vd_red_24);
            circleCrop = false; // The spam icon is an octagon so we don't crop it.

        } else if (!TextUtils.isEmpty(photoInfo.getPhotoUri())) {
            request = requestManager.load(parseUri(photoInfo.getPhotoUri()));

        } else if (photoInfo.getPhotoId() != 0) {
            request =
                    requestManager.load(ContentUris.withAppendedId(Data.CONTENT_URI, photoInfo.getPhotoId()));

        } else {
            // load null to indicate fallback should be used.
            request = requestManager.load((Object) null);
        }

        LetterTileDrawable defaultDrawable = getDefaultDrawable(photoInfo);
        request
                .placeholder(defaultDrawable) // when the photo is still loading.
                .fallback(defaultDrawable); // when there's nothing to load.

        if (circleCrop) {
            request.circleCrop();
        }

        return request;
    }

    /**
     * Generate the default drawable when photos are not available. Used when the photo is loading or
     * no photo is available.
     */
    private LetterTileDrawable getDefaultDrawable(PhotoInfo photoInfo) {
        LetterTileDrawable letterTileDrawable = new LetterTileDrawable(appContext.getResources());
        String displayName;
        String identifier;
        if (TextUtils.isEmpty(photoInfo.getLookupUri())) {
            // Use generic avatar instead of letter for non-contacts.
            displayName = null;
            identifier =
                    TextUtils.isEmpty(photoInfo.getName())
                            ? photoInfo.getFormattedNumber()
                            : photoInfo.getName();
        } else {
            displayName = photoInfo.getName();
            identifier = getIdentifier(photoInfo.getLookupUri());
        }
        letterTileDrawable.setCanonicalDialerLetterTileDetails(
                displayName,
                identifier,
                LetterTileDrawable.SHAPE_CIRCLE,
                LetterTileDrawable.getContactTypeFromPrimitives(
                        photoInfo.getIsVoicemail(),
                        photoInfo.getIsSpam(),
                        photoInfo.getIsBusiness(),
                        TelecomManager.PRESENTATION_ALLOWED, // TODO(twyen):implement
                        photoInfo.getIsConference()));
        return letterTileDrawable;
    }
}
