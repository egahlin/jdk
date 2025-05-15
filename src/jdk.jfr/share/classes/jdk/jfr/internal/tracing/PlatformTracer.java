/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
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
package jdk.jfr.internal.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jdk.jfr.events.MethodTimingEvent;
import jdk.jfr.internal.JVM;
import jdk.jfr.internal.LogLevel;
import jdk.jfr.internal.LogTag;
import jdk.jfr.internal.Logger;
import jdk.jfr.internal.MetadataRepository;
import jdk.jfr.internal.SecuritySupport;
import jdk.jfr.internal.Type;
import jdk.jfr.internal.util.Bytecode;
import jdk.jfr.tracing.MethodTracer;

/**
 * Class that contains the Method Tracer implementation.
 * <p>
 * By placing the implementation in jdk.jfr.internal.tracing package instead of
 * the jdk.jfr.tracing package fewer internals are exposed to the application.
 */
public final class PlatformTracer {
    private static final ConcurrentHashMap<Long, TimedMethod> timedMethods = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, TimedClass> timedClasses = new ConcurrentHashMap<>();

    private static List<Filter> traceFilters = List.of();
    private static List<Filter> timingFilters = List.of();
    private static TimedMethod OBJECT;

    public static byte[] onMethodTrace(Module module, ClassLoader classLoader, String className,
                                       byte[] oldBytecode, long[] ids, String[] names, String[] signatures,
                                       int[] modifications) {
        if (classLoader == null && ExcludeList.containsClass(className)) {
            log(LogLevel.DEBUG, "Risk of recursion, skipping bytecode generation", module, className);
            return null;
        }
        try {
            Instrumentation instrumentation = new Instrumentation(module, classLoader, className, oldBytecode);
            for (int i = 0; i < ids.length; i++) {
                instrumentation.addMethod(ids[i], names[i], signatures[i], modifications[i]);
            }
            byte[] bytecode = instrumentation.generateBytecode();
            for (Method method : instrumentation.getMethods()) {
                updateTiming(method);
            }
            return bytecode; // Returns null if bytecode was not modified.
        } catch (ClassCircularityError cce) {
            log(LogLevel.WARN, "Class circularity error, skipping instrumentation", module, className);
            return null;
        } catch (Throwable t) {
            log(LogLevel.WARN, "Unexpected error " + t.getMessage() + ". Skipping instrumentation", module, className);
            return null;
        }
    }

    private static void updateTiming(Method method) {
        boolean isTiming = method.modification().timing();
        Long classId = method.classId();

        if (!timedMethods.containsKey(method.methodId())) {
            if (isTiming) {
                // Timing started
                TimedClass timedClass = timedClasses.computeIfAbsent(classId, id -> new TimedClass());
                TimedMethod entry = timedClass.add(method);
                timedMethods.put(method.methodId(), entry);
                if ("java.lang.Object::<init>".equals(method.name())) {
                    OBJECT = entry;
                }
                method.log("Timing entry added");
            }
            return;
        }
        if (!isTiming) {
            TimedClass timedClass = timedClasses.get(classId);
            if (timedClass != null) {
                timedClass.remove(method);
            }
            timedMethods.remove(method.methodId());
            method.log("Timing entry removed");
        }
    }

    private static void log(LogLevel level, String message, Module module, String className) {
        if (!Logger.shouldLog(LogTag.JFR_METHODTRACE, level)) {
            return;
        }
        StringBuilder s = new StringBuilder();
        s.append(message);
        s.append(" for ");
        s.append(className.replace("/", "."));
        s.append(" in module ");
        s.append(module.getName());
        s.append(" and class loader " + module.getClassLoader());
        Logger.log(LogTag.JFR_METHODTRACE, level, s.toString());
    }

    public static void emitTiming() {
        // Metadata lock prevents rotation while emitting events.
        synchronized (MetadataRepository.getInstance()) {
            removeClasses(JVM.drainStaleMethodTracerIds());
            long timestamp = MethodTimingEvent.timestamp();
            for (var tc : timedClasses.values()) {
                tc.emit(timestamp);
            }
        }
    }

    public static void addObjectTiming(long duration) {
        OBJECT.invocations().getAndIncrement();
        OBJECT.time().addAndGet(duration);
    }

    public static void addTiming(long id, long duration) {
        TimedMethod entry = timedMethods.get(id);
        if (entry != null) {
            entry.invocations().getAndIncrement();
            entry.time().addAndGet(duration);
        }
    }

    public static boolean isValidFilter(String text) {
        return Filter.of(text, null) != null;
    }

    public static void setFilters(Modification modification, List<String> filters) {
        publishClasses(applyFilter(modification, filters));
    }

