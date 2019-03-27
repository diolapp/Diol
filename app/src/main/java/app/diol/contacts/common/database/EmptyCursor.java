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

package app.diol.contacts.common.database;

import android.database.AbstractCursor;
import android.database.CursorIndexOutOfBoundsException;

/**
 * A cursor that is empty.
 *
 * <p>If you want an empty cursor, this class is better than a MatrixCursor because it has less
 * overhead.
 */
public final class EmptyCursor extends AbstractCursor {

    private String[] mColumns;

    public EmptyCursor(String[] columns) {
        this.mColumns = columns;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public String[] getColumnNames() {
        return mColumns;
    }

    @Override
    public String getString(int column) {
        throw cursorException();
    }

    @Override
    public short getShort(int column) {
        throw cursorException();
    }

    @Override
    public int getInt(int column) {
        throw cursorException();
    }

    @Override
    public long getLong(int column) {
        throw cursorException();
    }

    @Override
    public float getFloat(int column) {
        throw cursorException();
    }

    @Override
    public double getDouble(int column) {
        throw cursorException();
    }

    @Override
    public boolean isNull(int column) {
        throw cursorException();
    }

    private CursorIndexOutOfBoundsException cursorException() {
        return new CursorIndexOutOfBoundsException("Operation not permitted on an empty cursor.");
    }
}
