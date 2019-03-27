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
package app.diol.voicemail.impl.mail;

import java.util.Comparator;

public class MessageDateComparator implements Comparator<Message> {
    @Override
    public int compare(Message o1, Message o2) {
        try {
            if (o1.getSentDate() == null) {
                return 1;
            } else if (o2.getSentDate() == null) {
                return -1;
            } else {
                return o2.getSentDate().compareTo(o1.getSentDate());
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
