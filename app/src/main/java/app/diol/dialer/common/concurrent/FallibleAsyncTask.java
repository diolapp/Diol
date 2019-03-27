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

package app.diol.dialer.common.concurrent;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import app.diol.dialer.common.concurrent.FallibleAsyncTask.FallibleTaskResult;

/**
 * A task that runs work in the background, passing Throwables from {@link
 * #doInBackground(Object[])} to {@link #onPostExecute(Object)} through a {@link
 * FallibleTaskResult}.
 *
 * @param <ParamsT>   the type of the parameters sent to the task upon execution
 * @param <ProgressT> the type of the progress units published during the background computation
 * @param <ResultT>   the type of the result of the background computation
 * @deprecated Please use {@link DialerExecutors}.
 */
@Deprecated
public abstract class FallibleAsyncTask<ParamsT, ProgressT, ResultT>
        extends AsyncTask<ParamsT, ProgressT, FallibleTaskResult<ResultT>> {

    @Override
    protected final FallibleTaskResult<ResultT> doInBackground(ParamsT... params) {
        try {
            return FallibleTaskResult.createSuccessResult(doInBackgroundFallible(params));
        } catch (Throwable t) {
            return FallibleTaskResult.createFailureResult(t);
        }
    }

    /**
     * Performs background work that may result in a Throwable.
     */
    @Nullable
    protected abstract ResultT doInBackgroundFallible(ParamsT... params) throws Throwable;

    /**
     * Holds the result of processing from {@link #doInBackground(Object[])}.
     *
     * @param <ResultT> the type of the result of the background computation
     */
    @AutoValue
    public abstract static class FallibleTaskResult<ResultT> {

        /**
         * Creates an instance of FallibleTaskResult for the given throwable.
         */
        private static <ResultT> FallibleTaskResult<ResultT> createFailureResult(@NonNull Throwable t) {
            return new AutoValue_FallibleAsyncTask_FallibleTaskResult<>(t, null);
        }

        /**
         * Creates an instance of FallibleTaskResult for the given result.
         */
        private static <ResultT> FallibleTaskResult<ResultT> createSuccessResult(
                @Nullable ResultT result) {
            return new AutoValue_FallibleAsyncTask_FallibleTaskResult<>(null, result);
        }

        /**
         * Returns the Throwable thrown in {@link #doInBackground(Object[])}, or {@code null} if
         * background work completed without throwing.
         */
        @Nullable
        public abstract Throwable getThrowable();

        /**
         * Returns the result of {@link #doInBackground(Object[])}, which may be {@code null}, or {@code
         * null} if the background work threw a Throwable.
         *
         * <p>Use {@link #isFailure()} to determine if a {@code null} return is the result of a
         * Throwable from the background work.
         */
        @Nullable
        public abstract ResultT getResult();

        /**
         * Returns {@code true} if this object is the result of background work that threw a Throwable.
         */
        public boolean isFailure() {
            //noinspection ThrowableResultOfMethodCallIgnored
            return getThrowable() != null;
        }
    }
}
