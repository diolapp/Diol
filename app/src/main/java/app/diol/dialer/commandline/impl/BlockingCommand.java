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

package app.diol.dialer.commandline.impl;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javax.inject.Inject;

import app.diol.dialer.DialerPhoneNumber;
import app.diol.dialer.blocking.Blocking;
import app.diol.dialer.commandline.Arguments;
import app.diol.dialer.commandline.Command;
import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.inject.ApplicationContext;
import app.diol.dialer.phonelookup.PhoneLookupComponent;
import app.diol.dialer.phonelookup.PhoneLookupInfo;
import app.diol.dialer.phonelookup.consolidator.PhoneLookupInfoConsolidator;
import app.diol.dialer.phonenumberproto.DialerPhoneNumberUtil;

/**
 * Block or unblock a number.
 */
public class BlockingCommand implements Command {

    private final Context appContext;
    private final ListeningExecutorService executorService;

    @Inject
    BlockingCommand(
            @ApplicationContext Context context,
            @BackgroundExecutor ListeningExecutorService executorService) {
        this.appContext = context;
        this.executorService = executorService;
    }

    @NonNull
    @Override
    public String getShortDescription() {
        return "block or unblock numbers";
    }

    @NonNull
    @Override
    public String getUsage() {
        return "blocking block|unblock|isblocked number\n\n" + "number should be e.164 formatted";
    }

    @Override
    public ListenableFuture<String> run(Arguments args) throws IllegalCommandLineArgumentException {
        if (args.getPositionals().isEmpty()) {
            return Futures.immediateFuture(getUsage());
        }

        String command = args.getPositionals().get(0);

        if ("block".equals(command)) {
            String number = args.getPositionals().get(1);
            return Futures.transform(
                    Blocking.block(appContext, ImmutableList.of(number), null),
                    (unused) -> "blocked " + number,
                    MoreExecutors.directExecutor());
        }

        if ("unblock".equals(command)) {
            String number = args.getPositionals().get(1);
            return Futures.transform(
                    Blocking.unblock(appContext, ImmutableList.of(number), null),
                    (unused) -> "unblocked " + number,
                    MoreExecutors.directExecutor());
        }

        if ("isblocked".equals(command)) {
            String number = args.getPositionals().get(1);
            ListenableFuture<DialerPhoneNumber> dialerPhoneNumberFuture =
                    executorService.submit(() -> new DialerPhoneNumberUtil().parse(number, null));

            ListenableFuture<PhoneLookupInfo> lookupFuture =
                    Futures.transformAsync(
                            dialerPhoneNumberFuture,
                            (dialerPhoneNumber) ->
                                    PhoneLookupComponent.get(appContext)
                                            .compositePhoneLookup()
                                            .lookup(dialerPhoneNumber),
                            executorService);

            return Futures.transform(
                    lookupFuture,
                    (info) -> new PhoneLookupInfoConsolidator(info).isBlocked() ? "true" : "false",
                    MoreExecutors.directExecutor());
        }

        return Futures.immediateFuture(getUsage());
    }
}
