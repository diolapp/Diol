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

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.concurrent.TimeUnit;

import app.diol.R;

/**
 * Shows an item in the gallery picker grid view. Hosts an FileImageView with a checkbox.
 */
public class GalleryGridItemView extends FrameLayout {

    private final GalleryGridItemData data = new GalleryGridItemData();

    private ImageView image;
    private View checkbox;
    private View gallery;
    private String currentFilePath;
    private boolean isGallery;

    public GalleryGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        image = (ImageView) findViewById(R.id.image);
        checkbox = findViewById(R.id.checkbox);
        gallery = findViewById(R.id.gallery);

        image.setClipToOutline(true);
        checkbox.setClipToOutline(true);
        gallery.setClipToOutline(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // The grid view auto-fit the columns, so we want to let the height match the width
        // to make the image square.
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public GalleryGridItemData getData() {
        return data;
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            checkbox.setVisibility(VISIBLE);
            int paddingPx = getResources().getDimensionPixelSize(R.dimen.gallery_item_selected_padding);
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        } else {
            checkbox.setVisibility(GONE);
            int paddingPx = getResources().getDimensionPixelOffset(R.dimen.gallery_item_padding);
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        }
    }

    public boolean isGallery() {
        return isGallery;
    }

    public void showGallery(boolean show) {
        isGallery = show;
        gallery.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void bind(Cursor cursor) {
        data.bind(cursor);
        showGallery(false);
        updateImageView();
    }

    private void updateImageView() {
        image.setScaleType(ScaleType.CENTER_CROP);

        if (currentFilePath == null || !currentFilePath.equals(data.getFilePath())) {
            currentFilePath = data.getFilePath();

            // Downloads/loads an image from the given URI so that the image's largest dimension is
            // between 1/2 the given dimensions and the given dimensions, with no restrictions on the
            // image's smallest dimension. We skip the memory cache, but glide still applies it's disk
            // cache to optimize loads.
            Glide.with(getContext())
                    .load(data.getFileUri())
                    .apply(RequestOptions.downsampleOf(DownsampleStrategy.AT_MOST).skipMemoryCache(true))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(image);
        }
        long dateModifiedSeconds = data.getDateModifiedSeconds();
        if (dateModifiedSeconds > 0) {
            image.setContentDescription(
                    getResources()
                            .getString(
                                    R.string.gallery_item_description,
                                    TimeUnit.SECONDS.toMillis(dateModifiedSeconds)));
        } else {
            image.setContentDescription(
                    getResources().getString(R.string.gallery_item_description_no_date));
        }
    }
}
