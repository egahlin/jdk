/*
 * Copyright (c) 2018, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

package jdk.jfr.jvm;

import static jdk.test.lib.Asserts.assertEquals;

import jdk.jfr.internal.JVM;

/**
 * @test TestPid
 * @requires vm.flagless
 * @requires vm.hasJFR
 * @library /test/lib
 * @modules jdk.jfr/jdk.jfr.internal
 * @run main/othervm jdk.jfr.jvm.TestPid
 */
public class TestPid {

    public static void main(String... args) throws InterruptedException {

        String pid = JVM.getPid();

        try {
            String managementPid = String.valueOf(ProcessHandle.current().pid());
            assertEquals(pid, managementPid, "Pid doesn't match value returned by RuntimeMXBean");
        } catch (NumberFormatException nfe) {
            throw new AssertionError("Pid must be numeric, but was '" + pid + "'");
        }
    }

}
