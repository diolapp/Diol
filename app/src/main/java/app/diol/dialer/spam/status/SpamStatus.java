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

package app.diol.dialer.spam.status;

import com.google.common.base.Optional;

/**
 * An interface representing a number's spam status.
 */
@SuppressWarnings("Guava")
public interface SpamStatus {

    /**
     * Returns true if the number is spam.
     */
    boolean isSpam();

    /**
     * Returns the timestamp (in milliseconds) indicating when the number's spam status entered the
     * underlying data source.
     *
     * <p>{@code Optional.absent()} is returned if
     *
     * <ul>
     * <li>the number's spam status doesn't exist in the underlying data source, or
     * <li>the underlying data source can't provide a timestamp.
     * </ul>
     */
    Optional<Long> getTimestampMillis();

    /**
     * Returns the {@link SpamMetadata} associated with this status.
     */
    SpamMetadata getSpamMetadata();
}
