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

package app.diol.dialer.protos;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import app.diol.dialer.common.Assert;

/**
 * Useful methods for using Protocol Buffers with Android.
 */
public final class ProtoParsers {

    private ProtoParsers() {
    }

    /**
     * Retrieve a proto from a Bundle which was not created within the current executable/version.
     */
    @SuppressWarnings("unchecked") // We want to eventually optimize away parser classes, so cast
    public static <T extends MessageLite> T get(
            @NonNull Bundle bundle, @NonNull String key, @NonNull T defaultInstance)
            throws InvalidProtocolBufferException {

        Assert.isNotNull(bundle);
        Assert.isNotNull(key);
        Assert.isNotNull(defaultInstance);

        byte[] bytes = bundle.getByteArray(key);
        return (T) mergeFrom(bytes, defaultInstance.getDefaultInstanceForType());
    }

    /**
     * Retrieve a proto from a ContentValues which was not created within the current
     * executable/version.
     */
    @SuppressWarnings("unchecked") // We want to eventually optimize away parser classes, so cast
    public static <T extends MessageLite> T get(
            @NonNull ContentValues contentValues, @NonNull String key, @NonNull T defaultInstance)
            throws InvalidProtocolBufferException {

        Assert.isNotNull(contentValues);
        Assert.isNotNull(key);
        Assert.isNotNull(defaultInstance);

        byte[] bytes = contentValues.getAsByteArray(key);
        return (T) mergeFrom(bytes, defaultInstance.getDefaultInstanceForType());
    }

    /**
     * Retrieve a proto from a trusted bundle which was created within the current executable/version.
     *
     * @throws IllegalStateException if the proto cannot be parsed
     */
    public static <T extends MessageLite> T getTrusted(
            @NonNull Bundle bundle, @NonNull String key, @NonNull T defaultInstance) {
        try {
            return get(bundle, key, defaultInstance);
        } catch (InvalidProtocolBufferException e) {
            throw Assert.createIllegalStateFailException(e.toString());
        }
    }

    /**
     * Retrieve a proto from a trusted ContentValues which was created within the current
     * executable/version.
     *
     * @throws IllegalStateException if the proto cannot be parsed
     */
    public static <T extends MessageLite> T getTrusted(
            @NonNull ContentValues contentValues, @NonNull String key, @NonNull T defaultInstance) {
        try {
            return get(contentValues, key, defaultInstance);
        } catch (InvalidProtocolBufferException e) {
            throw Assert.createIllegalStateFailException(e.toString());
        }
    }

    /**
     * Retrieve a proto from a trusted bundle which was created within the current executable/version.
     *
     * @throws RuntimeException if the proto cannot be parsed
     */
    public static <T extends MessageLite> T getTrusted(
            @NonNull Intent intent, @NonNull String key, @NonNull T defaultInstance) {
        Assert.isNotNull(intent);
        return getTrusted(intent.getExtras(), key, defaultInstance);
    }

    /**
     * Stores a proto in a Bundle, for later retrieval by {@link #get(Bundle, String, MessageLite)} or
     * {@link #getFromInstanceState(Bundle, String, MessageLite)}.
     */
    public static void put(
            @NonNull Bundle bundle, @NonNull String key, @NonNull MessageLite message) {
        Assert.isNotNull(message);
        Assert.isNotNull(bundle);
        Assert.isNotNull(key);
        bundle.putByteArray(key, message.toByteArray());
    }

    /**
     * Stores a proto in an Intent, for later retrieval by {@link #get(Bundle, String, MessageLite)}.
     * Needs separate method because Intent has similar to but different API than Bundle.
     */
    public static void put(
            @NonNull Intent intent, @NonNull String key, @NonNull MessageLite message) {
        Assert.isNotNull(message);
        Assert.isNotNull(intent);
        Assert.isNotNull(key);
        intent.putExtra(key, message.toByteArray());
    }

    /**
     * Parses a proto, throwing parser errors as runtime exceptions.
     */
    @SuppressWarnings("unchecked") // We want to eventually optimize away parser classes
    private static <T extends MessageLite> T mergeFrom(byte[] bytes, T defaultInstance) {
        try {
            return (T) defaultInstance.toBuilder().mergeFrom(bytes).build();
        } catch (InvalidProtocolBufferException e) {
            throw Assert.createIllegalStateFailException(e.toString());
        }
    }
}
