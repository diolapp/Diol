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

package app.diol.contacts.common;

/**
 * Meta-data for a contact group. We load all groups associated with the contact's constituent
 * accounts.
 */
public final class GroupMetaData {

    private String mAccountName;
    private String mAccountType;
    private String mDataSet;
    private long mGroupId;
    private String mTitle;
    private boolean mDefaultGroup;
    private boolean mFavorites;

    public GroupMetaData(
            String accountName,
            String accountType,
            String dataSet,
            long groupId,
            String title,
            boolean defaultGroup,
            boolean favorites) {
        this.mAccountName = accountName;
        this.mAccountType = accountType;
        this.mDataSet = dataSet;
        this.mGroupId = groupId;
        this.mTitle = title;
        this.mDefaultGroup = defaultGroup;
        this.mFavorites = favorites;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getAccountType() {
        return mAccountType;
    }

    public String getDataSet() {
        return mDataSet;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isDefaultGroup() {
        return mDefaultGroup;
    }

    public boolean isFavorites() {
        return mFavorites;
    }
}
