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

package app.diol.dialer.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.diol.R;
import app.diol.dialer.theme.base.ThemeComponent;

public class EmptyContentView extends LinearLayout implements View.OnClickListener {

    public static final int NO_LABEL = 0;
    public static final int NO_IMAGE = 0;
    private ImageView imageView;
    private TextView descriptionView;
    private TextView actionView;
    private OnEmptyViewActionButtonClickedListener onActionButtonClickedListener;
    private @StringRes
    int actionLabel;

    public EmptyContentView(Context context) {
        this(context, null);
    }

    public EmptyContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EmptyContentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateLayout();

        // Don't let touches fall through the empty view.
        setClickable(true);
        imageView = (ImageView) findViewById(R.id.empty_list_view_image);
        descriptionView = (TextView) findViewById(R.id.empty_list_view_message);
        actionView = (TextView) findViewById(R.id.empty_list_view_action);
        actionView.setOnClickListener(this);

        imageView.setImageTintList(
                ColorStateList.valueOf(ThemeComponent.get(context).theme().getColorIconSecondary()));
    }

    public void setDescription(int resourceId) {
        if (resourceId == NO_LABEL) {
            descriptionView.setText(null);
            descriptionView.setVisibility(View.GONE);
        } else {
            descriptionView.setText(resourceId);
            descriptionView.setVisibility(View.VISIBLE);
        }
    }

    public void setImage(int resourceId) {
        if (resourceId == NO_LABEL) {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageResource(resourceId);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public @StringRes
    int getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(@StringRes int resourceId) {
        actionLabel = resourceId;
        if (resourceId == NO_LABEL) {
            actionView.setText(null);
            actionView.setVisibility(View.GONE);
        } else {
            actionView.setText(resourceId);
            actionView.setVisibility(View.VISIBLE);
        }
    }

    public boolean isShowingContent() {
        return imageView.getVisibility() == View.VISIBLE
                || descriptionView.getVisibility() == View.VISIBLE
                || actionView.getVisibility() == View.VISIBLE;
    }

    public void setActionClickedListener(OnEmptyViewActionButtonClickedListener listener) {
        onActionButtonClickedListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (onActionButtonClickedListener != null) {
            onActionButtonClickedListener.onEmptyViewActionButtonClicked();
        }
    }

    protected void inflateLayout() {
        setOrientation(LinearLayout.VERTICAL);
        final LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.empty_content_view, this);
    }

    /**
     * Listener to call when action button is clicked.
     */
    public interface OnEmptyViewActionButtonClickedListener {
        void onEmptyViewActionButtonClicked();
    }
}
