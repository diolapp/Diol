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

package app.diol.incallui.baseui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Parent for all fragments that use Presenters and Ui design.
 */
public abstract class BaseFragment<T extends Presenter<U>, U extends Ui> extends Fragment {

    private static final String KEY_FRAGMENT_HIDDEN = "key_fragment_hidden";

    private T presenter;

    protected BaseFragment() {
        presenter = createPresenter();
    }

    public abstract T createPresenter();

    public abstract U getUi();

    /**
     * Presenter will be available after onActivityCreated().
     *
     * @return The presenter associated with this fragment.
     */
    public T getPresenter() {
        return presenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.onUiReady(getUi());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            presenter.onRestoreInstanceState(savedInstanceState);
            if (savedInstanceState.getBoolean(KEY_FRAGMENT_HIDDEN)) {
                getFragmentManager().beginTransaction().hide(this).commit();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onUiDestroy(getUi());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FRAGMENT_HIDDEN, isHidden());
    }
}
