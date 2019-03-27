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

package app.diol.dialer.glidephotomanager;

import android.support.annotation.MainThread;
import android.widget.ImageView;
import android.widget.QuickContactBadge;

/**
 * Class to load photo for call/contacts
 */
public interface GlidePhotoManager {

    /**
     * Load {@code photoInfo} into the {@code badge}. The loading is performed in the background and a
     * placeholder will be used appropriately. {@code badge} must be already attached to an
     * activity/fragment, and the load will be automatically canceled if the lifecycle of the activity
     * ends.
     */
    @MainThread
    void loadQuickContactBadge(QuickContactBadge badge, PhotoInfo photoInfo);

    /**
     * Load {@code photoInfo} into the {@code imageView}. The loading is performed in the background
     * and a placeholder will be used appropriately. {@code imageView} must be already attached to an
     * activity/fragment, and the load will be automatically canceled if the lifecycle of the activity
     * ends.
     */
    @MainThread
    void loadContactPhoto(ImageView imageView, PhotoInfo photoInfo);
}
