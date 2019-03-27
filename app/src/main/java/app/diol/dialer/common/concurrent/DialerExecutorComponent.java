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

import android.app.FragmentManager;
import android.content.Context;

import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.concurrent.ExecutorService;

import app.diol.dialer.common.concurrent.Annotations.BackgroundExecutor;
import app.diol.dialer.common.concurrent.Annotations.LightweightExecutor;
import app.diol.dialer.common.concurrent.Annotations.NonUiParallel;
import app.diol.dialer.common.concurrent.Annotations.Ui;
import app.diol.dialer.inject.HasRootComponent;
import app.diol.dialer.inject.IncludeInDialerRoot;
import dagger.Subcomponent;

/**
 * Dagger component which provides a {@link DialerExecutorFactory}.
 */
@Subcomponent
public abstract class DialerExecutorComponent {

    public static DialerExecutorComponent get(Context context) {
        return ((DialerExecutorComponent.HasComponent)
                ((HasRootComponent) context.getApplicationContext()).component())
                .dialerExecutorComponent();
    }

    public abstract DialerExecutorFactory dialerExecutorFactory();

    @NonUiParallel
    public abstract ExecutorService lowPriorityThreadPool();

    @Ui
    public abstract ListeningExecutorService uiExecutor();

    @BackgroundExecutor
    public abstract ListeningExecutorService backgroundExecutor();

    @LightweightExecutor
    public abstract ListeningExecutorService lightweightExecutor();

    public <OutputT> UiListener<OutputT> createUiListener(
            FragmentManager fragmentManager, String taskId) {
        return UiListener.create(fragmentManager, taskId);
    }

    /**
     * Version of {@link #createUiListener(FragmentManager, String)} that accepts support fragment
     * manager.
     */
    public <OutputT> SupportUiListener<OutputT> createUiListener(
            android.support.v4.app.FragmentManager fragmentManager, String taskId) {
        return SupportUiListener.create(fragmentManager, taskId);
    }

    /**
     * Used to refer to the root application component.
     */
    @IncludeInDialerRoot
    public interface HasComponent {
        DialerExecutorComponent dialerExecutorComponent();
    }
}
