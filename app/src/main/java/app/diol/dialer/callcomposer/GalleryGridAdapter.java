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
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.ArrayList;
import java.util.List;

import app.diol.R;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;

/**
 * Bridges between the image cursor loaded by GalleryBoundCursorLoader and the GalleryGridView.
 */
public class GalleryGridAdapter extends CursorAdapter {

    @NonNull
    private final OnClickListener onClickListener;
    @NonNull
    private final List<GalleryGridItemView> views = new ArrayList<>();
    @NonNull
    private final Context context;

    private GalleryGridItemData selectedData;

    public GalleryGridAdapter(
            @NonNull Context context, Cursor cursor, @NonNull OnClickListener onClickListener) {
        super(context, cursor, 0);
        this.onClickListener = Assert.isNotNull(onClickListener);
        this.context = Assert.isNotNull(context);
    }

    @Override
    public int getCount() {
        // Add one for the header.
        return super.getCount() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // At position 0, we want to insert a header. If position == 0, we don't need the cursor.
        // If position != 0, then we need to move the cursor to position - 1 to account for the offset
        // of the header.
        if (position != 0 && !getCursor().moveToPosition(position - 1)) {
            Assert.fail("couldn't move cursor to position " + (position - 1));
        }
        View view;
        if (convertView == null) {
            view = newView(context, getCursor(), parent);
        } else {
            view = convertView;
        }
        bindView(view, context, getCursor(), position);
        return view;
    }

    private void bindView(View view, Context context, Cursor cursor, int position) {
        if (position == 0) {
            GalleryGridItemView gridView = (GalleryGridItemView) view;
            gridView.showGallery(true);
        } else {
            bindView(view, context, cursor);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        GalleryGridItemView gridView = (GalleryGridItemView) view;
        gridView.bind(cursor);
        gridView.setSelected(gridView.getData().equals(selectedData));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        GalleryGridItemView view =
                (GalleryGridItemView)
                        LayoutInflater.from(context).inflate(R.layout.gallery_grid_item_view, parent, false);
        view.setOnClickListener(onClickListener);
        views.add(view);
        return view;
    }

    public void setSelected(GalleryGridItemData selectedData) {
        this.selectedData = selectedData;
        for (GalleryGridItemView view : views) {
            view.setSelected(view.getData().equals(selectedData));
        }
    }

    public void insertEntries(@NonNull List<GalleryGridItemData> entries) {
        Assert.checkArgument(entries.size() != 0);
        LogUtil.i("GalleryGridAdapter.insertRows", "inserting %d rows", entries.size());
        MatrixCursor extraRow = new MatrixCursor(GalleryGridItemData.IMAGE_PROJECTION);
        for (GalleryGridItemData entry : entries) {
            extraRow.addRow(new Object[]{0L, entry.getFilePath(), entry.getMimeType(), ""});
        }
        extraRow.moveToFirst();
        Cursor extendedCursor = new MergeCursor(new Cursor[]{extraRow, getCursor()});
        swapCursor(extendedCursor);
    }

    public GalleryGridItemData insertEntry(String filePath, String mimeType) {
        LogUtil.i("GalleryGridAdapter.insertRow", mimeType + " " + filePath);

        MatrixCursor extraRow = new MatrixCursor(GalleryGridItemData.IMAGE_PROJECTION);
        extraRow.addRow(new Object[]{0L, filePath, mimeType, ""});
        extraRow.moveToFirst();
        Cursor extendedCursor = new MergeCursor(new Cursor[]{extraRow, getCursor()});
        swapCursor(extendedCursor);

        return new GalleryGridItemData(extraRow);
    }
}
