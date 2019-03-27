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

package app.diol.dialer.speeddial.draghelper;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;

import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;

/**
 * {@link ItemTouchHelper} for Speed Dial favorite contacts.
 */
public class SpeedDialItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter adapter;
    private final Context context;

    // When dragged item is in removeView, onMove() and onChildDraw() are called in turn. This
    // behavior changes when dragged item entering/leaving removeView. The boolean field
    // movedOverRemoveView is for onMove() and onChildDraw() to flip.
    private boolean movedOverRemoveView;
    private boolean inRemoveView;

    public SpeedDialItemTouchHelperCallback(Context context, ItemTouchHelperAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // We'll manually call ItemTouchHelper#startDrag
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // We don't want to enable swiping
        return false;
    }

    @Override
    public boolean canDropOver(
            @NonNull RecyclerView recyclerView, @NonNull ViewHolder current, @NonNull ViewHolder target) {
        return adapter.canDropOver(target);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
        if (!adapter.canDropOver(viewHolder)) {
            return makeMovementFlags(0, 0);
        }

        int dragFlags =
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, /* swipeFlags */ 0);
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull ViewHolder viewHolder,
            @NonNull ViewHolder target) {
        if (target.getItemViewType() == 0) { // 0 for RowType.REMOVE_VIEW
            movedOverRemoveView = true;
            if (!inRemoveView) {
                // onMove() first called
                adapter.enterRemoveView();
                inRemoveView = true;
            }
            return false;
        } else if (inRemoveView) {
            // Move out of removeView fast
            inRemoveView = false;
            movedOverRemoveView = false;
            adapter.leaveRemoveView();
        }
        adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onMoved(
            @NonNull RecyclerView recyclerView,
            @NonNull ViewHolder viewHolder,
            int fromPos,
            @NonNull ViewHolder viewHolder1,
            int toPos,
            int x,
            int y) {
        Logger.get(context)
                .logImpression(DialerImpression.Type.FAVORITE_MOVE_FAVORITE_BY_DRAG_AND_DROP);
        super.onMoved(recyclerView, viewHolder, fromPos, viewHolder1, toPos, x, y);
    }

    @Override
    public void onChildDraw(
            @NonNull Canvas canvas,
            @NonNull RecyclerView recyclerView,
            @NonNull ViewHolder viewHolder,
            float dx,
            float dy,
            int i,
            boolean isCurrentlyActive) {
        if (inRemoveView) {
            if (!isCurrentlyActive) {
                // View animating back to its original state, which means drop in this case
                inRemoveView = false;
                adapter.dropOnRemoveView(viewHolder);
            }
            if (!movedOverRemoveView) {
                // when the view is over a droppable target, onMove() will be called before onChildDraw()
                // thus if onMove() is not called, it is not over a droppable target.
                inRemoveView = false;
                adapter.leaveRemoveView();
            }
        }
        movedOverRemoveView = false;
        super.onChildDraw(canvas, recyclerView, viewHolder, dx, dy, i, isCurrentlyActive);
    }

    @Override
    public void onSelectedChanged(@Nullable ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        adapter.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onSwiped(@NonNull ViewHolder viewHolder, int direction) {
        // No-op since we don't support swiping
    }

    /**
     * RecyclerView adapters interested in drag and drop should implement this interface.
     */
    public interface ItemTouchHelperAdapter {

        void onItemMove(int fromPosition, int toPosition);

        boolean canDropOver(ViewHolder target);

        void onSelectedChanged(@Nullable ViewHolder viewHolder, int actionState);

        void enterRemoveView();

        void leaveRemoveView();

        void dropOnRemoveView(ViewHolder fromViewHolder);
    }
}
