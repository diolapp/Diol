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

package app.diol.incallui;

import android.net.Uri;
import android.telecom.PhoneAccount;
import android.telephony.PhoneNumberUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import app.diol.dialer.common.LogUtil;

/**
 * Manages logging for the entire class.
 */
public class Log {

    public static void d(String tag, String msg) {
        LogUtil.d(tag, msg);
    }

    public static void d(Object obj, String msg) {
        LogUtil.d(getPrefix(obj), msg);
    }

    public static void d(Object obj, String str1, Object str2) {
        LogUtil.d(getPrefix(obj), str1 + str2);
    }

    public static void v(Object obj, String msg) {
        LogUtil.v(getPrefix(obj), msg);
    }

    public static void v(Object obj, String str1, Object str2) {
        LogUtil.v(getPrefix(obj), str1 + str2);
    }

    public static void e(String tag, String msg, Exception e) {
        LogUtil.e(tag, msg, e);
    }

    public static void e(String tag, String msg) {
        LogUtil.e(tag, msg);
    }

    public static void e(Object obj, String msg, Exception e) {
        LogUtil.e(getPrefix(obj), msg, e);
    }

    public static void e(Object obj, String msg) {
        LogUtil.e(getPrefix(obj), msg);
    }

    public static void i(String tag, String msg) {
        LogUtil.i(tag, msg);
    }

    public static void i(Object obj, String msg) {
        LogUtil.i(getPrefix(obj), msg);
    }

    public static void w(Object obj, String msg) {
        LogUtil.w(getPrefix(obj), msg);
    }

    public static String piiHandle(Object pii) {
        if (pii == null || LogUtil.isVerboseEnabled()) {
            return String.valueOf(pii);
        }

        if (pii instanceof Uri) {
            Uri uri = (Uri) pii;

            // All Uri's which are not "tel" go through normal pii() method.
            if (!PhoneAccount.SCHEME_TEL.equals(uri.getScheme())) {
                return pii(pii);
            } else {
                pii = uri.getSchemeSpecificPart();
            }
        }

        String originalString = String.valueOf(pii);
        StringBuilder stringBuilder = new StringBuilder(originalString.length());
        for (char c : originalString.toCharArray()) {
            if (PhoneNumberUtils.isDialable(c)) {
                stringBuilder.append('*');
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Redact personally identifiable information for production users. If we are running in verbose
     * mode, return the original string, otherwise return a SHA-1 hash of the input string.
     */
    public static String pii(Object pii) {
        if (pii == null || LogUtil.isVerboseEnabled()) {
            return String.valueOf(pii);
        }
        return "[" + secureHash(String.valueOf(pii).getBytes()) + "]";
    }

    private static String secureHash(byte[] input) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        messageDigest.update(input);
        byte[] result = messageDigest.digest();
        return encodeHex(result);
    }

    private static String encodeHex(byte[] bytes) {
        StringBuffer hex = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            int byteIntValue = bytes[i] & 0xff;
            if (byteIntValue < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toString(byteIntValue, 16));
        }

        return hex.toString();
    }

    private static String getPrefix(Object obj) {
        return (obj == null ? "" : (obj.getClass().getSimpleName()));
    }
}
