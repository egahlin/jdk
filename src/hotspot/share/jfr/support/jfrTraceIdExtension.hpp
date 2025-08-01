/*
* Copyright (c) 2012, 2025, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_JFR_SUPPORT_JFRTRACEIDEXTENSION_HPP
#define SHARE_JFR_SUPPORT_JFRTRACEIDEXTENSION_HPP

#include "jfr/recorder/checkpoint/types/traceid/jfrTraceId.hpp"
#include "utilities/macros.hpp"

#define DEFINE_TRACE_ID_FIELD mutable traceid _trace_id

#define DEFINE_TRACE_ID_METHODS \
  traceid trace_id() const { return _trace_id; } \
  traceid* trace_id_addr() const { return &_trace_id; } \
  void set_trace_id(traceid id) const { _trace_id = id; }

#define DEFINE_TRACE_ID_SIZE \
  static size_t trace_id_size() { return sizeof(traceid); }

#define INIT_ID(data) JfrTraceId::assign(data)
#define ASSIGN_PRIMITIVE_CLASS_ID(data) JfrTraceId::assign_primitive_klass_id()
#define REMOVE_ID(k) JfrTraceId::remove(k);
#define REMOVE_METHOD_ID(method) JfrTraceId::remove(method);
#define RESTORE_ID(k) JfrTraceId::restore(k);

static constexpr const uint16_t cleared_epoch_bits = 512 | 256;

class JfrTraceFlag {
 private:
  mutable uint16_t _flags;
 public:
  JfrTraceFlag() : _flags(cleared_epoch_bits) {}
  bool is_set(uint16_t flag) const {
    return (_flags & flag) != 0;
  }

  uint16_t flags() const {
    return _flags;
  }

  void set_flags(uint16_t flags) const {
    _flags = flags;
  }

  uint8_t* flags_addr() const {
#ifdef VM_LITTLE_ENDIAN
    return reinterpret_cast<uint8_t*>(&_flags);
#else
    return reinterpret_cast<uint8_t*>(&_flags) + 1;
#endif
  }

  uint8_t* meta_addr() const {
#ifdef VM_LITTLE_ENDIAN
    return reinterpret_cast<uint8_t*>(&_flags) + 1;
#else
    return reinterpret_cast<uint8_t*>(&_flags);
#endif
  }
};

#define DEFINE_TRACE_FLAG mutable JfrTraceFlag _trace_flags

#define DEFINE_TRACE_FLAG_ACCESSOR                 \
  bool is_trace_flag_set(uint16_t flag) const {    \
    return _trace_flags.is_set(flag);              \
  }                                                \
  uint16_t trace_flags() const {                   \
    return _trace_flags.flags();                   \
  }                                                \
  void set_trace_flags(uint16_t flags) const {     \
    _trace_flags.set_flags(flags);                 \
  }                                                \
  uint8_t* trace_flags_addr() const {              \
    return _trace_flags.flags_addr();              \
  }                                                \
  uint8_t* trace_flags_meta_addr() const {         \
    return _trace_flags.meta_addr();               \
  }                                                \
  void copy_trace_flags(const Method* rhm) const { \
    assert(rhm != nullptr, "invariant");           \
    set_trace_flags(rhm->trace_flags());           \
    assert(trace_flags()==rhm->trace_flags(), ""); \
  }

#endif // SHARE_JFR_SUPPORT_JFRTRACEIDEXTENSION_HPP
