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
package app.diol.voicemail.impl;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;
import java.util.Iterator;

import app.diol.dialer.common.LogUtil;
import app.diol.dialer.persistentlog.PersistentLogger;
import app.diol.voicemail.impl.utils.IndentingPrintWriter;

/**
 * Helper methods for adding to OMTP visual voicemail local logs.
 */
public class VvmLog {

    private static final int MAX_OMTP_VVM_LOGS = 100;

    private static final LocalLog localLog = new LocalLog(MAX_OMTP_VVM_LOGS);

    public static void log(String tag, String log) {
        PersistentLogger.logText(tag, log);
    }

    public static void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(printwriter, "  ");
        indentingPrintWriter.increaseIndent();
        localLog.dump(fd, indentingPrintWriter, args);
        indentingPrintWriter.decreaseIndent();
    }

    public static void e(String tag, String log) {
        log(tag, log);
        LogUtil.e(tag, log);
    }

    public static void e(String tag, String log, Throwable e) {
        log(tag, log + " " + e);
        LogUtil.e(tag, log, e);
    }

    public static void w(String tag, String log) {
        log(tag, log);
        LogUtil.w(tag, log);
    }

    public static void w(String tag, String log, Throwable e) {
        log(tag, log + " " + e);
        LogUtil.w(tag, log, e);
    }

    public static void i(String tag, String log) {
        log(tag, log);
        LogUtil.i(tag, log);
    }

    public static void i(String tag, String log, Throwable e) {
        log(tag, log + " " + e);
        LogUtil.i(tag, log, e);
    }

    public static void d(String tag, String log) {
        log(tag, log);
        LogUtil.d(tag, log);
    }

    public static void d(String tag, String log, Throwable e) {
        log(tag, log + " " + e);
        LogUtil.d(tag, log, e);
    }

    public static void v(String tag, String log) {
        log(tag, log);
        LogUtil.v(tag, log);
    }

    public static void v(String tag, String log, Throwable e) {
        log(tag, log + " " + e);
        LogUtil.v(tag, log, e);
    }

    public static void wtf(String tag, String log) {
        log(tag, log);
        LogUtil.e(tag, log);
    }

    public static void wtf(String tag, String log, Throwable e) {
        log(tag, log + " " + e);
        LogUtil.e(tag, log, e);
    }

    /**
     * Redact personally identifiable information for production users. If we are
     * running in verbose mode, return the original string, otherwise return a SHA-1
     * hash of the input string.
     */
    public static String pii(Object pii) {
        if (pii == null) {
            return String.valueOf(pii);
        }
        return "[PII]";
    }

    public static class LocalLog {

        private final Deque<String> log;
        private final int maxLines;

        public LocalLog(int maxLines) {
            this.maxLines = Math.max(0, maxLines);
            log = new ArrayDeque<>(this.maxLines);
        }

        public void log(String msg) {
            if (maxLines <= 0) {
                return;
            }
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            append(String.format("%tm-%td %tH:%tM:%tS.%tL - %s", c, c, c, c, c, c, msg));
        }

        private synchronized void append(String logLine) {
            while (log.size() >= maxLines) {
                log.remove();
            }
            log.add(logLine);
        }

        public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            Iterator<String> itr = log.iterator();
            while (itr.hasNext()) {
                pw.println(itr.next());
            }
        }

        public synchronized void reverseDump(FileDescriptor fd, PrintWriter pw, String[] args) {
            Iterator<String> itr = log.descendingIterator();
            while (itr.hasNext()) {
                pw.println(itr.next());
            }
        }

        public ReadOnlyLocalLog readOnlyLocalLog() {
            return new ReadOnlyLocalLog(this);
        }

        public static class ReadOnlyLocalLog {

            private final LocalLog log;

            ReadOnlyLocalLog(LocalLog log) {
                this.log = log;
            }

            public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                log.dump(fd, pw, args);
            }

            public void reverseDump(FileDescriptor fd, PrintWriter pw, String[] args) {
                log.reverseDump(fd, pw, args);
            }
        }
    }
}
