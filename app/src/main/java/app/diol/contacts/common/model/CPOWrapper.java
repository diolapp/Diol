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

package app.diol.contacts.common.model;

import android.content.ContentProviderOperation;

/**
 * This class is created for the purpose of compatibility and make the type of
 * ContentProviderOperation available on pre-M SDKs.
 */
public class CPOWrapper {

    private ContentProviderOperation mOperation;
    private int mType;

    public CPOWrapper(ContentProviderOperation builder, int type) {
        mOperation = builder;
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public ContentProviderOperation getOperation() {
        return mOperation;
    }

    public void setOperation(ContentProviderOperation operation) {
        this.mOperation = operation;
    }
}
