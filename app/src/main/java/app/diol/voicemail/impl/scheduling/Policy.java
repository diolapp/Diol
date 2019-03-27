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

package app.diol.voicemail.impl.scheduling;

import android.os.Bundle;

/**
 * A set of listeners managed by {@link BaseTask} for common behaviors such as
 * retrying. Call {@link BaseTask#addPolicy(Policy)} to add a policy.
 */
public interface Policy {

    void onCreate(BaseTask task, Bundle extras);

    void onBeforeExecute();

    void onCompleted();

    void onFail();

    void onDuplicatedTaskAdded();
}
