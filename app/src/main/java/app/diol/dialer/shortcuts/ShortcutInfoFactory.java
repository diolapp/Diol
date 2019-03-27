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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.diol.dialer.common.Assert;

/**
 * Creates {@link ShortcutInfo} objects (which are required by shortcut manager system service) from
 * {@link DialerShortcut} objects (which are package-private convenience data structures).
 *
 * <p>The main work this factory does is create shortcut intents. It also delegates to the {@link
 * IconFactory} to create icons.
 */
@TargetApi(VERSION_CODES.N_MR1) // Shortcuts introduced in N MR1
final class ShortcutInfoFactory {

    /**
     * Key for the contact ID extra (a long) stored as part of the shortcut intent.
     */
    static final String EXTRA_CONTACT_ID = "contactId";

    private final Context context;
    private final IconFactory iconFactory;

    ShortcutInfoFactory(@NonNull Context context, IconFactory iconFactory) {
        this.context = context;
        this.iconFactory = iconFactory;
    }

    /**
     * Builds a list {@link ShortcutInfo} objects from the provided collection of {@link
     * DialerShortcut} objects. This primarily means setting the intent and adding the icon, which
     * {@link DialerShortcut} objects do not hold.
     */
    @WorkerThread
    @NonNull
    List<ShortcutInfo> buildShortcutInfos(@NonNull Map<String, DialerShortcut> shortcutsById) {
        Assert.isWorkerThread();
        List<ShortcutInfo> shortcuts = new ArrayList<>(shortcutsById.size());
        for (DialerShortcut shortcut : shortcutsById.values()) {
            Intent intent = new Intent();
            intent.setClassName(context, "app.diol.dialer.shortcuts.CallContactActivity");
            intent.setData(shortcut.getLookupUri());
            intent.setAction("app.diol.dialer.shortcuts.CALL_CONTACT");
            intent.putExtra(EXTRA_CONTACT_ID, shortcut.getContactId());

            ShortcutInfo.Builder shortcutInfo =
                    new ShortcutInfo.Builder(context, shortcut.getShortcutId())
                            .setIntent(intent)
                            .setShortLabel(shortcut.getShortLabel())
                            .setLongLabel(shortcut.getLongLabel())
                            .setIcon(iconFactory.create(shortcut));

            if (shortcut.getRank() != DialerShortcut.NO_RANK) {
                shortcutInfo.setRank(shortcut.getRank());
            }
            shortcuts.add(shortcutInfo.build());
        }
        return shortcuts;
    }

    /**
     * Creates a copy of the provided {@link ShortcutInfo} but with an updated icon fetched from
     * contacts provider.
     */
    @WorkerThread
    @NonNull
    ShortcutInfo withUpdatedIcon(ShortcutInfo info) {
        Assert.isWorkerThread();
        return new ShortcutInfo.Builder(context, info.getId())
                .setIntent(info.getIntent())
                .setShortLabel(info.getShortLabel())
                .setLongLabel(info.getLongLabel())
                .setRank(info.getRank())
                .setIcon(iconFactory.create(info))
                .build();
    }
}
