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

package app.diol.contacts.common.model.account;

import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.diol.R;
import app.diol.contacts.common.model.dataitem.DataKind;
import app.diol.contacts.common.util.CommonDateUtils;
import app.diol.dialer.common.LogUtil;

public class GoogleAccountType extends BaseAccountType {

    /**
     * The package name that we should load contacts.xml from and rely on to handle G+ account
     * actions. Even though this points to gms, in some cases gms will still hand off responsibility
     * to the G+ app.
     */
    public static final String PLUS_EXTENSION_PACKAGE_NAME = "com.google.android.gms";

    public static final String ACCOUNT_TYPE = "com.google";
    private static final String TAG = "GoogleAccountType";
    private static final List<String> mExtensionPackages =
            new ArrayList<>(Collections.singletonList(PLUS_EXTENSION_PACKAGE_NAME));

    public GoogleAccountType(Context context, String authenticatorPackageName) {
        this.accountType = ACCOUNT_TYPE;
        this.resourcePackageName = null;
        this.syncAdapterPackageName = authenticatorPackageName;

        try {
            addDataKindStructuredName(context);
            addDataKindDisplayName(context);
            addDataKindPhoneticName(context);
            addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            addDataKindStructuredPostal(context);
            addDataKindIm(context);
            addDataKindOrganization(context);
            addDataKindPhoto(context);
            addDataKindNote(context);
            addDataKindWebsite(context);
            addDataKindSipAddress(context);
            addDataKindGroupMembership(context);
            addDataKindRelation(context);
            addDataKindEvent(context);

            mIsInitialized = true;
        } catch (DefinitionException e) {
            LogUtil.e(TAG, "Problem building account type", e);
        }
    }

    @Override
    public List<String> getExtensionPackageNames() {
        return mExtensionPackages;
    }

    @Override
    protected DataKind addDataKindPhone(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindPhone(context);

        kind.typeColumn = Phone.TYPE;
        kind.typeList = new ArrayList<>();
        kind.typeList.add(buildPhoneType(Phone.TYPE_MOBILE));
        kind.typeList.add(buildPhoneType(Phone.TYPE_WORK));
        kind.typeList.add(buildPhoneType(Phone.TYPE_HOME));
        kind.typeList.add(buildPhoneType(Phone.TYPE_MAIN));
        kind.typeList.add(buildPhoneType(Phone.TYPE_FAX_WORK).setSecondary(true));
        kind.typeList.add(buildPhoneType(Phone.TYPE_FAX_HOME).setSecondary(true));
        kind.typeList.add(buildPhoneType(Phone.TYPE_PAGER).setSecondary(true));
        kind.typeList.add(buildPhoneType(Phone.TYPE_OTHER));
        kind.typeList.add(
                buildPhoneType(Phone.TYPE_CUSTOM).setSecondary(true).setCustomColumn(Phone.LABEL));

        kind.fieldList = new ArrayList<>();
        kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));

        return kind;
    }

    @Override
    protected DataKind addDataKindEmail(Context context) throws DefinitionException {
        final DataKind kind = super.addDataKindEmail(context);

        kind.typeColumn = Email.TYPE;
        kind.typeList = new ArrayList<>();
        kind.typeList.add(buildEmailType(Email.TYPE_HOME));
        kind.typeList.add(buildEmailType(Email.TYPE_WORK));
        kind.typeList.add(buildEmailType(Email.TYPE_OTHER));
        kind.typeList.add(
                buildEmailType(Email.TYPE_CUSTOM).setSecondary(true).setCustomColumn(Email.LABEL));

        kind.fieldList = new ArrayList<>();
        kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));

        return kind;
    }

    private DataKind addDataKindRelation(Context context) throws DefinitionException {
        DataKind kind =
                addKind(
                        new DataKind(
                                Relation.CONTENT_ITEM_TYPE,
                                R.string.relationLabelsGroup,
                                Weight.RELATIONSHIP,
                                true));
        kind.actionHeader = new RelationActionInflater();
        kind.actionBody = new SimpleInflater(Relation.NAME);

        kind.typeColumn = Relation.TYPE;
        kind.typeList = new ArrayList<>();
        kind.typeList.add(buildRelationType(Relation.TYPE_ASSISTANT));
        kind.typeList.add(buildRelationType(Relation.TYPE_BROTHER));
        kind.typeList.add(buildRelationType(Relation.TYPE_CHILD));
        kind.typeList.add(buildRelationType(Relation.TYPE_DOMESTIC_PARTNER));
        kind.typeList.add(buildRelationType(Relation.TYPE_FATHER));
        kind.typeList.add(buildRelationType(Relation.TYPE_FRIEND));
        kind.typeList.add(buildRelationType(Relation.TYPE_MANAGER));
        kind.typeList.add(buildRelationType(Relation.TYPE_MOTHER));
        kind.typeList.add(buildRelationType(Relation.TYPE_PARENT));
        kind.typeList.add(buildRelationType(Relation.TYPE_PARTNER));
        kind.typeList.add(buildRelationType(Relation.TYPE_REFERRED_BY));
        kind.typeList.add(buildRelationType(Relation.TYPE_RELATIVE));
        kind.typeList.add(buildRelationType(Relation.TYPE_SISTER));
        kind.typeList.add(buildRelationType(Relation.TYPE_SPOUSE));
        kind.typeList.add(
                buildRelationType(Relation.TYPE_CUSTOM).setSecondary(true).setCustomColumn(Relation.LABEL));

        kind.defaultValues = new ContentValues();
        kind.defaultValues.put(Relation.TYPE, Relation.TYPE_SPOUSE);

        kind.fieldList = new ArrayList<>();
        kind.fieldList.add(new EditField(Relation.DATA, R.string.relationLabelsGroup, FLAGS_RELATION));

        return kind;
    }

    private DataKind addDataKindEvent(Context context) throws DefinitionException {
        DataKind kind =
                addKind(
                        new DataKind(Event.CONTENT_ITEM_TYPE, R.string.eventLabelsGroup, Weight.EVENT, true));
        kind.actionHeader = new EventActionInflater();
        kind.actionBody = new SimpleInflater(Event.START_DATE);

        kind.typeColumn = Event.TYPE;
        kind.typeList = new ArrayList<>();
        kind.dateFormatWithoutYear = CommonDateUtils.NO_YEAR_DATE_FORMAT;
        kind.dateFormatWithYear = CommonDateUtils.FULL_DATE_FORMAT;
        kind.typeList.add(buildEventType(Event.TYPE_BIRTHDAY, true).setSpecificMax(1));
        kind.typeList.add(buildEventType(Event.TYPE_ANNIVERSARY, false));
        kind.typeList.add(buildEventType(Event.TYPE_OTHER, false));
        kind.typeList.add(
                buildEventType(Event.TYPE_CUSTOM, false).setSecondary(true).setCustomColumn(Event.LABEL));

        kind.defaultValues = new ContentValues();
        kind.defaultValues.put(Event.TYPE, Event.TYPE_BIRTHDAY);

        kind.fieldList = new ArrayList<>();
        kind.fieldList.add(new EditField(Event.DATA, R.string.eventLabelsGroup, FLAGS_EVENT));

        return kind;
    }

    @Override
    public boolean isGroupMembershipEditable() {
        return true;
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }

    @Override
    public String getViewContactNotifyServiceClassName() {
        return "com.google.android.syncadapters.contacts." + "SyncHighResPhotoIntentService";
    }

    @Override
    public String getViewContactNotifyServicePackageName() {
        return "com.google.android.syncadapters.contacts";
    }
}
