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

package app.diol.dialer.calllog.datasources;

import android.content.ContentValues;
import android.util.ArrayMap;
import android.util.ArraySet;

import app.diol.dialer.common.Assert;

/**
 * A collection of mutations to the annotated call log.
 */
public final class CallLogMutations {

    private final ArrayMap<Long, ContentValues> inserts = new ArrayMap<>();
    private final ArrayMap<Long, ContentValues> updates = new ArrayMap<>();
    private final ArraySet<Long> deletes = new ArraySet<>();

    /**
     * @param contentValues an entire row not including the ID
     * @throws IllegalStateException if this {@link CallLogMutations} already contains an insert,
     *                               update, or delete with the provided id
     */
    public void insert(long id, ContentValues contentValues) {
        Assert.checkArgument(!inserts.containsKey(id), "Can't insert row already scheduled for insert");
        Assert.checkArgument(!updates.containsKey(id), "Can't insert row scheduled for update");
        Assert.checkArgument(!deletes.contains(id), "Can't insert row scheduled for delete");

        inserts.put(id, contentValues);
    }

    /**
     * Stores a database update using the provided ID and content values. If this {@link
     * CallLogMutations} object already contains an update with the specified ID, the existing content
     * values are merged with the provided ones, with the provided ones overwriting the existing ones
     * for values with the same key.
     *
     * @param contentValues the specific columns to update, not including the ID.
     * @throws IllegalStateException if this {@link CallLogMutations} already contains an insert or
     *                               delete with the provided id
     */
    public void update(long id, ContentValues contentValues) {
        Assert.checkArgument(!inserts.containsKey(id), "Can't update row scheduled for insert");
        Assert.checkArgument(!deletes.contains(id), "Can't delete row scheduled for delete");

        ContentValues existingContentValues = updates.get(id);
        if (existingContentValues != null) {
            existingContentValues.putAll(contentValues);
        } else {
            updates.put(id, contentValues);
        }
    }

    /**
     * @throws IllegalStateException if this {@link CallLogMutations} already contains an insert,
     *                               update, or delete with the provided id
     */
    public void delete(long id) {
        Assert.checkArgument(!inserts.containsKey(id), "Can't delete row scheduled for insert");
        Assert.checkArgument(!updates.containsKey(id), "Can't delete row scheduled for update");
        Assert.checkArgument(!deletes.contains(id), "Can't delete row already scheduled for delete");

        deletes.add(id);
    }

    public boolean isEmpty() {
        return inserts.isEmpty() && updates.isEmpty() && deletes.isEmpty();
    }

    /**
     * Get the pending inserts.
     *
     * @return the pending inserts where the key is the annotated call log database ID and the values
     * are values to be inserted (not including the ID)
     */
    public ArrayMap<Long, ContentValues> getInserts() {
        return inserts;
    }

    /**
     * Get the pending updates.
     *
     * @return the pending updates where the key is the annotated call log database ID and the values
     * are values to be updated (not including the ID)
     */
    public ArrayMap<Long, ContentValues> getUpdates() {
        return updates;
    }

    /**
     * Get the pending deletes.
     *
     * @return the annotated call log database IDs corresponding to the rows to be deleted
     */
    public ArraySet<Long> getDeletes() {
        return deletes;
    }
}
