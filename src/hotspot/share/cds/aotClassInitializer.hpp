/*
 * Copyright (c) 2024, 2025, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#ifndef SHARE_CDS_AOTCLASSINITIALIZER_HPP
#define SHARE_CDS_AOTCLASSINITIALIZER_HPP

#include "memory/allStatic.hpp"
#include "utilities/exceptions.hpp"

class InstanceKlass;

class AOTClassInitializer : AllStatic {
public:
  // Called by heapShared.cpp to see if src_ik->java_mirror() can be archived in
  // the initialized state.
  static bool can_archive_initialized_mirror(InstanceKlass* src_ik);

  static void call_runtime_setup(JavaThread* current, InstanceKlass* ik);

  // Support for regression testing. Available in debug builds only.
  static void init_test_class(TRAPS) NOT_DEBUG_RETURN;
  static bool has_test_class() NOT_DEBUG({ return false; });
};

#endif // SHARE_CDS_AOTCLASSINITIALIZER_HPP
