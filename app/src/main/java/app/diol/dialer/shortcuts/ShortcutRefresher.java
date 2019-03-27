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

package app.diol.dialer.shortcuts;

import android.content.Context;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import app.diol.contacts.common.list.ContactEntry;
import app.diol.dialer.common.Assert;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.common.concurrent.DialerExecutor.Worker;
import app.diol.dialer.common.concurrent.DialerExecutorComponent;
import app.diol.dialer.speeddial.loader.SpeedDialUiItem;

/**
 * Refreshes launcher shortcuts from UI components using provided list of contacts.
 */
public final class ShortcutRefresher {

    /**
     * Asynchronously updates launcher shortcuts using the provided list of contacts.
     */
    @MainThread
    public static void refresh(@NonNull Context context, List<ContactEntry> contacts) {
        Assert.isMainThread();
        Assert.isNotNull(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return;
        }

        if (!Shortcuts.areDynamicShortcutsEnabled(context)) {
            return;
        }

        DialerExecutorComponent.get(context)
                .dialerExecutorFactory()
                .createNonUiTaskBuilder(new RefreshWorker(context))
                .build()
                .executeSerial(new ArrayList<>(contacts));
    }

    public static List<ContactEntry> speedDialUiItemsToContactEntries(List<SpeedDialUiItem> items) {
        List<ContactEntry> contactEntries = new ArrayList<>();
        for (SpeedDialUiItem item : items) {
            ContactEntry entry = new ContactEntry();
            entry.id = item.contactId();
            entry.lookupKey = item.lookupKey();
            // SpeedDialUiItem name's are already configured for alternative display orders, so we don't
            // need to account for them in these entries.
            entry.namePrimary = item.name();
            contactEntries.add(entry);
        }
        return contactEntries;
    }

    private static final class RefreshWorker implements Worker<List<ContactEntry>, Void> {
        private final Context context;

        RefreshWorker(Context context) {
            this.context = context;
        }

        @Override
        public Void doInBackground(List<ContactEntry> contacts) {
            LogUtil.enterBlock("ShortcutRefresher.Task.doInBackground");

            // Only dynamic shortcuts are maintained from UI components. Pinned shortcuts are maintained
            // by the job scheduler. This is because a pinned contact may not necessarily still be in the
            // favorites tiles, so refreshing it would require an additional database query. We don't want
            // to incur the cost of that extra database query every time the favorites tiles change.
            new DynamicShortcuts(context, new IconFactory(context)).refresh(contacts); // Blocking

            return null;
        }
    }
}
