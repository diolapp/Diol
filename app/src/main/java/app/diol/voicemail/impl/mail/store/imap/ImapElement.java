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

package app.diol.voicemail.impl.mail.store.imap;

/**
 * Class representing "element"s in IMAP responses.
 *
 * <p>
 * Class hierarchy:
 *
 * <pre>
 * ImapElement
 *   |
 *   |-- ImapElement.NONE (for 'index out of range')
 *   |
 *   |-- ImapList (isList() == true)
 *   |   |
 *   |   |-- ImapList.EMPTY
 *   |   |
 *   |   --- ImapResponse
 *   |
 *   --- ImapString (isString() == true)
 *       |
 *       |-- ImapString.EMPTY
 *       |
 *       |-- ImapSimpleString
 *       |
 *       |-- ImapMemoryLiteral
 *       |
 *       --- ImapTempFileLiteral
 * </pre>
 */
public abstract class ImapElement {
    /**
     * An element that is returned by {@link ImapList#getElementOrNone} to indicate
     * an index is out of range.
     */
    public static final ImapElement NONE = new ImapElement() {
        @Override
        public void destroy() {
            // Don't call super.destroy().
            // It's a shared object. We don't want the mDestroyed to be set on this.
        }

        @Override
        public boolean isList() {
            return false;
        }

        @Override
        public boolean isString() {
            return false;
        }

        @Override
        public String toString() {
            return "[NO ELEMENT]";
        }

        @Override
        public boolean equalsForTest(ImapElement that) {
            return super.equalsForTest(that);
        }
    };

    private boolean destroyed = false;

    public abstract boolean isList();

    public abstract boolean isString();

    protected boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Clean up the resources used by the instance. It's for removing a temp file
     * used by {@link ImapTempFileLiteral}.
     */
    public void destroy() {
        destroyed = true;
    }

    /**
     * Throws {@link RuntimeException} if it's already destroyed.
     */
    protected final void checkNotDestroyed() {
        if (destroyed) {
            throw new RuntimeException("Already destroyed");
        }
    }

    /**
     * Return a string that represents this object; it's purely for the debug
     * purpose. Don't mistake it for {@link ImapString#getString}.
     *
     * <p>
     * Abstract to force subclasses to implement it.
     */
    @Override
    public abstract String toString();

    /**
     * The equals implementation that is intended to be used only for unit testing.
     * (Because it may be heavy and has a special sense of "equal" for testing.)
     */
    public boolean equalsForTest(ImapElement that) {
        if (that == null) {
            return false;
        }
        return this.getClass() == that.getClass(); // Has to be the same class.
    }
}
