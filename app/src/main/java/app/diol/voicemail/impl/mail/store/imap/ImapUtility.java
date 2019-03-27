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
package app.diol.voicemail.impl.mail.store.imap;

import java.util.ArrayList;

import app.diol.voicemail.impl.mail.utils.LogUtils;

/**
 * Utility methods for use with IMAP.
 */
public class ImapUtility {
    public static final String TAG = "ImapUtility";

    /**
     * Apply quoting rules per IMAP RFC, quoted = DQUOTE *QUOTED-CHAR DQUOTE
     * QUOTED-CHAR = <any TEXT-CHAR except quoted-specials> / "\" quoted-specials
     * quoted-specials = DQUOTE / "\"
     *
     * <p>
     * This is used primarily for IMAP login, but might be useful elsewhere.
     *
     * <p>
     * NOTE: Not very efficient - you may wish to preflight this, or perhaps it
     * should check for trouble chars before calling the replace functions.
     *
     * @param s The string to be quoted.
     * @return A copy of the string, having undergone quoting as described above
     */
    public static String imapQuoted(String s) {

        // First, quote any backslashes by replacing \ with \\
        // regex Pattern: \\ (Java string const = \\\\)
        // Substitute: \\\\ (Java string const = \\\\\\\\)
        String result = s.replaceAll("\\\\", "\\\\\\\\");

        // Then, quote any double-quotes by replacing " with \"
        // regex Pattern: " (Java string const = \")
        // Substitute: \\" (Java string const = \\\\\")
        result = result.replaceAll("\"", "\\\\\"");

        // return string with quotes around it
        return "\"" + result + "\"";
    }

    /**
     * Gets all of the values in a sequence set per RFC 3501. Any ranges are
     * expanded into a list of individual numbers. If the set is invalid, an empty
     * array is returned.
     *
     * <pre>
     * sequence-number = nz-number / "*"
     * sequence-range  = sequence-number ":" sequence-number
     * sequence-set    = (sequence-number / sequence-range) *("," sequence-set)
     * </pre>
     */
    public static String[] getImapSequenceValues(String set) {
        ArrayList<String> list = new ArrayList<String>();
        if (set != null) {
            String[] setItems = set.split(",");
            for (String item : setItems) {
                if (item.indexOf(':') == -1) {
                    // simple item
                    try {
                        Integer.parseInt(item); // Don't need the value; just ensure it's valid
                        list.add(item);
                    } catch (NumberFormatException e) {
                        LogUtils.d(TAG, "Invalid UID value", e);
                    }
                } else {
                    // range
                    for (String rangeItem : getImapRangeValues(item)) {
                        list.add(rangeItem);
                    }
                }
            }
        }
        String[] stringList = new String[list.size()];
        return list.toArray(stringList);
    }

    /**
     * Expand the given number range into a list of individual numbers. If the range
     * is not valid, an empty array is returned.
     *
     * <pre>
     * sequence-number = nz-number / "*"
     * sequence-range  = sequence-number ":" sequence-number
     * sequence-set    = (sequence-number / sequence-range) *("," sequence-set)
     * </pre>
     */
    public static String[] getImapRangeValues(String range) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            if (range != null) {
                int colonPos = range.indexOf(':');
                if (colonPos > 0) {
                    int first = Integer.parseInt(range.substring(0, colonPos));
                    int second = Integer.parseInt(range.substring(colonPos + 1));
                    if (first < second) {
                        for (int i = first; i <= second; i++) {
                            list.add(Integer.toString(i));
                        }
                    } else {
                        for (int i = first; i >= second; i--) {
                            list.add(Integer.toString(i));
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            LogUtils.d(TAG, "Invalid range value", e);
        }
        String[] stringList = new String[list.size()];
        return list.toArray(stringList);
    }
}
