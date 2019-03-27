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

package app.diol.voicemail;

import android.support.annotation.IntDef;
import android.support.annotation.WorkerThread;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface to change the PIN used to access the mailbox by calling.
 */
public interface PinChanger {

    int CHANGE_PIN_SUCCESS = 0;
    int CHANGE_PIN_TOO_SHORT = 1;
    int CHANGE_PIN_TOO_LONG = 2;
    int CHANGE_PIN_TOO_WEAK = 3;
    int CHANGE_PIN_MISMATCH = 4;
    int CHANGE_PIN_INVALID_CHARACTER = 5;
    int CHANGE_PIN_SYSTEM_ERROR = 6;

    @WorkerThread
    @ChangePinResult
    int changePin(String oldPin, String newPin);

    String getScrambledPin();

    /**
     * Set the scrambled PIN if it is auto generated during provisioning. Set to {@code null} to
     * clear.
     */
    void setScrambledPin(String pin);

    PinSpecification getPinSpecification();

    /**
     * Results from {@link #changePin(String, String)}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            value = {
                    CHANGE_PIN_SUCCESS,
                    CHANGE_PIN_TOO_SHORT,
                    CHANGE_PIN_TOO_LONG,
                    CHANGE_PIN_TOO_WEAK,
                    CHANGE_PIN_MISMATCH,
                    CHANGE_PIN_INVALID_CHARACTER,
                    CHANGE_PIN_SYSTEM_ERROR
            }
    )
    @interface ChangePinResult {
    }

    /**
     * Format requirements for the PIN.
     */
    class PinSpecification {
        public int minLength;
        public int maxLength;
    }
}
