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

package app.diol.contacts.common.list;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import app.diol.R;
import app.diol.contacts.common.MoreContactUtils;
import app.diol.dialer.callintent.CallInitiationType;
import app.diol.dialer.callintent.CallSpecificAppData;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.contactphoto.ContactPhotoManager;
import app.diol.dialer.contactphoto.ContactPhotoManager.DefaultImageRequest;
import app.diol.dialer.widget.BidiTextView;

/**
 * A ContactTile displays a contact's picture and name
 */
public abstract class ContactTileView extends FrameLayout {

    private static final String TAG = ContactTileView.class.getSimpleName();
    protected Listener mListener;
    private Uri mLookupUri;
    private ImageView mPhoto;
    private BidiTextView mName;
    private ContactPhotoManager mPhotoManager = null;

    public ContactTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mName = (BidiTextView) findViewById(R.id.contact_tile_name);
        mPhoto = (ImageView) findViewById(R.id.contact_tile_image);

        OnClickListener listener = createClickListener();
        setOnClickListener(listener);
    }

    protected OnClickListener createClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    return;
                }
                CallSpecificAppData callSpecificAppData =
                        CallSpecificAppData.newBuilder()
                                .setCallInitiationType(CallInitiationType.Type.SPEED_DIAL)
                                .setAllowAssistedDialing(true)
                                .build();
                mListener.onContactSelected(
                        getLookupUri(),
                        MoreContactUtils.getTargetRectFromView(ContactTileView.this),
                        callSpecificAppData);
            }
        };
    }

    public void setPhotoManager(ContactPhotoManager photoManager) {
        mPhotoManager = photoManager;
    }

    /**
     * Populates the data members to be displayed from the fields in {@link
     * com.android.contacts.common.list.ContactEntry}
     */
    public void loadFromContact(ContactEntry entry) {

        if (entry != null) {
            mName.setText(getNameForView(entry));
            mLookupUri = entry.lookupUri;

            setVisibility(View.VISIBLE);

            if (mPhotoManager != null) {
                DefaultImageRequest request = getDefaultImageRequest(entry.namePrimary, entry.lookupKey);
                configureViewForImage(entry.photoUri == null);
                if (mPhoto != null) {
                    mPhotoManager.loadPhoto(
                            mPhoto,
                            entry.photoUri,
                            getApproximateImageSize(),
                            isDarkTheme(),
                            isContactPhotoCircular(),
                            request);


                }
            } else {
                LogUtil.w(TAG, "contactPhotoManager not set");
            }
        } else {
            setVisibility(View.INVISIBLE);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public Uri getLookupUri() {
        return mLookupUri;
    }

    /**
     * Returns the string that should actually be displayed as the contact's name. Subclasses can
     * override this to return formatted versions of the name - i.e. first name only.
     */
    protected String getNameForView(ContactEntry contactEntry) {
        return contactEntry.namePrimary;
    }

    /**
     * Implemented by subclasses to estimate the size of the picture. This can return -1 if only a
     * thumbnail is shown anyway
     */
    protected abstract int getApproximateImageSize();

    protected abstract boolean isDarkTheme();

    /**
     * Implemented by subclasses to reconfigure the view's layout and subviews, based on whether or
     * not the contact has a user-defined photo.
     *
     * @param isDefaultImage True if the contact does not have a user-defined contact photo (which
     *                       means a default contact image will be applied by the {@link ContactPhotoManager}
     */
    protected void configureViewForImage(boolean isDefaultImage) {
        // No-op by default.
    }

    /**
     * Implemented by subclasses to allow them to return a {@link DefaultImageRequest} with the
     * various image parameters defined to match their own layouts.
     *
     * @param displayName The display name of the contact
     * @param lookupKey   The lookup key of the contact
     * @return A {@link DefaultImageRequest} object with each field configured by the subclass as
     * desired, or {@code null}.
     */
    protected DefaultImageRequest getDefaultImageRequest(String displayName, String lookupKey) {
        return new DefaultImageRequest(displayName, lookupKey, isContactPhotoCircular());
    }

    /**
     * Whether contact photo should be displayed as a circular image. Implemented by subclasses so
     * they can change which drawables to fetch.
     */
    protected boolean isContactPhotoCircular() {
        return true;
    }

    public interface Listener {

        /**
         * Notification that the contact was selected; no specific action is dictated.
         */
        void onContactSelected(
                Uri contactLookupUri, Rect viewRect, CallSpecificAppData callSpecificAppData);

        /**
         * Notification that the specified number is to be called.
         */
        void onCallNumberDirectly(String phoneNumber, CallSpecificAppData callSpecificAppData);
    }
}
