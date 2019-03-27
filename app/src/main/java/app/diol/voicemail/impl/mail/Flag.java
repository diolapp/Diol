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
package app.diol.voicemail.impl.mail;

/**
 * Flags that can be applied to Messages.
 */
public class Flag {
    // If adding new flags: ALL FLAGS MUST BE UPPER CASE.
    public static final String DELETED = "deleted";
    public static final String SEEN = "seen";
    public static final String ANSWERED = "answered";
    public static final String FLAGGED = "flagged";
    public static final String DRAFT = "draft";
    public static final String RECENT = "recent";
}
