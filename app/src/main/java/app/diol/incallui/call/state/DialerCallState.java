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

package app.diol.incallui.call.state;

/**
 * Defines different states of {@link app.diol.incallui.call.DialerCall}
 */
public class DialerCallState {

    public static final int INVALID = 0;
    public static final int NEW = 1; /* The call is new. */
    public static final int IDLE = 2; /* The call is idle.  Nothing active */
    public static final int ACTIVE = 3; /* There is an active call */
    public static final int INCOMING = 4; /* A normal incoming phone call */
    public static final int CALL_WAITING = 5; /* Incoming call while another is active */
    public static final int DIALING = 6; /* An outgoing call during dial phase */
    public static final int REDIALING = 7; /* Subsequent dialing attempt after a failure */
    public static final int ONHOLD = 8; /* An active phone call placed on hold */
    public static final int DISCONNECTING = 9; /* A call is being ended. */
    public static final int DISCONNECTED = 10; /* State after a call disconnects */
    public static final int CONFERENCED = 11; /* DialerCall part of a conference call */
    public static final int SELECT_PHONE_ACCOUNT = 12; /* Waiting for account selection */
    public static final int CONNECTING = 13; /* Waiting for Telecom broadcast to finish */
    public static final int BLOCKED = 14; /* The number was found on the block list */
    public static final int PULLING = 15; /* An external call being pulled to the device */
    public static final int CALL_PENDING = 16; /* A call is pending on a long process to finish */

    public static boolean isConnectingOrConnected(int state) {
        switch (state) {
            case ACTIVE:
            case INCOMING:
            case CALL_WAITING:
            case CONNECTING:
            case DIALING:
            case PULLING:
            case REDIALING:
            case ONHOLD:
            case CONFERENCED:
                return true;
            default:
                return false;
        }
    }

    public static boolean isDialing(int state) {
        return state == DIALING || state == PULLING || state == REDIALING;
    }

    public static String toString(int state) {
        switch (state) {
            case INVALID:
                return "INVALID";
            case NEW:
                return "NEW";
            case IDLE:
                return "IDLE";
            case ACTIVE:
                return "ACTIVE";
            case INCOMING:
                return "INCOMING";
            case CALL_WAITING:
                return "CALL_WAITING";
            case DIALING:
                return "DIALING";
            case PULLING:
                return "PULLING";
            case REDIALING:
                return "REDIALING";
            case ONHOLD:
                return "ONHOLD";
            case DISCONNECTING:
                return "DISCONNECTING";
            case DISCONNECTED:
                return "DISCONNECTED";
            case CONFERENCED:
                return "CONFERENCED";
            case SELECT_PHONE_ACCOUNT:
                return "SELECT_PHONE_ACCOUNT";
            case CONNECTING:
                return "CONNECTING";
            case BLOCKED:
                return "BLOCKED";
            default:
                return "UNKNOWN";
        }
    }
}
