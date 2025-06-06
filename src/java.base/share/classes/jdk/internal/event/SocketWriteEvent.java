/*
 * Copyright (c) 2023, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.event;


import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;

/**
 * A JFR event for socket write operations.  This event is mirrored in
 * {@code jdk.jfr.events.SocketWriteEvent } where the metadata for the event is
 * provided with annotations.  Some of the methods are replaced by generated
 * methods when jfr is enabled.  Note that the order of the arguments of the
 * {@link #commit(long, long, String, String, int, long)} method
 * must be the same as the order of the fields.
 */
public class SocketWriteEvent extends Event {

    // THE ORDER OF THE FOLLOWING FIELDS IS IMPORTANT!
    // The order must match the argument order of the generated commit method.
    public String host;
    public String address;
    public int port;
    public long bytesWritten;

    /**
     * Actually commit a socket write event.  This is generated automatically.
     * The order of the fields must be the same as the parameters in this method.
     * {@code commit(..., String, String, int, long)}
     *
     * @param start     timestamp of the start of the operation
     * @param duration  time in nanoseconds to complete the operation
     * @param host      remote host of the transfer
     * @param address   remote address of the transfer
     * @param port      remote port of the transfer
     * @param bytes     number of bytes that were transferred
     */
    public static void commit(long start, long duration, String host, String address, int port, long bytes) {
        // Generated by JFR
    }

    /**
     * Determine if an event should be emitted.  The duration of the operation
     * must exceed some threshold in order to commit the event.  The implementation
     * of this method is generated automatically if jfr is enabled.
     *
     * @param duration  time to complete the operation
     * @param end  timestamp at the end of the operation
     * @return  true if the event should be commited
     */
    public static boolean shouldThrottleCommit(long duration, long end) {
        // Generated by JFR
        return false;
    }

    /**
     * Determine if this kind of event is enabled.  The implementation
     * of this method is generated automatically if jfr is enabled.
     *
     * @return true if socket write events are enabled, false otherwise
     */
    public static boolean enabled() {
        // Generated by JFR
        return false;
    }

    /**
     * Fetch the current timestamp in nanoseconds.  This method is used
     * to determine the start and end of an operation.  The implementation
     * of this method is generated automatically if jfr is enabled.
     *
     * @return  the current timestamp value
     */
    public static long timestamp() {
        // Generated by JFR
        return 0L;
    }

    /**
     * Helper method to offer the data needed to potentially commit an event.
     * The duration of the operation is computed using the current
     * timestamp and the given start time.  If the duration is meets
     * or exceeds the configured value (determined by calling the generated method
     * {@link #shouldThrottleCommit(long)}), an event will be emitted by calling
     * {@link #emit(long, long, long, SocketAddress)}.
     *
     * @param start  the start time
     * @param bytesWritten  how many bytes were sent
     * @param remote  the address of the remote socket being written to
     */
    public static void offer(long start, long bytesWritten, SocketAddress remote) {
        long end = timestamp();
        long duration = end - start;
        if (shouldThrottleCommit(duration, end)) {
            emit(start, duration, bytesWritten, remote);
        }
    }

    /**
     * Helper method to perform a common task of getting event data ready and
     * then emitting the event by calling
     * {@link #commit(long, long, String, String, int, long)}.
     *
     * @param start  the start time
     * @param duration the duration
     * @param bytesWritten  how many bytes were sent
     * @param remote  the address of the remote socket being written to
     */
    public static void emit(long start, long duration, long bytesWritten, SocketAddress remote) {
        long bytes = bytesWritten < 0 ? 0 : bytesWritten;
        if (remote instanceof InetSocketAddress isa) {
            commit(start, duration, isa.getHostString(), isa.getAddress().getHostAddress(), isa.getPort(), bytes);
        } else if (remote instanceof UnixDomainSocketAddress udsa) {
            String path = "[" + udsa.getPath().toString() + "]";
            commit(start, duration, "Unix domain socket", path, 0, bytes);
        }
    }
}
