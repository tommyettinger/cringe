package com.github.tommyettinger.cringe;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.github.tommyettinger.cringe.RotationSupport.*;

public class RotationTest {
    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}.
     * @param seed any long; will be scrambled
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @return a newly-allocated {@code dimension * dimension}-element float array, meant as effectively a
     * {@code dimension}-D rotation matrix
     */
    public static float[] randomRotationArbitrary0(long seed, int dimension) {
        dimension = Math.max(2, dimension);
        float[] base = randomRotation2D(seed);
        for (int d = 3; d <= dimension; d++) {
            base = rotateStep(seed += d, base, d);
        }
        return base;
    }

    @Test
    public void testRandomRotationArbitrary() {
        float[] r0, r1;
        StringBuilder sb = new StringBuilder();
        for (int d = 2; d < 16; d++) {
            long seed = Scramblers.scramble(d);
            for (int i = 0; i < 16; i++) {
                seed = Scramblers.scramble(seed + i);
                r0 = randomRotationArbitrary0(seed, d);
                r1 = randomRotationArbitrary(seed, d);
                appendMatrix(sb.append("float[] r0 = "), r0, d).append(";\n");
                appendMatrix(sb.append("float[] r1 = "), r1, d).append(";\n");
                System.out.println(sb);
                sb.setLength(0);
                Assert.assertArrayEquals(r0, r1, 0.0001f);
            }
        }
    }
}
