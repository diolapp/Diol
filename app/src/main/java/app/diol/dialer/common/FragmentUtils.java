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

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;

import app.diol.dialer.main.MainActivityPeer;

/**
 * Utility methods for working with Fragments
 */
public class FragmentUtils {

    private static Object parentForTesting;

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setParentForTesting(Object parentForTesting) {
        FragmentUtils.parentForTesting = parentForTesting;
    }

    /**
     * Returns an instance of the {@code callbackInterface} that is defined in the parent of the
     * {@code fragment}, or null if no such call back can be found.
     */
    @CheckResult(suggest = "#checkParent(Fragment, Class)}")
    @Nullable
    public static <T> T getParent(@NonNull Fragment fragment, @NonNull Class<T> callbackInterface) {
        if (callbackInterface.isInstance(parentForTesting)) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = (T) parentForTesting;
            return parent;
        }

        Fragment parentFragment = fragment.getParentFragment();
        if (callbackInterface.isInstance(parentFragment)) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = (T) parentFragment;
            return parent;
        } else if (callbackInterface.isInstance(fragment.getActivity())) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = (T) fragment.getActivity();
            return parent;
        } else if (fragment.getActivity() instanceof FragmentUtilListener) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = ((FragmentUtilListener) fragment.getActivity()).getImpl(callbackInterface);
            return parent;
        } else if (fragment.getActivity() instanceof MainActivityPeer.PeerSupplier) {
            MainActivityPeer peer = ((MainActivityPeer.PeerSupplier) fragment.getActivity()).getPeer();
            if (peer instanceof FragmentUtilListener) {
                return ((FragmentUtilListener) peer).getImpl(callbackInterface);
            }
        }
        return null;
    }

    /**
     * Returns an instance of the {@code callbackInterface} that is defined in the parent of the
     * {@code fragment}, or null if no such call back can be found.
     */
    @CheckResult(suggest = "#checkParent(Fragment, Class)}")
    @Nullable
    public static <T> T getParent(
            @NonNull android.app.Fragment fragment, @NonNull Class<T> callbackInterface) {
        if (callbackInterface.isInstance(parentForTesting)) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = (T) parentForTesting;
            return parent;
        }

        android.app.Fragment parentFragment = fragment.getParentFragment();
        if (callbackInterface.isInstance(parentFragment)) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = (T) parentFragment;
            return parent;
        } else if (callbackInterface.isInstance(fragment.getActivity())) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = (T) fragment.getActivity();
            return parent;
        } else if (fragment.getActivity() instanceof FragmentUtilListener) {
            @SuppressWarnings("unchecked") // Casts are checked using runtime methods
                    T parent = ((FragmentUtilListener) fragment.getActivity()).getImpl(callbackInterface);
            return parent;
        } else if (fragment.getActivity() instanceof MainActivityPeer.PeerSupplier) {
            MainActivityPeer peer = ((MainActivityPeer.PeerSupplier) fragment.getActivity()).getPeer();
            if (peer instanceof FragmentUtilListener) {
                return ((FragmentUtilListener) peer).getImpl(callbackInterface);
            }
        }
        return null;
    }

    /**
     * Returns the parent or throws. Should perform check elsewhere(e.g. onAttach, newInstance).
     */
    @NonNull
    public static <T> T getParentUnsafe(
            @NonNull Fragment fragment, @NonNull Class<T> callbackInterface) {
        return Assert.isNotNull(getParent(fragment, callbackInterface));
    }

    /**
     * Version of {@link #getParentUnsafe(Fragment, Class)} which supports {@link
     * android.app.Fragment}.
     */
    @NonNull
    public static <T> T getParentUnsafe(
            @NonNull android.app.Fragment fragment, @NonNull Class<T> callbackInterface) {
        return Assert.isNotNull(getParent(fragment, callbackInterface));
    }

    /**
     * Ensures fragment has a parent that implements the corresponding interface
     *
     * @param frag              The Fragment whose parents are to be checked
     * @param callbackInterface The interface class that a parent should implement
     * @throws IllegalStateException if no parents are found that implement callbackInterface
     */
    public static void checkParent(@NonNull Fragment frag, @NonNull Class<?> callbackInterface)
            throws IllegalStateException {
        if (parentForTesting != null) {
            return;
        }
        if (FragmentUtils.getParent(frag, callbackInterface) == null) {
            String parent =
                    frag.getParentFragment() == null
                            ? frag.getActivity().getClass().getName()
                            : frag.getParentFragment().getClass().getName();
            throw new IllegalStateException(
                    frag.getClass().getName()
                            + " must be added to a parent"
                            + " that implements "
                            + callbackInterface.getName()
                            + ". Instead found "
                            + parent);
        }
    }

    /**
     * Useful interface for activities that don't want to implement arbitrary listeners.
     */
    public interface FragmentUtilListener {

        /**
         * Returns an implementation of T if parent has one, otherwise null.
         */
        @Nullable
        <T> T getImpl(Class<T> callbackInterface);
    }
}
