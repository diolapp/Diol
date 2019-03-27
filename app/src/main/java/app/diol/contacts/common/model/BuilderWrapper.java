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

import android.content.ContentProviderOperation.Builder;

/**
 * This class is created for the purpose of compatibility and make the type of
 * ContentProviderOperation available on pre-M SDKs. Since ContentProviderOperation is usually
 * created by Builder and we don’t have access to the type via Builder, so we need to create a
 * wrapper class for Builder first and include type. Then we could use the builder and the type in
 * this class to create a wrapper of ContentProviderOperation.
 */
public class BuilderWrapper {

    private Builder mBuilder;
    private int mType;

    public BuilderWrapper(Builder builder, int type) {
        mBuilder = builder;
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public Builder getBuilder() {
        return mBuilder;
    }

    public void setBuilder(Builder mBuilder) {
        this.mBuilder = mBuilder;
    }
}
