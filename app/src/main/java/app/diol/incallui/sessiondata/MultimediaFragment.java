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

package app.diol.incallui.sessiondata;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import app.diol.R;
import app.diol.dialer.common.FragmentUtils;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.multimedia.MultimediaData;
import app.diol.dialer.theme.base.ThemeComponent;
import app.diol.incallui.maps.MapsComponent;

/**
 * Displays info from {@link MultimediaData MultimediaData}.
 *
 * <p>Currently displays image, location (as a map), and message that come bundled with
 * MultimediaData when calling {@link #newInstance(MultimediaData, boolean, boolean, boolean)}.
 */
public class MultimediaFragment extends Fragment implements AvatarPresenter {

    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_IMAGE = "image";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_INTERACTIVE = "interactive";
    private static final String ARG_SHOW_AVATAR = "show_avatar";
    private static final String ARG_IS_SPAM = "is_spam";
    private ImageView avatarImageView;

    private boolean showAvatar;
    private boolean isSpam;

    public static MultimediaFragment newInstance(
            @NonNull MultimediaData multimediaData,
            boolean isInteractive,
            boolean showAvatar,
            boolean isSpam) {
        return newInstance(
                multimediaData.getText(),
                multimediaData.getImageUri(),
                multimediaData.getLocation(),
                isInteractive,
                showAvatar,
                isSpam);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static MultimediaFragment newInstance(
            @Nullable String subject,
            @Nullable Uri imageUri,
            @Nullable Location location,
            boolean isInteractive,
            boolean showAvatar,
            boolean isSpam) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        args.putParcelable(ARG_IMAGE, imageUri);
        args.putParcelable(ARG_LOCATION, location);
        args.putBoolean(ARG_INTERACTIVE, isInteractive);
        args.putBoolean(ARG_SHOW_AVATAR, showAvatar);
        args.putBoolean(ARG_IS_SPAM, isSpam);
        MultimediaFragment fragment = new MultimediaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        showAvatar = getArguments().getBoolean(ARG_SHOW_AVATAR);
        isSpam = getArguments().getBoolean(ARG_IS_SPAM);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        layoutInflater =
                layoutInflater.cloneInContext(
                        ThemeComponent.get(getContext()).theme().getThemedContext(getContext()));

        if (isSpam) {
            LogUtil.i("MultimediaFragment.onCreateView", "show spam layout");
            return layoutInflater.inflate(R.layout.fragment_spam, viewGroup, false);
        }

        boolean hasImage = getImageUri() != null;
        boolean hasSubject = !TextUtils.isEmpty(getSubject());
        boolean hasMap = getLocation() != null;
        if (hasMap && MapsComponent.get(getContext()).getMaps().isAvailable()) {
            if (hasImage) {
                if (hasSubject) {
                    LogUtil.i("MultimediaFragment.onCreateView", "show text, image, location layout");
                    return layoutInflater.inflate(
                            R.layout.fragment_composer_text_image_frag, viewGroup, false);
                } else {
                    LogUtil.i("MultimediaFragment.onCreateView", "show image, location layout");
                    return layoutInflater.inflate(R.layout.fragment_composer_image_frag, viewGroup, false);
                }
            } else if (hasSubject) {
                LogUtil.i("MultimediaFragment.onCreateView", "show text, location layout");
                return layoutInflater.inflate(R.layout.fragment_composer_text_frag, viewGroup, false);
            } else {
                LogUtil.i("MultimediaFragment.onCreateView", "show location layout");
                return layoutInflater.inflate(R.layout.fragment_composer_frag, viewGroup, false);
            }
        } else if (hasImage) {
            if (hasSubject) {
                LogUtil.i("MultimediaFragment.onCreateView", "show text, image layout");
                return layoutInflater.inflate(R.layout.fragment_composer_text_image, viewGroup, false);
            } else {
                LogUtil.i("MultimediaFragment.onCreateView", "show image layout");
                return layoutInflater.inflate(R.layout.fragment_composer_image, viewGroup, false);
            }
        } else {
            LogUtil.i("MultimediaFragment.onCreateView", "show text layout");
            return layoutInflater.inflate(R.layout.fragment_composer_text, viewGroup, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        View container = view.findViewById(R.id.answer_message_container);
        if (container != null) {
            container.setClipToOutline(true);
        }

        // If the call is spam and only has a subject, update the view to reflect that.
        if (isSpam
                && getLocation() == null
                && getImageUri() == null
                && !TextUtils.isEmpty(getSubject())) {
            ((ImageView) view.findViewById(R.id.spam_image))
                    .setImageResource(R.drawable.quantum_ic_message_white_24);
            ((TextView) view.findViewById(R.id.spam_text)).setText(R.string.spam_message_text);
        }

        TextView messageText = view.findViewById(R.id.answer_message_text);
        if (messageText != null) {
            messageText.setText(getSubject());
        }
        ImageView mainImage = view.findViewById(R.id.answer_message_image);
        if (mainImage != null) {
            Glide.with(this)
                    .load(getImageUri())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(
                            new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(
                                        @Nullable GlideException e,
                                        Object model,
                                        Target<Drawable> target,
                                        boolean isFirstResource) {
                                    view.findViewById(R.id.loading_spinner).setVisibility(View.GONE);
                                    LogUtil.e("MultimediaFragment.onLoadFailed", null, e);
                                    // TODO(a bug) handle error cases nicely
                                    return false; // Let Glide handle the rest
                                }

                                @Override
                                public boolean onResourceReady(
                                        Drawable drawable,
                                        Object model,
                                        Target<Drawable> target,
                                        DataSource dataSource,
                                        boolean isFirstResource) {
                                    LogUtil.enterBlock("MultimediaFragment.onResourceReady");
                                    view.findViewById(R.id.loading_spinner).setVisibility(View.GONE);
                                    return false;
                                }
                            })
                    .into(mainImage);
            mainImage.setClipToOutline(true);
        }
        FrameLayout fragmentHolder = view.findViewById(R.id.answer_message_frag);
        if (fragmentHolder != null) {
            fragmentHolder.setClipToOutline(true);
            Fragment mapFragment =
                    MapsComponent.get(getContext()).getMaps().createStaticMapFragment(getLocation());
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.answer_message_frag, mapFragment)
                    .commitNow();
        }
        avatarImageView = view.findViewById(R.id.answer_message_avatar);
        if (avatarImageView != null) {
            avatarImageView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        }

        Holder parent = FragmentUtils.getParent(this, Holder.class);
        if (parent != null) {
            parent.updateAvatar(this);
        }
    }

    @Nullable
    @Override
    public ImageView getAvatarImageView() {
        return avatarImageView;
    }

    @Override
    public int getAvatarSize() {
        return getResources().getDimensionPixelSize(R.dimen.answer_message_avatar_size);
    }

    @Override
    public boolean shouldShowAnonymousAvatar() {
        return showAvatar;
    }

    @Nullable
    public String getSubject() {
        return getArguments().getString(ARG_SUBJECT);
    }

    @Nullable
    public Uri getImageUri() {
        return getArguments().getParcelable(ARG_IMAGE);
    }

    @Nullable
    public Location getLocation() {
        return getArguments().getParcelable(ARG_LOCATION);
    }

    /**
     * Interface for notifying the fragment parent of changes.
     */
    public interface Holder {
        void updateAvatar(AvatarPresenter sessionDataScreen);
    }
}
