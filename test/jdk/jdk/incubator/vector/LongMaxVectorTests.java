/*
 * Copyright (c) 2018, 2024, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @key randomness
 *
 * @library /test/lib
 * @modules jdk.incubator.vector
 * @run testng/othervm/timeout=300 -ea -esa -Xbatch -XX:-TieredCompilation LongMaxVectorTests
 */

// -- This file was mechanically generated: Do not edit! -- //

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.Vector;
import jdk.incubator.vector.VectorMath;

import jdk.incubator.vector.LongVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.Integer;
import java.util.List;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class LongMaxVectorTests extends AbstractVectorTest {

    static final VectorSpecies<Long> SPECIES =
                LongVector.SPECIES_MAX;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);

    static VectorShape getMaxBit() {
        return VectorShape.S_Max_BIT;
    }

    private static final int Max = 256;  // juts so we can do N/Max

    private static final long CONST_SHIFT = Long.SIZE / 2;

    static final int BUFFER_REPS = Integer.getInteger("jdk.incubator.vector.test.buffer-vectors", 25000 / Max);

    static void assertArraysStrictlyEquals(long[] r, long[] a) {
        for (int i = 0; i < a.length; i++) {
            if (r[i] != a[i]) {
                Assert.fail("at index #" + i + ", expected = " + a[i] + ", actual = " + r[i]);
            }
        }
    }

    interface FUnOp {
        long apply(long a);
    }

    static void assertArraysEquals(long[] r, long[] a, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i]), "at index #" + i + ", input = " + a[i]);
        }
    }

    interface FUnArrayOp {
        long[] apply(long a);
    }

    static void assertArraysEquals(long[] r, long[] a, FUnArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a[i]));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a[i]);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, boolean[] mask, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i], "at index #" + i + ", input = " + a[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    interface FReductionOp {
        long apply(long[] a, int idx);
    }

    interface FReductionAllOp {
        long apply(long[] a);
    }

    static void assertReductionArraysEquals(long[] r, long rc, long[] a,
                                            FReductionOp f, FReductionAllOp fa) {
        int i = 0;
        try {
            Assert.assertEquals(rc, fa.apply(a));
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(rc, fa.apply(a), "Final result is incorrect!");
            Assert.assertEquals(r[i], f.apply(a, i), "at index #" + i);
        }
    }

    interface FReductionMaskedOp {
        long apply(long[] a, int idx, boolean[] mask);
    }

    interface FReductionAllMaskedOp {
        long apply(long[] a, boolean[] mask);
    }

    static void assertReductionArraysEqualsMasked(long[] r, long rc, long[] a, boolean[] mask,
                                            FReductionMaskedOp f, FReductionAllMaskedOp fa) {
        int i = 0;
        try {
            Assert.assertEquals(rc, fa.apply(a, mask));
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(r[i], f.apply(a, i, mask));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(rc, fa.apply(a, mask), "Final result is incorrect!");
            Assert.assertEquals(r[i], f.apply(a, i, mask), "at index #" + i);
        }
    }


    interface FBoolReductionOp {
        boolean apply(boolean[] a, int idx);
    }

    static void assertReductionBoolArraysEquals(boolean[] r, boolean[] a, FBoolReductionOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a, i), "at index #" + i);
        }
    }

    interface FMaskReductionOp {
        int apply(boolean[] a, int idx);
    }

    static void assertMaskReductionArraysEquals(int[] r, boolean[] a, FMaskReductionOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a, i), "at index #" + i);
        }
    }

    static void assertRearrangeArraysEquals(long[] r, long[] a, int[] order, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    Assert.assertEquals(r[i+j], a[i+order[i+j]]);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            Assert.assertEquals(r[i+j], a[i+order[i+j]], "at index #" + idx + ", input = " + a[i+order[i+j]]);
        }
    }

    static void assertcompressArraysEquals(long[] r, long[] a, boolean[] m, int vector_len) {
        int i = 0, j = 0, k = 0;
        try {
            for (; i < a.length; i += vector_len) {
                k = 0;
                for (j = 0; j < vector_len; j++) {
                    if (m[(i + j) % SPECIES.length()]) {
                        Assert.assertEquals(r[i + k], a[i + j]);
                        k++;
                    }
                }
                for (; k < vector_len; k++) {
                    Assert.assertEquals(r[i + k], (long)0);
                }
            }
        } catch (AssertionError e) {
            int idx = i + k;
            if (m[(i + j) % SPECIES.length()]) {
                Assert.assertEquals(r[idx], a[i + j], "at index #" + idx);
            } else {
                Assert.assertEquals(r[idx], (long)0, "at index #" + idx);
            }
        }
    }

    static void assertexpandArraysEquals(long[] r, long[] a, boolean[] m, int vector_len) {
        int i = 0, j = 0, k = 0;
        try {
            for (; i < a.length; i += vector_len) {
                k = 0;
                for (j = 0; j < vector_len; j++) {
                    if (m[(i + j) % SPECIES.length()]) {
                        Assert.assertEquals(r[i + j], a[i + k]);
                        k++;
                    } else {
                        Assert.assertEquals(r[i + j], (long)0);
                    }
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            if (m[idx % SPECIES.length()]) {
                Assert.assertEquals(r[idx], a[i + k], "at index #" + idx);
            } else {
                Assert.assertEquals(r[idx], (long)0, "at index #" + idx);
            }
        }
    }

    static void assertSelectFromTwoVectorEquals(long[] r, long[] order, long[] a, long[] b, int vector_len) {
        int i = 0, j = 0;
        boolean is_exceptional_idx = false;
        int idx = 0, wrapped_index = 0, oidx = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    idx = i + j;
                    wrapped_index = Math.floorMod((int)order[idx], 2 * vector_len);
                    is_exceptional_idx = wrapped_index >= vector_len;
                    oidx = is_exceptional_idx ? (wrapped_index - vector_len) : wrapped_index;
                    Assert.assertEquals(r[idx], (is_exceptional_idx ? b[i + oidx] : a[i + oidx]));
                }
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[idx], (is_exceptional_idx ? b[i + oidx] : a[i + oidx]), "at index #" + idx + ", order = " + order[idx] + ", a = " + a[i + oidx] + ", b = " + b[i + oidx]);
        }
    }

    static void assertSelectFromArraysEquals(long[] r, long[] a, long[] order, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    Assert.assertEquals(r[i+j], a[i+(int)order[i+j]]);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            Assert.assertEquals(r[i+j], a[i+(int)order[i+j]], "at index #" + idx + ", input = " + a[i+(int)order[i+j]]);
        }
    }

    static void assertRearrangeArraysEquals(long[] r, long[] a, int[] order, boolean[] mask, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    if (mask[j % SPECIES.length()])
                         Assert.assertEquals(r[i+j], a[i+order[i+j]]);
                    else
                         Assert.assertEquals(r[i+j], (long)0);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            if (mask[j % SPECIES.length()])
                Assert.assertEquals(r[i+j], a[i+order[i+j]], "at index #" + idx + ", input = " + a[i+order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
            else
                Assert.assertEquals(r[i+j], (long)0, "at index #" + idx + ", input = " + a[i+order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
        }
    }

    static void assertSelectFromArraysEquals(long[] r, long[] a, long[] order, boolean[] mask, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    if (mask[j % SPECIES.length()])
                         Assert.assertEquals(r[i+j], a[i+(int)order[i+j]]);
                    else
                         Assert.assertEquals(r[i+j], (long)0);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            if (mask[j % SPECIES.length()])
                Assert.assertEquals(r[i+j], a[i+(int)order[i+j]], "at index #" + idx + ", input = " + a[i+(int)order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
            else
                Assert.assertEquals(r[i+j], (long)0, "at index #" + idx + ", input = " + a[i+(int)order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a) {
        int i = 0;
        for (; i < a.length; i += SPECIES.length()) {
            int idx = i;
            for (int j = idx; j < (idx + SPECIES.length()); j++)
                a[j]=a[idx];
        }

        try {
            for (i = 0; i < a.length; i++) {
                Assert.assertEquals(r[i], a[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], a[i], "at index #" + i + ", input = " + a[i]);
        }
    }

    interface FBinOp {
        long apply(long a, long b);
    }

    interface FBinMaskOp {
        long apply(long a, long b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEqualsAssociative(long[] rl, long[] rr, long[] a, long[] b, long[] c, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                //Left associative
                Assert.assertEquals(rl[i], f.apply(f.apply(a[i], b[i]), c[i]));

                //Right associative
                Assert.assertEquals(rr[i], f.apply(a[i], f.apply(b[i], c[i])));

                //Results equal sanity check
                Assert.assertEquals(rl[i], rr[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(rl[i], f.apply(f.apply(a[i], b[i]), c[i]), "left associative test at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i]);
            Assert.assertEquals(rr[i], f.apply(a[i], f.apply(b[i], c[i])), "right associative test at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i]);
            Assert.assertEquals(rl[i], rr[i], "Result checks not equal at index #" + i + "leftRes = " + rl[i] + ", rightRes = " + rr[i]);
        }
    }

   static void assertArraysEqualsAssociative(long[] rl, long[] rr, long[] a, long[] b, long[] c, boolean[] mask, FBinOp f) {
       assertArraysEqualsAssociative(rl, rr, a, b, c, mask, FBinMaskOp.lift(f));
   }

    static void assertArraysEqualsAssociative(long[] rl, long[] rr, long[] a, long[] b, long[] c, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        boolean mask_bit = false;
        try {
            for (; i < a.length; i++) {
                mask_bit = mask[i % SPECIES.length()];
                //Left associative
                Assert.assertEquals(rl[i], f.apply(f.apply(a[i], b[i], mask_bit), c[i], mask_bit));

                //Right associative
                Assert.assertEquals(rr[i], f.apply(a[i], f.apply(b[i], c[i], mask_bit), mask_bit));

                //Results equal sanity check
                Assert.assertEquals(rl[i], rr[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(rl[i], f.apply(f.apply(a[i], b[i], mask_bit), c[i], mask_bit), "left associative masked test at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i] + ", mask = " + mask_bit);
            Assert.assertEquals(rr[i], f.apply(a[i], f.apply(b[i], c[i], mask_bit), mask_bit), "right associative masked test at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i] + ", mask = " + mask_bit);
            Assert.assertEquals(rl[i], rr[i], "Result checks not equal at index #" + i + "leftRes = " + rl[i] + ", rightRes = " + rr[i]);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "(" + a[i] + ", " + b[i] + ") at index #" + i);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long b, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b), "(" + a[i] + ", " + b + ") at index #" + i);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()]),
                                "(" + a[i] + ", " + b[(i / SPECIES.length()) * SPECIES.length()] + ") at index #" + i);
        }
    }

    static void assertBroadcastLongArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()])));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()])),
                                "(" + a[i] + ", " + b[(i / SPECIES.length()) * SPECIES.length()] + ") at index #" + i);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long b, boolean[] mask, FBinOp f) {
        assertArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(long[] r, long[] a, long b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b, mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b, mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertBroadcastArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] +
                                ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }

    static void assertBroadcastLongArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertBroadcastLongArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertBroadcastLongArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()]), mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()]),
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] +
                                ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }

    static void assertShiftArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]));
                }
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]), "at index #" + i + ", " + j);
        }
    }

    static void assertShiftArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertShiftArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertShiftArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j], mask[i]));
                }
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j], mask[i]), "at index #" + i + ", input1 = " + a[i+j] + ", input2 = " + b[j] + ", mask = " + mask[i]);
        }
    }

    interface FBinConstOp {
        long apply(long a);
    }

    interface FBinConstMaskOp {
        long apply(long a, boolean m);

        static FBinConstMaskOp lift(FBinConstOp f) {
            return (a, m) -> m ? f.apply(a) : a;
        }
    }

    static void assertShiftConstEquals(long[] r, long[] a, FBinConstOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j]));
                }
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j]), "at index #" + i + ", " + j);
        }
    }

    static void assertShiftConstEquals(long[] r, long[] a, boolean[] mask, FBinConstOp f) {
        assertShiftConstEquals(r, a, mask, FBinConstMaskOp.lift(f));
    }

    static void assertShiftConstEquals(long[] r, long[] a, boolean[] mask, FBinConstMaskOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], mask[i]));
                }
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], mask[i]), "at index #" + i + ", input1 = " + a[i+j] + ", mask = " + mask[i]);
        }
    }

    interface FTernOp {
        long apply(long a, long b, long c);
    }

    interface FTernMaskOp {
        long apply(long a, long b, long c, boolean m);

        static FTernMaskOp lift(FTernOp f) {
            return (a, b, c, m) -> m ? f.apply(a, b, c) : a;
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i]);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask, FTernOp f) {
        assertArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask, FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = "
              + b[i] + ", input3 = " + c[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()]), "at index #" +
                                i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " +
                                c[(i / SPECIES.length()) * SPECIES.length()]);
        }
    }

    static void assertAltBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i]), "at index #" +
                                i + ", input1 = " + a[i] + ", input2 = " +
                                b[(i / SPECIES.length()) * SPECIES.length()] + ",  input3 = " + c[i]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernOp f) {
        assertBroadcastArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()],
                                    mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()],
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " +
                                b[i] + ", input3 = " + c[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }

    static void assertAltBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernOp f) {
        assertAltBroadcastArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertAltBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i],
                                    mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i],
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] +
                                ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] +
                                ", input3 = " + c[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertDoubleBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                    c[(i / SPECIES.length()) * SPECIES.length()]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                c[(i / SPECIES.length()) * SPECIES.length()]), "at index #" + i + ", input1 = " + a[i]
                                + ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] + ", input3 = " +
                                c[(i / SPECIES.length()) * SPECIES.length()]);
        }
    }

    static void assertDoubleBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                                  FTernOp f) {
        assertDoubleBroadcastArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertDoubleBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                                  FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                    c[(i / SPECIES.length()) * SPECIES.length()], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                c[(i / SPECIES.length()) * SPECIES.length()], mask[i % SPECIES.length()]), "at index #"
                                + i + ", input1 = " + a[i] + ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] +
                                ", input3 = " + c[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }



    interface FGatherScatterOp {
        long[] apply(long[] a, int ix, int[] b, int iy);
    }

    static void assertArraysEquals(long[] r, long[] a, int[] b, FGatherScatterOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, b, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, i, b, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + " at index #" + i);
        }
    }

    interface FGatherMaskedOp {
        long[] apply(long[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    interface FScatterMaskedOp {
        long[] apply(long[] r, long[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    static void assertArraysEquals(long[] r, long[] a, int[] b, boolean[] mask, FGatherMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, i, mask, b, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + ", mask: "
              + Arrays.toString(mask)
              + " at index #" + i);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, int[] b, boolean[] mask, FScatterMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(r, a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(r, a, i, mask, b, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + ", r: "
              + Arrays.toString(Arrays.copyOfRange(r, i, i+SPECIES.length()))
              + ", mask: "
              + Arrays.toString(mask)
              + " at index #" + i);
        }
    }

    interface FLaneOp {
        long[] apply(long[] a, int origin, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, int origin, FLaneOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, origin, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, origin, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
        }
    }

    interface FLaneBop {
        long[] apply(long[] a, long[] b, int origin, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, FLaneBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin);
        }
    }

    interface FLaneMaskedBop {
        long[] apply(long[] a, long[] b, int origin, boolean[] mask, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, boolean[] mask, FLaneMaskedBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, mask, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, mask, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin);
        }
    }

    interface FLanePartBop {
        long[] apply(long[] a, long[] b, int origin, int part, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, int part, FLanePartBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, part, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, part, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin
              + ", with part #" + part);
        }
    }

    interface FLanePartMaskedBop {
        long[] apply(long[] a, long[] b, int origin, int part, boolean[] mask, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, int part, boolean[] mask, FLanePartMaskedBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, part, mask, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, part, mask, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin
              + ", with part #" + part);
        }
    }

    static int intCornerCaseValue(int i) {
        switch(i % 5) {
            case 0:
                return Integer.MAX_VALUE;
            case 1:
                return Integer.MIN_VALUE;
            case 2:
                return Integer.MIN_VALUE;
            case 3:
                return Integer.MAX_VALUE;
            default:
                return (int)0;
        }
    }

    static final List<IntFunction<long[]>> INT_LONG_GENERATORS = List.of(
            withToString("long[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(-i * 5));
            }),
            withToString("long[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i * 5));
            }),
            withToString("long[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((long)(i + 1) == 0) ? 1 : (long)(i + 1)));
            }),
            withToString("long[intCornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)intCornerCaseValue(i));
            })
    );

    static void assertArraysEquals(int[] r, long[] a, int offs) {
        int i = 0;
        try {
            for (; i < r.length; i++) {
                Assert.assertEquals(r[i], (int)(a[i+offs]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], (int)(a[i+offs]), "at index #" + i + ", input = " + a[i+offs]);
        }
    }



    static void assertArraysEquals(long[] r, long[] a, int offs) {
        int i = 0;
        try {
            for (; i < r.length; i++) {
                Assert.assertEquals(r[i], (long)(a[i+offs]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], (long)(a[i+offs]), "at index #" + i + ", input = " + a[i+offs]);
        }
    }

    static void assertArraysEquals(double[] r, long[] a, int offs) {
        int i = 0;
        try {
            for (; i < r.length; i++) {
                Assert.assertEquals(r[i], (double)(a[i+offs]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], (double)(a[i+offs]), "at index #" + i + ", input = " + a[i+offs]);
        }
    }

    static long bits(long e) {
        return  e;
    }

    static final List<IntFunction<long[]>> LONG_GENERATORS = List.of(
            withToString("long[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(-i * 5));
            }),
            withToString("long[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i * 5));
            }),
            withToString("long[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((long)(i + 1) == 0) ? 1 : (long)(i + 1)));
            }),
            withToString("long[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> cornerCaseValue(i));
            })
    );

    static final List<IntFunction<long[]>> LONG_SATURATING_GENERATORS = List.of(
            withToString("long[Long.MIN_VALUE]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(Long.MIN_VALUE));
            }),
            withToString("long[Long.MAX_VALUE]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(Long.MAX_VALUE));
            }),
            withToString("long[Long.MAX_VALUE - 100]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(Long.MAX_VALUE - 100));
            }),
            withToString("long[Long.MIN_VALUE + 100]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(Long.MIN_VALUE + 100));
            }),
            withToString("long[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(-i * 5));
            }),
            withToString("long[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i * 5));
            })
    );

    static final List<IntFunction<long[]>> LONG_SATURATING_GENERATORS_ASSOC = List.of(
            withToString("long[Long.MAX_VALUE]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(Long.MAX_VALUE));
            }),
            withToString("long[Long.MAX_VALUE - 100]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(Long.MAX_VALUE - 100));
            }),
            withToString("long[-1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(-1));
            })
    );

    // Create combinations of pairs
    // @@@ Might be sensitive to order e.g. div by 0
    static final List<List<IntFunction<long[]>>> LONG_GENERATOR_PAIRS =
        Stream.of(LONG_GENERATORS.get(0)).
                flatMap(fa -> LONG_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    static final List<List<IntFunction<long[]>>> LONG_SATURATING_GENERATOR_PAIRS =
        Stream.of(LONG_GENERATORS.get(0)).
                flatMap(fa -> LONG_SATURATING_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    static final List<List<IntFunction<long[]>>> LONG_SATURATING_GENERATOR_TRIPLETS =
            Stream.of(LONG_GENERATORS.get(1))
                    .flatMap(fa -> LONG_SATURATING_GENERATORS_ASSOC.stream().map(fb -> List.of(fa, fb)))
                    .flatMap(pair -> LONG_SATURATING_GENERATORS_ASSOC.stream().map(f -> List.of(pair.get(0), pair.get(1), f)))
                    .collect(Collectors.toList());

    @DataProvider
    public Object[][] boolUnaryOpProvider() {
        return BOOL_ARRAY_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<List<IntFunction<long[]>>> LONG_GENERATOR_TRIPLES =
        LONG_GENERATOR_PAIRS.stream().
                flatMap(pair -> LONG_GENERATORS.stream().map(f -> List.of(pair.get(0), pair.get(1), f))).
                collect(Collectors.toList());

    static final List<IntFunction<long[]>> SELECT_FROM_INDEX_GENERATORS = List.of(
            withToString("long[0..VECLEN*2)", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(RAND.nextInt()));
            })
    );

    static final List<List<IntFunction<long[]>>> LONG_GENERATOR_SELECT_FROM_TRIPLES =
        LONG_GENERATOR_PAIRS.stream().
                flatMap(pair -> SELECT_FROM_INDEX_GENERATORS.stream().map(f -> List.of(pair.get(0), pair.get(1), f))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] longBinaryOpProvider() {
        return LONG_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longSaturatingBinaryOpProvider() {
        return LONG_SATURATING_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longSaturatingBinaryOpAssocProvider() {
        return LONG_SATURATING_GENERATOR_TRIPLETS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longSaturatingBinaryOpAssocMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_SATURATING_GENERATOR_TRIPLETS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }


    @DataProvider
    public Object[][] longIndexedOpProvider() {
        return LONG_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longSaturatingBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_SATURATING_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

   @DataProvider
   public Object[][] longSaturatingUnaryOpProvider() {
       return LONG_SATURATING_GENERATORS.stream().
                    map(f -> new Object[]{f}).
                    toArray(Object[][]::new);
   }

   @DataProvider
   public Object[][] longSaturatingUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_SATURATING_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
   }

    @DataProvider
    public Object[][] longBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longTernaryOpProvider() {
        return LONG_GENERATOR_TRIPLES.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longSelectFromTwoVectorOpProvider() {
        return LONG_GENERATOR_SELECT_FROM_TRIPLES.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longTernaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_GENERATOR_TRIPLES.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpProvider() {
        return LONG_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longtoIntUnaryOpProvider() {
        return INT_LONG_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] maskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] maskCompareOpProvider() {
        return BOOLEAN_MASK_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shuffleProvider() {
        return INT_SHUFFLE_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shuffleCompareOpProvider() {
        return INT_SHUFFLE_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpShuffleProvider() {
        return INT_SHUFFLE_GENERATORS.stream().
                flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpShuffleMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INT_SHUFFLE_GENERATORS.stream().
                    flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                        return new Object[] {fa, fs, fm};
                }))).
                toArray(Object[][]::new);
    }

    static final List<BiFunction<Integer,Integer,long[]>> LONG_SHUFFLE_GENERATORS = List.of(
            withToStringBi("shuffle[random]", (Integer l, Integer m) -> {
                long[] a = new long[l];
                int upper = m;
                for (int i = 0; i < 1; i++) {
                    a[i] = (long)RAND.nextInt(upper);
                }
                return a;
            })
    );

    @DataProvider
    public Object[][] longUnaryOpSelectFromProvider() {
        return LONG_SHUFFLE_GENERATORS.stream().
                flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpSelectFromMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_SHUFFLE_GENERATORS.stream().
                    flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                        return new Object[] {fa, fs, fm};
                }))).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<long[]>> LONG_COMPARE_GENERATORS = List.of(
            withToString("long[i]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)i);
            }),
            withToString("long[i - length / 2]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i - (s * BUFFER_REPS / 2)));
            }),
            withToString("long[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i + 1));
            }),
            withToString("long[i - 2]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i - 2));
            }),
            withToString("long[zigZag(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> i%3 == 0 ? (long)i : (i%3 == 1 ? (long)(i + 1) : (long)(i - 2)));
            }),
            withToString("long[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> cornerCaseValue(i));
            })
    );

    static final List<List<IntFunction<long[]>>> LONG_TEST_GENERATOR_ARGS =
        LONG_COMPARE_GENERATORS.stream().
                map(fa -> List.of(fa)).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] longTestOpProvider() {
        return LONG_TEST_GENERATOR_ARGS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longTestOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_TEST_GENERATOR_ARGS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    static final List<List<IntFunction<long[]>>> LONG_COMPARE_GENERATOR_PAIRS =
        LONG_COMPARE_GENERATORS.stream().
                flatMap(fa -> LONG_COMPARE_GENERATORS.stream().map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] longCompareOpProvider() {
        return LONG_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longCompareOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_COMPARE_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    interface ToLongF {
        long apply(int i);
    }

    static long[] fill(int s , ToLongF f) {
        return fill(new long[s], f);
    }

    static long[] fill(long[] a, ToLongF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    static long cornerCaseValue(int i) {
        switch(i % 5) {
            case 0:
                return Long.MAX_VALUE;
            case 1:
                return Long.MIN_VALUE;
            case 2:
                return Long.MIN_VALUE;
            case 3:
                return Long.MAX_VALUE;
            default:
                return (long)0;
        }
    }

    static final IntFunction<long[]> fr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new long[length];
    };

    static final IntFunction<boolean[]> fmr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new boolean[length];
    };

    static void replaceZero(long[] a, long v) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 0) {
                a[i] = v;
            }
        }
    }

    static void replaceZero(long[] a, boolean[] mask, long v) {
        for (int i = 0; i < a.length; i++) {
            if (mask[i % mask.length] && a[i] == 0) {
                a[i] = v;
            }
        }
    }

    static long ROL_scalar(long a, long b) {
        return Long.rotateLeft(a, ((int)b));
    }

    static long ROR_scalar(long a, long b) {
        return Long.rotateRight(a, ((int)b));
    }

    static long TRAILING_ZEROS_COUNT_scalar(long a) {
        return Long.numberOfTrailingZeros(a);
    }

    static long LEADING_ZEROS_COUNT_scalar(long a) {
        return Long.numberOfLeadingZeros(a);
    }

    static long REVERSE_scalar(long a) {
        return Long.reverse(a);
    }

    static boolean eq(long a, long b) {
        return a == b;
    }

    static boolean neq(long a, long b) {
        return a != b;
    }

    static boolean lt(long a, long b) {
        return a < b;
    }

    static boolean le(long a, long b) {
        return a <= b;
    }

    static boolean gt(long a, long b) {
        return a > b;
    }

    static boolean ge(long a, long b) {
        return a >= b;
    }

    static boolean ult(long a, long b) {
        return Long.compareUnsigned(a, b) < 0;
    }

    static boolean ule(long a, long b) {
        return Long.compareUnsigned(a, b) <= 0;
    }

    static boolean ugt(long a, long b) {
        return Long.compareUnsigned(a, b) > 0;
    }

    static boolean uge(long a, long b) {
        return Long.compareUnsigned(a, b) >= 0;
    }

    static long firstNonZero(long a, long b) {
        return Long.compare(a, (long) 0) != 0 ? a : b;
    }

    @Test
    static void smokeTest1() {
        LongVector three = LongVector.broadcast(SPECIES, (byte)-3);
        LongVector three2 = (LongVector) SPECIES.broadcast(-3);
        assert(three.eq(three2).allTrue());
        LongVector three3 = three2.broadcast(1).broadcast(-3);
        assert(three.eq(three3).allTrue());
        int scale = 2;
        Class<?> ETYPE = long.class;
        if (ETYPE == double.class || ETYPE == long.class)
            scale = 1000000;
        else if (ETYPE == byte.class && SPECIES.length() >= 64)
            scale = 1;
        LongVector higher = three.addIndex(scale);
        VectorMask<Long> m = three.compare(VectorOperators.LE, higher);
        assert(m.allTrue());
        m = higher.min((long)-1).test(VectorOperators.IS_NEGATIVE);
        assert(m.allTrue());
        long max = higher.reduceLanes(VectorOperators.MAX);
        assert(max == -3 + scale * (SPECIES.length()-1));
    }

    private static long[]
    bothToArray(LongVector a, LongVector b) {
        long[] r = new long[a.length() + b.length()];
        a.intoArray(r, 0);
        b.intoArray(r, a.length());
        return r;
    }

    @Test
    static void smokeTest2() {
        // Do some zipping and shuffling.
        LongVector io = (LongVector) SPECIES.broadcast(0).addIndex(1);
        LongVector io2 = (LongVector) VectorShuffle.iota(SPECIES,0,1,false).toVector();
        Assert.assertEquals(io, io2);
        LongVector a = io.add((long)1); //[1,2]
        LongVector b = a.neg();  //[-1,-2]
        long[] abValues = bothToArray(a,b); //[1,2,-1,-2]
        VectorShuffle<Long> zip0 = VectorShuffle.makeZip(SPECIES, 0);
        VectorShuffle<Long> zip1 = VectorShuffle.makeZip(SPECIES, 1);
        LongVector zab0 = a.rearrange(zip0,b); //[1,-1]
        LongVector zab1 = a.rearrange(zip1,b); //[2,-2]
        long[] zabValues = bothToArray(zab0, zab1); //[1,-1,2,-2]
        // manually zip
        long[] manual = new long[zabValues.length];
        for (int i = 0; i < manual.length; i += 2) {
            manual[i+0] = abValues[i/2];
            manual[i+1] = abValues[a.length() + i/2];
        }
        Assert.assertEquals(Arrays.toString(zabValues), Arrays.toString(manual));
        VectorShuffle<Long> unz0 = VectorShuffle.makeUnzip(SPECIES, 0);
        VectorShuffle<Long> unz1 = VectorShuffle.makeUnzip(SPECIES, 1);
        LongVector uab0 = zab0.rearrange(unz0,zab1);
        LongVector uab1 = zab0.rearrange(unz1,zab1);
        long[] abValues1 = bothToArray(uab0, uab1);
        Assert.assertEquals(Arrays.toString(abValues), Arrays.toString(abValues1));
    }

    static void iotaShuffle() {
        LongVector io = (LongVector) SPECIES.broadcast(0).addIndex(1);
        LongVector io2 = (LongVector) VectorShuffle.iota(SPECIES, 0 , 1, false).toVector();
        Assert.assertEquals(io, io2);
    }

    @Test
    // Test all shuffle related operations.
    static void shuffleTest() {
        // To test backend instructions, make sure that C2 is used.
        for (int loop = 0; loop < INVOC_COUNT * INVOC_COUNT; loop++) {
            iotaShuffle();
        }
    }

    @Test
    void viewAsIntegeralLanesTest() {
        Vector<?> asIntegral = SPECIES.zero().viewAsIntegralLanes();
        Assert.assertEquals(asIntegral.species(), SPECIES);
    }

    @Test
    void viewAsFloatingLanesTest() {
        Vector<?> asFloating = SPECIES.zero().viewAsFloatingLanes();
        VectorSpecies<?> asFloatingSpecies = asFloating.species();
        Assert.assertNotEquals(asFloatingSpecies.elementType(), SPECIES.elementType());
        Assert.assertEquals(asFloatingSpecies.vectorShape(), SPECIES.vectorShape());
        Assert.assertEquals(asFloatingSpecies.length(), SPECIES.length());
        Assert.assertEquals(asFloating.viewAsIntegralLanes().species(), SPECIES);
    }

    @Test
    // Test div by 0.
    static void bitwiseDivByZeroSmokeTest() {
        try {
            LongVector a = (LongVector) SPECIES.broadcast(0).addIndex(1);
            LongVector b = (LongVector) SPECIES.broadcast(0);
            a.div(b);
            Assert.fail();
        } catch (ArithmeticException e) {
        }

        try {
            LongVector a = (LongVector) SPECIES.broadcast(0).addIndex(1);
            LongVector b = (LongVector) SPECIES.broadcast(0);
            VectorMask<Long> m = a.lt((long) 1);
            a.div(b, m);
            Assert.fail();
        } catch (ArithmeticException e) {
        }
    }

    static long ADD(long a, long b) {
        return (long)(a + b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ADDLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::ADD);
    }

    static long add(long a, long b) {
        return (long)(a + b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void addLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::add);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ADDLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::ADD);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void addLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.add(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::add);
    }

    static long SUB(long a, long b) {
        return (long)(a - b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void SUBLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::SUB);
    }

    static long sub(long a, long b) {
        return (long)(a - b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void subLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::sub);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void SUBLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::SUB);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void subLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.sub(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::sub);
    }

    static long MUL(long a, long b) {
        return (long)(a * b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MULLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::MUL);
    }

    static long mul(long a, long b) {
        return (long)(a * b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void mulLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::mul);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void MULLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::MUL);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void mulLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.mul(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::mul);
    }

    static long DIV(long a, long b) {
        return (long)(a / b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void DIVLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        replaceZero(b, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.DIV, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::DIV);
    }

    static long div(long a, long b) {
        return (long)(a / b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void divLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        replaceZero(b, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.div(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::div);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void DIVLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        replaceZero(b, mask, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.DIV, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::DIV);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void divLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        replaceZero(b, mask, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.div(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::div);
    }

    static long FIRST_NONZERO(long a, long b) {
        return (long)((a)!=0?a:b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void FIRST_NONZEROLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::FIRST_NONZERO);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void FIRST_NONZEROLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::FIRST_NONZERO);
    }

    static long AND(long a, long b) {
        return (long)(a & b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ANDLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::AND);
    }

    static long and(long a, long b) {
        return (long)(a & b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void andLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.and(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::and);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ANDLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::AND);
    }

    static long AND_NOT(long a, long b) {
        return (long)(a & ~b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void AND_NOTLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::AND_NOT);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void AND_NOTLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::AND_NOT);
    }

    static long OR(long a, long b) {
        return (long)(a | b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ORLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::OR);
    }

    static long or(long a, long b) {
        return (long)(a | b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void orLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.or(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::or);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ORLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::OR);
    }

    static long XOR(long a, long b) {
        return (long)(a ^ b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void XORLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::XOR);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void XORLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::XOR);
    }

    static long COMPRESS_BITS(long a, long b) {
        return (long)(Long.compress(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void COMPRESS_BITSLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.COMPRESS_BITS, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::COMPRESS_BITS);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void COMPRESS_BITSLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.COMPRESS_BITS, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::COMPRESS_BITS);
    }

    static long EXPAND_BITS(long a, long b) {
        return (long)(Long.expand(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void EXPAND_BITSLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.EXPAND_BITS, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::EXPAND_BITS);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void EXPAND_BITSLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.EXPAND_BITS, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::EXPAND_BITS);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void addLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.add(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::add);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void addLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.add(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, LongMaxVectorTests::add);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void subLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.sub(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::sub);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void subLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.sub(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, LongMaxVectorTests::sub);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void mulLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.mul(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::mul);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void mulLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.mul(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, LongMaxVectorTests::mul);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void divLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        replaceZero(b, (long) 1);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.div(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::div);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void divLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        replaceZero(b, (long) 1);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.div(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, LongMaxVectorTests::div);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ORLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::OR);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void orLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.or(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::or);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ORLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, LongMaxVectorTests::OR);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ANDLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.AND, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::AND);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void andLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.and(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::and);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ANDLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.AND, b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, LongMaxVectorTests::AND);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ORLongMaxVectorTestsBroadcastLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, (long)b[i]).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, LongMaxVectorTests::OR);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ORLongMaxVectorTestsBroadcastMaskedLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, (long)b[i], vmask).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, mask, LongMaxVectorTests::OR);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ADDLongMaxVectorTestsBroadcastLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.ADD, (long)b[i]).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, LongMaxVectorTests::ADD);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ADDLongMaxVectorTestsBroadcastMaskedLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.ADD, (long)b[i], vmask).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, mask, LongMaxVectorTests::ADD);
    }

    static long LSHL(long a, long b) {
        return (long)((a << b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHLLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::LSHL);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHLLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::LSHL);
    }

    static long ASHR(long a, long b) {
        return (long)((a >> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ASHRLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::ASHR);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ASHRLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::ASHR);
    }

    static long LSHR(long a, long b) {
        return (long)((a >>> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHRLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::LSHR);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHRLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::LSHR);
    }

    static long LSHL_unary(long a, long b) {
        return (long)((a << b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHLLongMaxVectorTestsScalarShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, LongMaxVectorTests::LSHL_unary);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHLLongMaxVectorTestsScalarShiftMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, LongMaxVectorTests::LSHL_unary);
    }

    static long LSHR_unary(long a, long b) {
        return (long)((a >>> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHRLongMaxVectorTestsScalarShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, LongMaxVectorTests::LSHR_unary);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHRLongMaxVectorTestsScalarShiftMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, LongMaxVectorTests::LSHR_unary);
    }

    static long ASHR_unary(long a, long b) {
        return (long)((a >> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ASHRLongMaxVectorTestsScalarShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, LongMaxVectorTests::ASHR_unary);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ASHRLongMaxVectorTestsScalarShiftMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, LongMaxVectorTests::ASHR_unary);
    }

    static long ROR(long a, long b) {
        return (long)(ROR_scalar(a,b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void RORLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ROR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::ROR);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void RORLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ROR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::ROR);
    }

    static long ROL(long a, long b) {
        return (long)(ROL_scalar(a,b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ROLLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ROL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::ROL);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ROLLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ROL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::ROL);
    }

    static long ROR_unary(long a, long b) {
        return (long)(ROR_scalar(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void RORLongMaxVectorTestsScalarShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, LongMaxVectorTests::ROR_unary);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void RORLongMaxVectorTestsScalarShiftMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, LongMaxVectorTests::ROR_unary);
    }

    static long ROL_unary(long a, long b) {
        return (long)(ROL_scalar(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ROLLongMaxVectorTestsScalarShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROL, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, LongMaxVectorTests::ROL_unary);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ROLLongMaxVectorTestsScalarShiftMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROL, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, LongMaxVectorTests::ROL_unary);
    }
    static long LSHR_binary_const(long a) {
        return (long)((a >>> CONST_SHIFT));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void LSHRLongMaxVectorTestsScalarShiftConst(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, CONST_SHIFT).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, LongMaxVectorTests::LSHR_binary_const);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void LSHRLongMaxVectorTestsScalarShiftMaskedConst(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, CONST_SHIFT, vmask).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, mask, LongMaxVectorTests::LSHR_binary_const);
    }

    static long LSHL_binary_const(long a) {
        return (long)((a << CONST_SHIFT));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void LSHLLongMaxVectorTestsScalarShiftConst(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, CONST_SHIFT).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, LongMaxVectorTests::LSHL_binary_const);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void LSHLLongMaxVectorTestsScalarShiftMaskedConst(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, CONST_SHIFT, vmask).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, mask, LongMaxVectorTests::LSHL_binary_const);
    }

    static long ASHR_binary_const(long a) {
        return (long)((a >> CONST_SHIFT));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ASHRLongMaxVectorTestsScalarShiftConst(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, CONST_SHIFT).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, LongMaxVectorTests::ASHR_binary_const);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ASHRLongMaxVectorTestsScalarShiftMaskedConst(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, CONST_SHIFT, vmask).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, mask, LongMaxVectorTests::ASHR_binary_const);
    }

    static long ROR_binary_const(long a) {
        return (long)(ROR_scalar(a, CONST_SHIFT));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void RORLongMaxVectorTestsScalarShiftConst(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROR, CONST_SHIFT).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, LongMaxVectorTests::ROR_binary_const);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void RORLongMaxVectorTestsScalarShiftMaskedConst(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROR, CONST_SHIFT, vmask).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, mask, LongMaxVectorTests::ROR_binary_const);
    }

    static long ROL_binary_const(long a) {
        return (long)(ROL_scalar(a, CONST_SHIFT));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ROLLongMaxVectorTestsScalarShiftConst(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROL, CONST_SHIFT).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, LongMaxVectorTests::ROL_binary_const);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ROLLongMaxVectorTestsScalarShiftMaskedConst(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ROL, CONST_SHIFT, vmask).intoArray(r, i);
            }
        }

        assertShiftConstEquals(r, a, mask, LongMaxVectorTests::ROL_binary_const);
    }


    static LongVector bv_MIN = LongVector.broadcast(SPECIES, (long)10);

    @Test(dataProvider = "longUnaryOpProvider")
    static void MINLongMaxVectorTestsWithMemOp(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.MIN, bv_MIN).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, (long)10, LongMaxVectorTests::MIN);
    }

    static LongVector bv_min = LongVector.broadcast(SPECIES, (long)10);

    @Test(dataProvider = "longUnaryOpProvider")
    static void minLongMaxVectorTestsWithMemOp(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.min(bv_min).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, (long)10, LongMaxVectorTests::min);
    }

    static LongVector bv_MIN_M = LongVector.broadcast(SPECIES, (long)10);

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MINLongMaxVectorTestsMaskedWithMemOp(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.MIN, bv_MIN_M, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, (long)10, mask, LongMaxVectorTests::MIN);
    }

    static LongVector bv_MAX = LongVector.broadcast(SPECIES, (long)10);

    @Test(dataProvider = "longUnaryOpProvider")
    static void MAXLongMaxVectorTestsWithMemOp(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.MAX, bv_MAX).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, (long)10, LongMaxVectorTests::MAX);
    }

    static LongVector bv_max = LongVector.broadcast(SPECIES, (long)10);

    @Test(dataProvider = "longUnaryOpProvider")
    static void maxLongMaxVectorTestsWithMemOp(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.max(bv_max).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, (long)10, LongMaxVectorTests::max);
    }

    static LongVector bv_MAX_M = LongVector.broadcast(SPECIES, (long)10);

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MAXLongMaxVectorTestsMaskedWithMemOp(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.MAX, bv_MAX_M, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, (long)10, mask, LongMaxVectorTests::MAX);
    }

    static long MIN(long a, long b) {
        return (long)(Math.min(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MINLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MIN, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::MIN);
    }

    static long min(long a, long b) {
        return (long)(Math.min(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void minLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::min);
    }

    static long MAX(long a, long b) {
        return (long)(Math.max(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MAXLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MAX, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::MAX);
    }

    static long max(long a, long b) {
        return (long)(Math.max(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void maxLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::max);
    }

    static long UMIN(long a, long b) {
        return (long)(VectorMath.minUnsigned(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void UMINLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.UMIN, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::UMIN);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void UMINLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.UMIN, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::UMIN);
    }

    static long UMAX(long a, long b) {
        return (long)(VectorMath.maxUnsigned(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void UMAXLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.UMAX, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::UMAX);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void UMAXLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.UMAX, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::UMAX);
    }

    static long SADD(long a, long b) {
        return (long)(VectorMath.addSaturating(a, b));
    }

    @Test(dataProvider = "longSaturatingBinaryOpProvider")
    static void SADDLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SADD, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::SADD);
    }

    @Test(dataProvider = "longSaturatingBinaryOpMaskProvider")
    static void SADDLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SADD, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::SADD);
    }

    static long SSUB(long a, long b) {
        return (long)(VectorMath.subSaturating(a, b));
    }

    @Test(dataProvider = "longSaturatingBinaryOpProvider")
    static void SSUBLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SSUB, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::SSUB);
    }

    @Test(dataProvider = "longSaturatingBinaryOpMaskProvider")
    static void SSUBLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SSUB, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::SSUB);
    }

    static long SUADD(long a, long b) {
        return (long)(VectorMath.addSaturatingUnsigned(a, b));
    }

    @Test(dataProvider = "longSaturatingBinaryOpProvider")
    static void SUADDLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUADD, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::SUADD);
    }

    @Test(dataProvider = "longSaturatingBinaryOpMaskProvider")
    static void SUADDLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUADD, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::SUADD);
    }

    static long SUSUB(long a, long b) {
        return (long)(VectorMath.subSaturatingUnsigned(a, b));
    }

    @Test(dataProvider = "longSaturatingBinaryOpProvider")
    static void SUSUBLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUSUB, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, LongMaxVectorTests::SUSUB);
    }

    @Test(dataProvider = "longSaturatingBinaryOpMaskProvider")
    static void SUSUBLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUSUB, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::SUSUB);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MINLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.MIN, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::MIN);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void minLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.min(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::min);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MAXLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.MAX, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::MAX);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void maxLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.max(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, LongMaxVectorTests::max);
    }
    @Test(dataProvider = "longSaturatingBinaryOpAssocProvider")
    static void SUADDAssocLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] rl = fr.apply(SPECIES.length());
        long[] rr = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                LongVector cv = LongVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.SUADD, bv).lanewise(VectorOperators.SUADD, cv).intoArray(rl, i);
                av.lanewise(VectorOperators.SUADD, bv.lanewise(VectorOperators.SUADD, cv)).intoArray(rr, i);
            }
        }

        assertArraysEqualsAssociative(rl, rr, a, b, c, LongMaxVectorTests::SUADD);
    }

    @Test(dataProvider = "longSaturatingBinaryOpAssocMaskProvider")
    static void SUADDAssocLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                     IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        long[] rl = fr.apply(SPECIES.length());
        long[] rr = fr.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                LongVector cv = LongVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.SUADD, bv, vmask).lanewise(VectorOperators.SUADD, cv, vmask).intoArray(rl, i);
                av.lanewise(VectorOperators.SUADD, bv.lanewise(VectorOperators.SUADD, cv, vmask), vmask).intoArray(rr, i);
            }
        }

        assertArraysEqualsAssociative(rl, rr, a, b, c, mask, LongMaxVectorTests::SUADD);
    }

    static long ANDReduce(long[] a, int idx) {
        long res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    static long ANDReduceAll(long[] a) {
        long res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res &= ANDReduce(a, i);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ANDReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::ANDReduce, LongMaxVectorTests::ANDReduceAll);
    }

    static long ANDReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res &= a[i];
        }

        return res;
    }

    static long ANDReduceAllMasked(long[] a, boolean[] mask) {
        long res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res &= ANDReduceMasked(a, i, mask);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ANDReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::ANDReduceMasked, LongMaxVectorTests::ANDReduceAllMasked);
    }

    static long ORReduce(long[] a, int idx) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    static long ORReduceAll(long[] a) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res |= ORReduce(a, i);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ORReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::ORReduce, LongMaxVectorTests::ORReduceAll);
    }

    static long ORReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res |= a[i];
        }

        return res;
    }

    static long ORReduceAllMasked(long[] a, boolean[] mask) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res |= ORReduceMasked(a, i, mask);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ORReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::ORReduceMasked, LongMaxVectorTests::ORReduceAllMasked);
    }

    static long XORReduce(long[] a, int idx) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res ^= a[i];
        }

        return res;
    }

    static long XORReduceAll(long[] a) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res ^= XORReduce(a, i);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void XORReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::XORReduce, LongMaxVectorTests::XORReduceAll);
    }

    static long XORReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res ^= a[i];
        }

        return res;
    }

    static long XORReduceAllMasked(long[] a, boolean[] mask) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res ^= XORReduceMasked(a, i, mask);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void XORReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::XORReduceMasked, LongMaxVectorTests::XORReduceAllMasked);
    }

    static long ADDReduce(long[] a, int idx) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res += a[i];
        }

        return res;
    }

    static long ADDReduceAll(long[] a) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res += ADDReduce(a, i);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ADDReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::ADDReduce, LongMaxVectorTests::ADDReduceAll);
    }

    static long ADDReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res += a[i];
        }

        return res;
    }

    static long ADDReduceAllMasked(long[] a, boolean[] mask) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res += ADDReduceMasked(a, i, mask);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ADDReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::ADDReduceMasked, LongMaxVectorTests::ADDReduceAllMasked);
    }

    static long MULReduce(long[] a, int idx) {
        long res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res *= a[i];
        }

        return res;
    }

    static long MULReduceAll(long[] a) {
        long res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res *= MULReduce(a, i);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void MULReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::MULReduce, LongMaxVectorTests::MULReduceAll);
    }

    static long MULReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res *= a[i];
        }

        return res;
    }

    static long MULReduceAllMasked(long[] a, boolean[] mask) {
        long res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res *= MULReduceMasked(a, i, mask);
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MULReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::MULReduceMasked, LongMaxVectorTests::MULReduceAllMasked);
    }

    static long MINReduce(long[] a, int idx) {
        long res = Long.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (long) Math.min(res, a[i]);
        }

        return res;
    }

    static long MINReduceAll(long[] a) {
        long res = Long.MAX_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) Math.min(res, MINReduce(a, i));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void MINReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = Long.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) Math.min(ra, av.reduceLanes(VectorOperators.MIN));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::MINReduce, LongMaxVectorTests::MINReduceAll);
    }

    static long MINReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = Long.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res = (long) Math.min(res, a[i]);
        }

        return res;
    }

    static long MINReduceAllMasked(long[] a, boolean[] mask) {
        long res = Long.MAX_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) Math.min(res, MINReduceMasked(a, i, mask));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MINReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = Long.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) Math.min(ra, av.reduceLanes(VectorOperators.MIN, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::MINReduceMasked, LongMaxVectorTests::MINReduceAllMasked);
    }

    static long MAXReduce(long[] a, int idx) {
        long res = Long.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (long) Math.max(res, a[i]);
        }

        return res;
    }

    static long MAXReduceAll(long[] a) {
        long res = Long.MIN_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) Math.max(res, MAXReduce(a, i));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void MAXReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = Long.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) Math.max(ra, av.reduceLanes(VectorOperators.MAX));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::MAXReduce, LongMaxVectorTests::MAXReduceAll);
    }

    static long MAXReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = Long.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res = (long) Math.max(res, a[i]);
        }

        return res;
    }

    static long MAXReduceAllMasked(long[] a, boolean[] mask) {
        long res = Long.MIN_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) Math.max(res, MAXReduceMasked(a, i, mask));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MAXReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = Long.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) Math.max(ra, av.reduceLanes(VectorOperators.MAX, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::MAXReduceMasked, LongMaxVectorTests::MAXReduceAllMasked);
    }

    static long UMINReduce(long[] a, int idx) {
        long res = Long.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (long) VectorMath.minUnsigned(res, a[i]);
        }

        return res;
    }

    static long UMINReduceAll(long[] a) {
        long res = Long.MAX_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) VectorMath.minUnsigned(res, UMINReduce(a, i));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void UMINReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = Long.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.UMIN);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) VectorMath.minUnsigned(ra, av.reduceLanes(VectorOperators.UMIN));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::UMINReduce, LongMaxVectorTests::UMINReduceAll);
    }

    static long UMINReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = Long.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res = (long) VectorMath.minUnsigned(res, a[i]);
        }

        return res;
    }

    static long UMINReduceAllMasked(long[] a, boolean[] mask) {
        long res = Long.MAX_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) VectorMath.minUnsigned(res, UMINReduceMasked(a, i, mask));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void UMINReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = Long.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.UMIN, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) VectorMath.minUnsigned(ra, av.reduceLanes(VectorOperators.UMIN, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::UMINReduceMasked, LongMaxVectorTests::UMINReduceAllMasked);
    }

    static long UMAXReduce(long[] a, int idx) {
        long res = Long.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (long) VectorMath.maxUnsigned(res, a[i]);
        }

        return res;
    }

    static long UMAXReduceAll(long[] a) {
        long res = Long.MIN_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) VectorMath.maxUnsigned(res, UMAXReduce(a, i));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void UMAXReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = Long.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.UMAX);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) VectorMath.maxUnsigned(ra, av.reduceLanes(VectorOperators.UMAX));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::UMAXReduce, LongMaxVectorTests::UMAXReduceAll);
    }

    static long UMAXReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = Long.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res = (long) VectorMath.maxUnsigned(res, a[i]);
        }

        return res;
    }

    static long UMAXReduceAllMasked(long[] a, boolean[] mask) {
        long res = Long.MIN_VALUE;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) VectorMath.maxUnsigned(res, UMAXReduceMasked(a, i, mask));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void UMAXReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = Long.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.UMAX, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) VectorMath.maxUnsigned(ra, av.reduceLanes(VectorOperators.UMAX, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::UMAXReduceMasked, LongMaxVectorTests::UMAXReduceAllMasked);
    }

    static long FIRST_NONZEROReduce(long[] a, int idx) {
        long res = (long) 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = firstNonZero(res, a[i]);
        }

        return res;
    }

    static long FIRST_NONZEROReduceAll(long[] a) {
        long res = (long) 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = firstNonZero(res, FIRST_NONZEROReduce(a, i));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void FIRST_NONZEROReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = (long) 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.FIRST_NONZERO);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = (long) 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = firstNonZero(ra, av.reduceLanes(VectorOperators.FIRST_NONZERO));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::FIRST_NONZEROReduce, LongMaxVectorTests::FIRST_NONZEROReduceAll);
    }

    static long FIRST_NONZEROReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = (long) 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res = firstNonZero(res, a[i]);
        }

        return res;
    }

    static long FIRST_NONZEROReduceAllMasked(long[] a, boolean[] mask) {
        long res = (long) 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = firstNonZero(res, FIRST_NONZEROReduceMasked(a, i, mask));
        }

        return res;
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void FIRST_NONZEROReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = (long) 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.FIRST_NONZERO, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = (long) 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = firstNonZero(ra, av.reduceLanes(VectorOperators.FIRST_NONZERO, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::FIRST_NONZEROReduceMasked, LongMaxVectorTests::FIRST_NONZEROReduceAllMasked);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueLongMaxVectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(r, mask, LongMaxVectorTests::anyTrue);
    }

    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueLongMaxVectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(r, mask, LongMaxVectorTests::allTrue);
    }

    static long SUADDReduce(long[] a, int idx) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (long) VectorMath.addSaturatingUnsigned(res, a[i]);
        }

        return res;
    }

    static long SUADDReduceAll(long[] a) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) VectorMath.addSaturatingUnsigned(res, SUADDReduce(a, i));
        }

        return res;
    }

    @Test(dataProvider = "longSaturatingUnaryOpProvider")
    static void SUADDReduceLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.SUADD);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) VectorMath.addSaturatingUnsigned(ra, av.reduceLanes(VectorOperators.SUADD));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::SUADDReduce, LongMaxVectorTests::SUADDReduceAll);
    }

    static long SUADDReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res = (long) VectorMath.addSaturatingUnsigned(res, a[i]);
        }

        return res;
    }

    static long SUADDReduceAllMasked(long[] a, boolean[] mask) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res = (long) VectorMath.addSaturatingUnsigned(res, SUADDReduceMasked(a, i, mask));
        }

        return res;
    }
    @Test(dataProvider = "longSaturatingUnaryOpMaskProvider")
    static void SUADDReduceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.SUADD, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long) VectorMath.addSaturatingUnsigned(ra, av.reduceLanes(VectorOperators.SUADD, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::SUADDReduceMasked, LongMaxVectorTests::SUADDReduceAllMasked);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void withLongMaxVectorTests(IntFunction<long []> fa, IntFunction<long []> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0, j = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.withLane(j, b[i + j]).intoArray(r, i);
                a[i + j] = b[i + j];
                j = (j + 1) & (SPECIES.length() - 1);
            }
        }


        assertArraysStrictlyEquals(r, a);
    }

    static boolean testIS_DEFAULT(long a) {
        return bits(a)==0;
    }

    @Test(dataProvider = "longTestOpProvider")
    static void IS_DEFAULTLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                VectorMask<Long> mv = av.test(VectorOperators.IS_DEFAULT);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), testIS_DEFAULT(a[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longTestOpMaskProvider")
    static void IS_DEFAULTMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                VectorMask<Long> mv = av.test(VectorOperators.IS_DEFAULT, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j),  vmask.laneIsSet(j) && testIS_DEFAULT(a[i + j]));
                }
            }
        }
    }

    static boolean testIS_NEGATIVE(long a) {
        return bits(a)<0;
    }

    @Test(dataProvider = "longTestOpProvider")
    static void IS_NEGATIVELongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                VectorMask<Long> mv = av.test(VectorOperators.IS_NEGATIVE);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), testIS_NEGATIVE(a[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longTestOpMaskProvider")
    static void IS_NEGATIVEMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                VectorMask<Long> mv = av.test(VectorOperators.IS_NEGATIVE, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j),  vmask.laneIsSet(j) && testIS_NEGATIVE(a[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void LTLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), lt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void ltLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.lt(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), lt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void LTLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && lt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void GTLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), gt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void GTLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && gt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void EQLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.EQ, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), eq(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void eqLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.eq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), eq(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void EQLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.EQ, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && eq(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void NELongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.NE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), neq(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void NELongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.NE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && neq(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void LELongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), le(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void LELongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && le(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void GELongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ge(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void GELongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ge(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void ULTLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.ULT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ult(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void ULTLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.ULT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ult(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void UGTLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UGT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ugt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void UGTLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UGT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ugt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void ULELongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.ULE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ule(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void ULELongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.ULE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ule(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void UGELongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UGE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), uge(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void UGELongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UGE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && uge(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void LTLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.LT, b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i]);
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void LTLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa,
                                IntFunction<long[]> fb, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.LT, b[i], vmask);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), mask[j] && (a[i + j] < b[i]));
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void EQLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.EQ, b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i]);
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void EQLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa,
                                IntFunction<long[]> fb, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.EQ, b[i], vmask);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), mask[j] && (a[i + j] == b[i]));
            }
        }
    }


    static long blend(long a, long b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void blendLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, LongMaxVectorTests::blend);
    }

    @Test(dataProvider = "longUnaryOpShuffleProvider")
    static void RearrangeLongMaxVectorTests(IntFunction<long[]> fa,
                                           BiFunction<Integer,Integer,int[]> fs) {
        long[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.rearrange(VectorShuffle.fromArray(SPECIES, order, i)).intoArray(r, i);
            }
        }

        assertRearrangeArraysEquals(r, a, order, SPECIES.length());
    }

    @Test(dataProvider = "longUnaryOpShuffleMaskProvider")
    static void RearrangeLongMaxVectorTestsMaskedSmokeTest(IntFunction<long[]> fa,
                                                          BiFunction<Integer,Integer,int[]> fs,
                                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.rearrange(VectorShuffle.fromArray(SPECIES, order, i), vmask).intoArray(r, i);
        }

        assertRearrangeArraysEquals(r, a, order, mask, SPECIES.length());
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void compressLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.compress(vmask).intoArray(r, i);
            }
        }

        assertcompressArraysEquals(r, a, mask, SPECIES.length());
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void expandLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.expand(vmask).intoArray(r, i);
            }
        }

        assertexpandArraysEquals(r, a, mask, SPECIES.length());
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void getLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                int num_lanes = SPECIES.length();
                // Manually unroll because full unroll happens after intrinsification.
                // Unroll is needed because get intrinsic requires for index to be a known constant.
                if (num_lanes == 1) {
                    r[i]=av.lane(0);
                } else if (num_lanes == 2) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                } else if (num_lanes == 4) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                } else if (num_lanes == 8) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                } else if (num_lanes == 16) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                } else if (num_lanes == 32) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                } else if (num_lanes == 64) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                    r[i+32]=av.lane(32);
                    r[i+33]=av.lane(33);
                    r[i+34]=av.lane(34);
                    r[i+35]=av.lane(35);
                    r[i+36]=av.lane(36);
                    r[i+37]=av.lane(37);
                    r[i+38]=av.lane(38);
                    r[i+39]=av.lane(39);
                    r[i+40]=av.lane(40);
                    r[i+41]=av.lane(41);
                    r[i+42]=av.lane(42);
                    r[i+43]=av.lane(43);
                    r[i+44]=av.lane(44);
                    r[i+45]=av.lane(45);
                    r[i+46]=av.lane(46);
                    r[i+47]=av.lane(47);
                    r[i+48]=av.lane(48);
                    r[i+49]=av.lane(49);
                    r[i+50]=av.lane(50);
                    r[i+51]=av.lane(51);
                    r[i+52]=av.lane(52);
                    r[i+53]=av.lane(53);
                    r[i+54]=av.lane(54);
                    r[i+55]=av.lane(55);
                    r[i+56]=av.lane(56);
                    r[i+57]=av.lane(57);
                    r[i+58]=av.lane(58);
                    r[i+59]=av.lane(59);
                    r[i+60]=av.lane(60);
                    r[i+61]=av.lane(61);
                    r[i+62]=av.lane(62);
                    r[i+63]=av.lane(63);
                } else {
                    for (int j = 0; j < SPECIES.length(); j++) {
                        r[i+j]=av.lane(j);
                    }
                }
            }
        }

        assertArraysStrictlyEquals(r, a);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void BroadcastLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector.broadcast(SPECIES, a[i]).intoArray(r, i);
            }
        }

        assertBroadcastArraysEquals(r, a);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ZeroLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector.zero(SPECIES).intoArray(a, i);
            }
        }

        Assert.assertEquals(a, r);
    }

    static long[] sliceUnary(long[] a, int origin, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = a[idx+i+origin];
            else
                res[i] = (long)0;
        }
        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void sliceUnaryLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = RAND.nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.slice(origin).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, origin, LongMaxVectorTests::sliceUnary);
    }

    static long[] sliceBinary(long[] a, long[] b, int origin, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = a[idx+i+origin];
            else {
                res[i] = b[idx+j];
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void sliceBinaryLongMaxVectorTestsBinary(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = RAND.nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.slice(origin, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, LongMaxVectorTests::sliceBinary);
    }

    static long[] slice(long[] a, long[] b, int origin, boolean[] mask, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = mask[i] ? a[idx+i+origin] : (long)0;
            else {
                res[i] = mask[i] ? b[idx+j] : (long)0;
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void sliceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
    IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        long[] r = new long[a.length];
        int origin = RAND.nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.slice(origin, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, mask, LongMaxVectorTests::slice);
    }

    static long[] unsliceUnary(long[] a, int origin, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i < origin)
                res[i] = (long)0;
            else {
                res[i] = a[idx+j];
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void unsliceUnaryLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = RAND.nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.unslice(origin).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, origin, LongMaxVectorTests::unsliceUnary);
    }

    static long[] unsliceBinary(long[] a, long[] b, int origin, int part, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if (part == 0) {
                if (i < origin)
                    res[i] = b[idx+i];
                else {
                    res[i] = a[idx+j];
                    j++;
                }
            } else if (part == 1) {
                if (i < origin)
                    res[i] = a[idx+SPECIES.length()-origin+i];
                else {
                    res[i] = b[idx+origin+j];
                    j++;
                }
            }
        }
        return res;
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void unsliceBinaryLongMaxVectorTestsBinary(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = RAND.nextInt(SPECIES.length());
        int part = RAND.nextInt(2);
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.unslice(origin, bv, part).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, part, LongMaxVectorTests::unsliceBinary);
    }

    static long[] unslice(long[] a, long[] b, int origin, int part, boolean[] mask, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = b[idx+i+origin];
            else {
                res[i] = b[idx+j];
                j++;
            }
        }
        for (int i = 0; i < SPECIES.length(); i++){
            res[i] = mask[i] ? a[idx+i] : res[i];
        }
        long[] res1 = new long[SPECIES.length()];
        if (part == 0) {
            for (int i = 0, j = 0; i < SPECIES.length(); i++){
                if (i < origin)
                    res1[i] = b[idx+i];
                else {
                   res1[i] = res[j];
                   j++;
                }
            }
        } else if (part == 1) {
            for (int i = 0, j = 0; i < SPECIES.length(); i++){
                if (i < origin)
                    res1[i] = res[SPECIES.length()-origin+i];
                else {
                    res1[i] = b[idx+origin+j];
                    j++;
                }
            }
        }
        return res1;
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void unsliceLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
    IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long[] r = new long[a.length];
        int origin = RAND.nextInt(SPECIES.length());
        int part = RAND.nextInt(2);
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.unslice(origin, bv, part, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, part, mask, LongMaxVectorTests::unslice);
    }

    static long BITWISE_BLEND(long a, long b, long c) {
        return (long)((a&~(c))|(b&c));
    }

    static long bitwiseBlend(long a, long b, long c) {
        return (long)((a&~(c))|(b&c));
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                LongVector cv = LongVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, c, LongMaxVectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.bitwiseBlend(bv, cv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, c, LongMaxVectorTests::bitwiseBlend);
    }

    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                LongVector cv = LongVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, c, mask, LongMaxVectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, bv, c[i]).intoArray(r, i);
        }
        assertBroadcastArraysEquals(r, a, b, c, LongMaxVectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLongMaxVectorTestsAltBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], cv).intoArray(r, i);
        }
        assertAltBroadcastArraysEquals(r, a, b, c, LongMaxVectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.bitwiseBlend(bv, c[i]).intoArray(r, i);
        }
        assertBroadcastArraysEquals(r, a, b, c, LongMaxVectorTests::bitwiseBlend);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLongMaxVectorTestsAltBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.bitwiseBlend(b[i], cv).intoArray(r, i);
        }
        assertAltBroadcastArraysEquals(r, a, b, c, LongMaxVectorTests::bitwiseBlend);
    }

    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, bv, c[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, c, mask, LongMaxVectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLongMaxVectorTestsAltBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], cv, vmask).intoArray(r, i);
        }

        assertAltBroadcastArraysEquals(r, a, b, c, mask, LongMaxVectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLongMaxVectorTestsDoubleBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], c[i]).intoArray(r, i);
        }

        assertDoubleBroadcastArraysEquals(r, a, b, c, LongMaxVectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLongMaxVectorTestsDoubleBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.bitwiseBlend(b[i], c[i]).intoArray(r, i);
        }

        assertDoubleBroadcastArraysEquals(r, a, b, c, LongMaxVectorTests::bitwiseBlend);
    }

    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLongMaxVectorTestsDoubleBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], c[i], vmask).intoArray(r, i);
        }

        assertDoubleBroadcastArraysEquals(r, a, b, c, mask, LongMaxVectorTests::BITWISE_BLEND);
    }

    static long NEG(long a) {
        return (long)(-((long)a));
    }

    static long neg(long a) {
        return (long)(-((long)a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void NEGLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::NEG);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void negLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::neg);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void NEGMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::NEG);
    }

    static long ABS(long a) {
        return (long)(Math.abs((long)a));
    }

    static long abs(long a) {
        return (long)(Math.abs((long)a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ABSLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::ABS);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void absLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::abs);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ABSMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::ABS);
    }

    static long NOT(long a) {
        return (long)(~((long)a));
    }

    static long not(long a) {
        return (long)(~((long)a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void NOTLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::NOT);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void notLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.not().intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::not);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void NOTMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::NOT);
    }

    static long ZOMO(long a) {
        return (long)((a==0?0:-1));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ZOMOLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::ZOMO);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ZOMOMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::ZOMO);
    }

    static long BIT_COUNT(long a) {
        return (long)(Long.bitCount(a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void BIT_COUNTLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.BIT_COUNT).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::BIT_COUNT);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void BIT_COUNTMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.BIT_COUNT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::BIT_COUNT);
    }

    static long TRAILING_ZEROS_COUNT(long a) {
        return (long)(TRAILING_ZEROS_COUNT_scalar(a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void TRAILING_ZEROS_COUNTLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.TRAILING_ZEROS_COUNT).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::TRAILING_ZEROS_COUNT);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void TRAILING_ZEROS_COUNTMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.TRAILING_ZEROS_COUNT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::TRAILING_ZEROS_COUNT);
    }

    static long LEADING_ZEROS_COUNT(long a) {
        return (long)(LEADING_ZEROS_COUNT_scalar(a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void LEADING_ZEROS_COUNTLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LEADING_ZEROS_COUNT).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::LEADING_ZEROS_COUNT);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void LEADING_ZEROS_COUNTMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LEADING_ZEROS_COUNT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::LEADING_ZEROS_COUNT);
    }

    static long REVERSE(long a) {
        return (long)(REVERSE_scalar(a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void REVERSELongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.REVERSE).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::REVERSE);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void REVERSEMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.REVERSE, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::REVERSE);
    }

    static long REVERSE_BYTES(long a) {
        return (long)(Long.reverseBytes(a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void REVERSE_BYTESLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.REVERSE_BYTES).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, LongMaxVectorTests::REVERSE_BYTES);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void REVERSE_BYTESMaskedLongMaxVectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.REVERSE_BYTES, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, LongMaxVectorTests::REVERSE_BYTES);
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void ltLongMaxVectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.lt(b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i]);
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void eqLongMaxVectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.eq(b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i]);
            }
        }
    }

    @Test(dataProvider = "longtoIntUnaryOpProvider")
    static void toIntArrayLongMaxVectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            int[] r = av.toIntArray();
            assertArraysEquals(r, a, i);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void toLongArrayLongMaxVectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            long[] r = av.toLongArray();
            assertArraysEquals(r, a, i);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void toDoubleArrayLongMaxVectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            double[] r = av.toDoubleArray();
            assertArraysEquals(r, a, i);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void toStringLongMaxVectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            String str = av.toString();

            long subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            Assert.assertTrue(str.equals(Arrays.toString(subarr)), "at index " + i + ", string should be = " + Arrays.toString(subarr) + ", but is = " + str);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void hashCodeLongMaxVectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            int hash = av.hashCode();

            long subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            int expectedHash = Objects.hash(SPECIES, Arrays.hashCode(subarr));
            Assert.assertTrue(hash == expectedHash, "at index " + i + ", hash should be = " + expectedHash + ", but is = " + hash);
        }
    }



    @Test(dataProvider = "longUnaryOpProvider")
    static void ADDReduceLongLongMaxVectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            r[i] = av.reduceLanesToLong(VectorOperators.ADD);
        }

        ra = 0;
        for (int i = 0; i < a.length; i ++) {
            ra += r[i];
        }

        assertReductionArraysEquals(r, ra, a,
                LongMaxVectorTests::ADDReduce, LongMaxVectorTests::ADDReduceAll);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ADDReduceLongLongMaxVectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            r[i] = av.reduceLanesToLong(VectorOperators.ADD, vmask);
        }

        ra = 0;
        for (int i = 0; i < a.length; i ++) {
            ra += r[i];
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                LongMaxVectorTests::ADDReduceMasked, LongMaxVectorTests::ADDReduceAllMasked);
    }

    @Test(dataProvider = "longUnaryOpSelectFromProvider")
    static void SelectFromLongMaxVectorTests(IntFunction<long[]> fa,
                                           BiFunction<Integer,Integer,long[]> fs) {
        long[] a = fa.apply(SPECIES.length());
        long[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, order, i);
            bv.selectFrom(av).intoArray(r, i);
        }

        assertSelectFromArraysEquals(r, a, order, SPECIES.length());
    }

    @Test(dataProvider = "longSelectFromTwoVectorOpProvider")
    static void SelectFromTwoVectorLongMaxVectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] idx = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < idx.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                LongVector idxv = LongVector.fromArray(SPECIES, idx, i);
                idxv.selectFrom(av, bv).intoArray(r, i);
            }
        }
        assertSelectFromTwoVectorEquals(r, idx, a, b, SPECIES.length());
    }

    @Test(dataProvider = "longUnaryOpSelectFromMaskProvider")
    static void SelectFromLongMaxVectorTestsMaskedSmokeTest(IntFunction<long[]> fa,
                                                           BiFunction<Integer,Integer,long[]> fs,
                                                           IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, order, i);
            bv.selectFrom(av, vmask).intoArray(r, i);
        }

        assertSelectFromArraysEquals(r, a, order, mask, SPECIES.length());
    }

    @Test(dataProvider = "shuffleProvider")
    static void shuffleMiscellaneousLongMaxVectorTestsSmokeTest(BiFunction<Integer,Integer,int[]> fs) {
        int[] a = fs.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var shuffle = VectorShuffle.fromArray(SPECIES, a, i);
            int hash = shuffle.hashCode();
            int length = shuffle.length();

            int subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            int expectedHash = Objects.hash(SPECIES, Arrays.hashCode(subarr));
            Assert.assertTrue(hash == expectedHash, "at index " + i + ", hash should be = " + expectedHash + ", but is = " + hash);
            Assert.assertEquals(length, SPECIES.length());
        }
    }

    @Test(dataProvider = "shuffleProvider")
    static void shuffleToStringLongMaxVectorTestsSmokeTest(BiFunction<Integer,Integer,int[]> fs) {
        int[] a = fs.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var shuffle = VectorShuffle.fromArray(SPECIES, a, i);
            String str = shuffle.toString();

            int subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            Assert.assertTrue(str.equals("Shuffle" + Arrays.toString(subarr)), "at index " +
                i + ", string should be = " + Arrays.toString(subarr) + ", but is = " + str);
        }
    }

    @Test(dataProvider = "shuffleCompareOpProvider")
    static void shuffleEqualsLongMaxVectorTestsSmokeTest(BiFunction<Integer,Integer,int[]> fa, BiFunction<Integer,Integer,int[]> fb) {
        int[] a = fa.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());
        int[] b = fb.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = VectorShuffle.fromArray(SPECIES, a, i);
            var bv = VectorShuffle.fromArray(SPECIES, b, i);
            boolean eq = av.equals(bv);
            int to = i + SPECIES.length();
            Assert.assertEquals(eq, Arrays.equals(a, i, to, b, i, to));
        }
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskEqualsLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            boolean equals = av.equals(bv);
            int to = i + SPECIES.length();
            Assert.assertEquals(equals, Arrays.equals(a, i, to, b, i, to));
        }
    }

    static boolean band(boolean a, boolean b) {
        return a & b;
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskAndLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());
        boolean[] r = new boolean[a.length];

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            var cv = av.and(bv);
            cv.intoArray(r, i);
        }
        assertArraysEquals(r, a, b, LongMaxVectorTests::band);
    }

    static boolean bor(boolean a, boolean b) {
        return a | b;
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskOrLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());
        boolean[] r = new boolean[a.length];

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            var cv = av.or(bv);
            cv.intoArray(r, i);
        }
        assertArraysEquals(r, a, b, LongMaxVectorTests::bor);
    }

    static boolean bxor(boolean a, boolean b) {
        return a != b;
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskXorLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());
        boolean[] r = new boolean[a.length];

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            var cv = av.xor(bv);
            cv.intoArray(r, i);
        }
        assertArraysEquals(r, a, b, LongMaxVectorTests::bxor);
    }

    static boolean bandNot(boolean a, boolean b) {
        return a & !b;
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskAndNotLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());
        boolean[] r = new boolean[a.length];

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            var cv = av.andNot(bv);
            cv.intoArray(r, i);
        }
        assertArraysEquals(r, a, b, LongMaxVectorTests::bandNot);
    }

    static boolean beq(boolean a, boolean b) {
        return (a == b);
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskEqLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());
        boolean[] r = new boolean[a.length];

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            var cv = av.eq(bv);
            cv.intoArray(r, i);
        }
        assertArraysEquals(r, a, b, LongMaxVectorTests::beq);
    }

    @Test(dataProvider = "maskProvider")
    static void maskHashCodeLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var vmask = SPECIES.loadMask(a, i);
            int hash = vmask.hashCode();

            boolean subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            int expectedHash = Objects.hash(SPECIES, Arrays.hashCode(subarr));
            Assert.assertTrue(hash == expectedHash, "at index " + i + ", hash should be = " + expectedHash + ", but is = " + hash);
        }
    }

    static int maskTrueCount(boolean[] a, int idx) {
        int trueCount = 0;
        for (int i = idx; i < idx + SPECIES.length(); i++) {
            trueCount += a[i] ? 1 : 0;
        }
        return trueCount;
    }

    @Test(dataProvider = "maskProvider")
    static void maskTrueCountLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT * INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                r[i] = vmask.trueCount();
            }
        }

        assertMaskReductionArraysEquals(r, a, LongMaxVectorTests::maskTrueCount);
    }

    static int maskLastTrue(boolean[] a, int idx) {
        int i = idx + SPECIES.length() - 1;
        for (; i >= idx; i--) {
            if (a[i]) {
                break;
            }
        }
        return i - idx;
    }

    @Test(dataProvider = "maskProvider")
    static void maskLastTrueLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT * INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                r[i] = vmask.lastTrue();
            }
        }

        assertMaskReductionArraysEquals(r, a, LongMaxVectorTests::maskLastTrue);
    }

    static int maskFirstTrue(boolean[] a, int idx) {
        int i = idx;
        for (; i < idx + SPECIES.length(); i++) {
            if (a[i]) {
                break;
            }
        }
        return i - idx;
    }

    @Test(dataProvider = "maskProvider")
    static void maskFirstTrueLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT * INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                r[i] = vmask.firstTrue();
            }
        }

        assertMaskReductionArraysEquals(r, a, LongMaxVectorTests::maskFirstTrue);
    }

    @Test(dataProvider = "maskProvider")
    static void maskCompressLongMaxVectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        int trueCount = 0;
        boolean[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT * INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                trueCount = vmask.trueCount();
                var rmask = vmask.compress();
                for (int j = 0; j < SPECIES.length(); j++)  {
                    Assert.assertEquals(rmask.laneIsSet(j), j < trueCount);
                }
            }
        }
    }


    @DataProvider
    public static Object[][] offsetProvider() {
        return new Object[][]{
                {0},
                {-1},
                {+1},
                {+2},
                {-2},
        };
    }

    @Test(dataProvider = "offsetProvider")
    static void indexInRangeLongMaxVectorTestsSmokeTest(int offset) {
        int limit = SPECIES.length() * BUFFER_REPS;
        for (int i = 0; i < limit; i += SPECIES.length()) {
            var actualMask = SPECIES.indexInRange(i + offset, limit);
            var expectedMask = SPECIES.maskAll(true).indexInRange(i + offset, limit);
            assert(actualMask.equals(expectedMask));
            for (int j = 0; j < SPECIES.length(); j++)  {
                int index = i + j + offset;
                Assert.assertEquals(actualMask.laneIsSet(j), index >= 0 && index < limit);
            }
        }
    }

    @Test(dataProvider = "offsetProvider")
    static void indexInRangeLongLongMaxVectorTestsSmokeTest(int offset) {
        long limit = SPECIES.length() * BUFFER_REPS;
        for (long i = 0; i < limit; i += SPECIES.length()) {
            var actualMask = SPECIES.indexInRange(i + offset, limit);
            var expectedMask = SPECIES.maskAll(true).indexInRange(i + offset, limit);
            assert(actualMask.equals(expectedMask));
            for (int j = 0; j < SPECIES.length(); j++)  {
                long index = i + j + offset;
                Assert.assertEquals(actualMask.laneIsSet(j), index >= 0 && index < limit);
            }
        }
    }

    @DataProvider
    public static Object[][] lengthProvider() {
        return new Object[][]{
                {0},
                {1},
                {32},
                {37},
                {1024},
                {1024+1},
                {1024+5},
        };
    }

    @Test(dataProvider = "lengthProvider")
    static void loopBoundLongMaxVectorTestsSmokeTest(int length) {
        int actualLoopBound = SPECIES.loopBound(length);
        int expectedLoopBound = length - Math.floorMod(length, SPECIES.length());
        Assert.assertEquals(actualLoopBound, expectedLoopBound);
    }

    @Test(dataProvider = "lengthProvider")
    static void loopBoundLongLongMaxVectorTestsSmokeTest(int _length) {
        long length = _length;
        long actualLoopBound = SPECIES.loopBound(length);
        long expectedLoopBound = length - Math.floorMod(length, SPECIES.length());
        Assert.assertEquals(actualLoopBound, expectedLoopBound);
    }

    @Test
    static void ElementSizeLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        int elsize = av.elementSize();
        Assert.assertEquals(elsize, Long.SIZE);
    }

    @Test
    static void VectorShapeLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorShape vsh = av.shape();
        assert(vsh.equals(VectorShape.S_Max_BIT));
    }

    @Test
    static void ShapeWithLanesLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorShape vsh = av.shape();
        VectorSpecies species = vsh.withLanes(long.class);
        assert(species.equals(SPECIES));
    }

    @Test
    static void ElementTypeLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        assert(av.species().elementType() == long.class);
    }

    @Test
    static void SpeciesElementSizeLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        assert(av.species().elementSize() == Long.SIZE);
    }

    @Test
    static void VectorTypeLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        assert(av.species().vectorType() == av.getClass());
    }

    @Test
    static void WithLanesLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorSpecies species = av.species().withLanes(long.class);
        assert(species.equals(SPECIES));
    }

    @Test
    static void WithShapeLongMaxVectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorShape vsh = av.shape();
        VectorSpecies species = av.species().withShape(vsh);
        assert(species.equals(SPECIES));
    }

    @Test
    static void MaskAllTrueLongMaxVectorTestsSmokeTest() {
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
          Assert.assertEquals(SPECIES.maskAll(true).toLong(), -1L >>> (64 - SPECIES.length()));
        }
    }
}