    private static long[] applyFilter(Modification modification, List<String> filters) {
        boolean hadFilters = hasFilters();
        if (modification.tracing()) {
            traceFilters = makeFilters(filters, modification);
        }
        if (modification.timing()) {
            timingFilters = makeFilters(filters, modification);
        }
        if (hadFilters || hasFilters()) {
            int size = filterCount();
            List<Filter> allFilters = new ArrayList<>(size);
            allFilters.addAll(traceFilters);
            allFilters.addAll(timingFilters);
            String[] classes = new String[size];
            String[] methods = new String[size];
            String[] annotations = new String[size];
            int[] modifications = new int[size];
            for (int index = 0; index < size; index++) {
                Filter filter = allFilters.get(index);
                classes[index] = Bytecode.internalName(filter.className());
                methods[index] = filter.methodName();
                annotations[index] = Bytecode.descriptorName(filter.annotationName());
                modifications[index] = filter.modification().toInt();
            }
            return JVM.setMethodTraceFilters(classes, methods, annotations, modifications);
        }
        return null;
    }

    private static void removeClasses(long[] classIds) {
        if (classIds == null) {
            return;
        }
        for (int i = 0; i < classIds.length; i++) {
            TimedClass timedClass = timedClasses.remove(classIds[i]);
            if (timedClass != null) {
                for (TimedMethod tm : timedClass.methods()) {
                    timedMethods.remove(tm.method().methodId());
                    tm.method().log("Timing entry unloaded");
                }
                if (Logger.shouldLog(LogTag.JFR_METHODTRACE, LogLevel.INFO)) {
                    Logger.log(LogTag.JFR_METHODTRACE, LogLevel.INFO, "Timing entry unloaded for class " + classIds[i]);
                }
            }
        }
    }

    private static void publishClasses(long[] classIds) {
        if (classIds == null) {
            return;
        }
        for (int i = 0; i < classIds.length; i++) {
            publishClass(classIds[i]);
        }
    }

    public static void publishClass(long classId) {
        TimedClass timedClass = timedClasses.get(classId);
        // The class may be null if a class is drained/unloaded before
        // it is being published by setFilter().
        if (timedClass != null) {
            timedClass.publish();
        }
    }

    private static boolean hasFilters() {
        return filterCount() > 0;
    }

    private static int filterCount() {
        return traceFilters.size() + timingFilters.size();
    }

    private static List<Filter> makeFilters(List<String> filterTexts, Modification modification) {
        List<Filter> filters = new ArrayList<>(filterTexts.size());
        for (String filterText : filterTexts) {
            Filter filter = Filter.of(filterText, modification);
            if (filter != null) {
                filters.add(filter);
            }
        }
        return filters;
    }

    private synchronized static void reset() {
        timedMethods.clear();
        timedClasses.clear();
    }

    // This method has three purposes:
    //
    // 1) Load classes before instrumentation to avoid recursion in class
    // initializers when onMethodTrace(...) is called by the JVM.
    //
    // 2) Warm up methods used by the PlatformTracer class to reduce the observer
    // effect later.
    //
    // 3) Export the jdk.jfr.tracing package to all other modules.
    //
    // This method takes 1-10 milliseconds to run and is only executed once,
    // provided a user has specified a non-empty filter for the MethodTrace or
    // MethodTiming event.
    public static void initialize() {
        try {
            Logger.log(LogTag.JFR_METHODTRACE, LogLevel.INFO, "Method tracer initialization started.");
            Thread current = Thread.currentThread();
            JVM.exclude(current);
            long classId = Type.getKnownType(String.class).getId();
            long methodId = classId << 16 + 983059;
            ClassLoader cl = null;
            String className = " java/lang/String";
            Module m = String.class.getModule();
            var is = ClassLoader.getSystemClassLoader().getResourceAsStream("java/lang/String.class");
            byte[] oldBytecode = is.readAllBytes();
            is.close();
            long[] ids = { methodId };
            String[] names = { "<clinit>" };
            String[] signatures = { "()V" };
            int[] modifications = { 3 };
            byte[] bytes = onMethodTrace(m, cl, className, oldBytecode, ids, names, signatures, modifications);
            if (bytes == null) {
                throw new Exception("Could not generate bytecode");
            }
            publishClass(classId);
            for (int id = 0; id < 25_000; id++) {
                MethodTracer.timing(MethodTracer.timestamp(), methodId);
                MethodTracer.trace(MethodTracer.timestamp(), methodId);
                MethodTracer.traceTiming(MethodTracer.timestamp(), methodId);
            }
            reset();
            JVM.include(current);
            SecuritySupport.addTracingExport();
            Logger.log(LogTag.JFR_METHODTRACE, LogLevel.INFO, "Method tracer initialization complete.");
        } catch (Exception e) {
            Logger.log(LogTag.JFR_METHODTRACE, LogLevel.WARN, "Method tracer initialization failed. " + e.getMessage());
        }
    }
}
