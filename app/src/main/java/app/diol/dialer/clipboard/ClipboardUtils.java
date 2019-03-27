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

package app.diol.dialer.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import app.diol.R;

/**
 * Copies provided label and text to the clipboard and optionally shows a "text copied" toast.
 */
public final class ClipboardUtils {

    private ClipboardUtils() {
    }

    /**
     * Copy a text to clipboard.
     *
     * @param context   Context
     * @param label     Label to show to the user describing this clip.
     * @param text      Text to copy.
     * @param showToast If {@code true}, a toast is shown to the user.
     */
    public static void copyText(
            Context context, CharSequence label, CharSequence text, boolean showToast) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        ClipboardManager clipboardManager =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label == null ? "" : label, text);
        clipboardManager.setPrimaryClip(clipData);

        if (showToast) {
            String toastText = context.getString(R.string.toast_text_copied);
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        }
    }
}
