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

package app.diol.dialer.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringDef;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import app.diol.R;

/**
 * Utility class for dealing with network
 */
public class NetworkUtil {

    /* Returns the current network type. */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    @NetworkType
    public static String getCurrentNetworkType(@Nullable Context context) {
        if (context == null) {
            return NetworkType.NONE;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return getNetworkType(connectivityManager.getActiveNetworkInfo());
    }

    /* Returns the current network info. */
    @Nullable
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static NetworkInfo getCurrentNetworkInfo(@Nullable Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    /**
     * Returns the current network type as a string. For mobile network types the subtype name of the
     * network is appended.
     */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static String getCurrentNetworkTypeName(@Nullable Context context) {
        if (context == null) {
            return NetworkType.NONE;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        @NetworkType String networkType = getNetworkType(netInfo);
        if (isNetworkTypeMobile(networkType)) {
            return networkType + " (" + netInfo.getSubtypeName() + ")";
        }
        return networkType;
    }

    @NetworkType
    public static String getNetworkType(@Nullable NetworkInfo netInfo) {
        if (netInfo == null || !netInfo.isConnected()) {
            return NetworkType.NONE;
        }
        switch (netInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return NetworkType.WIFI;
            case ConnectivityManager.TYPE_MOBILE:
                return getMobileNetworkType(netInfo.getSubtype());
            default:
                return NetworkType.UNKNOWN;
        }
    }

    public static boolean isNetworkTypeMobile(@NetworkType String networkType) {
        return Objects.equals(networkType, NetworkType.MOBILE_2G)
                || Objects.equals(networkType, NetworkType.MOBILE_3G)
                || Objects.equals(networkType, NetworkType.MOBILE_4G);
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    public static String getCurrentNetworkName(Context context) {
        @NetworkType String networkType = getCurrentNetworkType(context);
        switch (networkType) {
            case NetworkType.WIFI:
                return getWifiNetworkName(context);
            case NetworkType.MOBILE_2G:
            case NetworkType.MOBILE_3G:
            case NetworkType.MOBILE_4G:
            case NetworkType.MOBILE_UNKNOWN:
                return getMobileNetworkName(context);
            default:
                return "";
        }
    }

    private static String getWifiNetworkName(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String name = null;
        if (context.checkSelfPermission("android.permission.ACCESS_WIFI_STATE")
                == PackageManager.PERMISSION_GRANTED) {
            //noinspection MissingPermission
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo == null) {
                return "";
            }
            name = wifiInfo.getSSID();
        }
        return TextUtils.isEmpty(name)
                ? context.getString(R.string.network_name_wifi)
                : name.replaceAll("\"", "");
    }

    private static String getMobileNetworkName(Context context) {
        TelephonyManager telephonyMgr =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String name = telephonyMgr.getNetworkOperatorName();
        return TextUtils.isEmpty(name)
                ? context.getString(R.string.network_name_mobile)
                : name.replaceAll("\"", "");
    }

    @NetworkType
    private static String getMobileNetworkType(int networkSubtype) {
        switch (networkSubtype) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NetworkType.MOBILE_2G;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return NetworkType.MOBILE_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NetworkType.MOBILE_4G;
            default:
                return NetworkType.MOBILE_UNKNOWN;
        }
    }

    /**
     * Network types.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef(
            value = {
                    NetworkType.NONE,
                    NetworkType.WIFI,
                    NetworkType.MOBILE_2G,
                    NetworkType.MOBILE_3G,
                    NetworkType.MOBILE_4G,
                    NetworkType.MOBILE_UNKNOWN,
                    NetworkType.UNKNOWN
            }
    )
    public @interface NetworkType {

        String NONE = "NONE";
        String WIFI = "WIFI";
        String MOBILE_2G = "MOBILE_2G";
        String MOBILE_3G = "MOBILE_3G";
        String MOBILE_4G = "MOBILE_4G";
        String MOBILE_UNKNOWN = "MOBILE_UNKNOWN";
        String UNKNOWN = "UNKNOWN";
    }
}
