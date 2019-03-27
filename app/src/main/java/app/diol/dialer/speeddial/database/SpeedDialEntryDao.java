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

package app.diol.dialer.speeddial.database;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Interface that databases support speed dial entries should implement.
 *
 * <p>This database is only used for favorite/starred contacts.
 */
public interface SpeedDialEntryDao {

    /**
     * Return all entries in the database
     */
    ImmutableList<SpeedDialEntry> getAllEntries();

    /**
     * Insert new entries.
     *
     * <p>{@link SpeedDialEntry#id() ids} must be null.
     *
     * @return a map of the inserted entries to their new ids.
     */
    ImmutableMap<SpeedDialEntry, Long> insert(ImmutableList<SpeedDialEntry> entries);

    /**
     * Insert a new entry.
     *
     * <p>{@link SpeedDialEntry#id() ids} must be null.
     */
    long insert(SpeedDialEntry entry);

    /**
     * Updates existing entries based on {@link SpeedDialEntry#id}.
     *
     * <p>Fails if the {@link SpeedDialEntry#id()} doesn't exist.
     */
    void update(ImmutableList<SpeedDialEntry> entries);

    /**
     * Delete the passed in entries based on {@link SpeedDialEntry#id}.
     *
     * <p>Fails if the {@link SpeedDialEntry#id()} doesn't exist.
     */
    void delete(ImmutableList<Long> entries);

    /**
     * Inserts, updates and deletes rows all in on transaction.
     *
     * @return a map of the inserted entries to their new ids.
     * @see #insert(ImmutableList)
     * @see #update(ImmutableList)
     * @see #delete(ImmutableList)
     */
    ImmutableMap<SpeedDialEntry, Long> insertUpdateAndDelete(
            ImmutableList<SpeedDialEntry> entriesToInsert,
            ImmutableList<SpeedDialEntry> entriesToUpdate,
            ImmutableList<Long> entriesToDelete);

    /**
     * Delete all entries in the database.
     */
    void deleteAll();
}
