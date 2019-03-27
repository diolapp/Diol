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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * A common superclass that keeps track of whether an {@link Activity} has saved its state yet or
 * not.
 */
public abstract class TransactionSafeFragmentActivity extends FragmentActivity {

    private boolean isSafeToCommitTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSafeToCommitTransactions = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isSafeToCommitTransactions = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isSafeToCommitTransactions = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isSafeToCommitTransactions = false;
    }

    /**
     * Returns true if it is safe to commit {@link FragmentTransaction}s at this time, based on
     * whether {@link Activity#onSaveInstanceState} has been called or not.
     *
     * <p>Make sure that the current activity calls into {@link super.onSaveInstanceState(Bundle
     * outState)} (if that method is overridden), so the flag is properly set.
     */
    public boolean isSafeToCommitTransactions() {
        return isSafeToCommitTransactions;
    }
}
