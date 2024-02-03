/*
 * Copyright (c) 2022-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.cringe;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.OrderedMap;

import java.util.Random;

/**
 * An implementation of the Poisson Disk sampling algorithm on a continuous plane. The sampleXYZ methods here all return
 * OrderedMap results with Vector2 keys and Array-of-Vector2 values, where the key represents a sampled (and
 * chosen) point and the value represents "children" of the key point. The value is often an empty array; if it isn't,
 * it holds some amount of Vector2 nodes (always other keys in the result) that radiate away from the center. You
 * may want just the sampled points; get the {@link OrderedMap#orderedKeys()} Array for a convenient source of those.
 */
public final class PoissonDiskSmooth {
    private static final float inverseRootTwo = (float)Math.sqrt(0.5f);

    /**
     * No need to instantiate.
     */
    private PoissonDiskSmooth() {
    }

    /**
     * Get a group of Vector2s, each randomly positioned around the given center out to the given radius (measured
     * with Euclidean distance, so a true circle), but with the given minimum distance from any other Vector2 in the
     * group. The parameters maxX and maxY should typically correspond to the width and height of the map; no points
     * will have positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will
     * have negative x or y.
     * <br>
     * This is the same as calling {@link #sampleCircle(Vector2, float, float, float, float, int, Random)} with 10 points
     * per iteration and {@link MathUtils#random} as its Random instance. You can set the seed or state of
     * {@link MathUtils#random} to ensure deterministic results, or pass any instance of {@link Random} to one of the
     * overloads that takes one.
     * @param center the center of the circle to spray Vector2s into
     * @param radius the radius of the circle to spray Vector2s into
     * @param minimumDistance the minimum distance between Vector2s, in Euclidean distance as a float.
     * @param maxX higher than the highest x that can be assigned (the exclusive upper limit)
     * @param maxY higher than the highest y that can be assigned (the exclusive upper limit)
     * @return an OrderedMap of Vector2 keys to Array of Vector2 values (representing points the key connects to);
     *         keys will satisfy the minimum distance to other keys
     */
    public static OrderedMap<Vector2, Array<Vector2>> sampleCircle(Vector2 center, float radius, float minimumDistance,
                                                                   float maxX, float maxY)
    {
        return sampleCircle(center, radius, minimumDistance, maxX, maxY, 10, MathUtils.random);
    }

    /**
     * Get a group of Vector2s, each randomly positioned around the given center out to the given radius (measured with
     * Euclidean distance, so a true circle), but with the given minimum distance from any other Vector2 in the group.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param center the center of the circle to spray Vector2s into
     * @param radius the radius of the circle to spray Vector2s into
     * @param minimumDistance the minimum distance between Vector2s, in Euclidean distance as a float.
     * @param maxX higher than the highest x that can be assigned (the exclusive upper limit)
     * @param maxY higher than the highest y that can be assigned (the exclusive upper limit)
     * @param pointsPerIteration with small radii, this can be around 5; with larger ones, 30 is reasonable
     * @param rng a Random to use for all random sampling.
     * @return an OrderedMap of Vector2 keys to Array of Vector2 values (representing points the key connects to);
     *         keys will satisfy the minimum distance to other keys
     */
    public static OrderedMap<Vector2, Array<Vector2>> sampleCircle(Vector2 center, float radius, float minimumDistance,
                                                 float maxX, float maxY, int pointsPerIteration, Random rng)
    {
        int radius2 = Math.round(radius);
        return sample(center.cpy().add(-radius2, -radius2), center.cpy().add(radius2, radius2), radius, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }

    /**
     * Get a group of Vector2s, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Vector2 in the group.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * <br>
     * This is the same as calling {@link #sampleRectangle(Vector2, Vector2, float, int, Random)} with 10 points
     * per iteration and {@link MathUtils#random} as its Random instance. You can set the seed or state of
     * {@link MathUtils#random} to ensure deterministic results, or pass any instance of {@link Random} to one of the
     * overloads that takes one.
     * @param minPosition the Vector2 with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Vector2 with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Vector2s, in Euclidean distance as a float.
     * @return an OrderedMap of Vector2 keys to Array of Vector2 values (representing points the key connects to);
     *         keys will satisfy the minimum distance to other keys
     */
    public static OrderedMap<Vector2, Array<Vector2>> sampleRectangle(Vector2 minPosition, Vector2 maxPosition, float minimumDistance)
    {
        return sampleRectangle(minPosition, maxPosition, minimumDistance, maxPosition.x + 1f, maxPosition.y + 1f, 10, MathUtils.random);
    }

    /**
     * Get a group of Vector2s, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Vector2 in the group.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Vector2 with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Vector2 with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Vector2s, in Euclidean distance as a float.
     * @param pointsPerIteration with small areas, this can be around 5; with larger ones, 30 is reasonable
     * @param rng a Random to use for all random sampling.
     * @return an OrderedMap of Vector2 keys to Array of Vector2 values (representing points the key connects to);
     *         keys will satisfy the minimum distance to other keys
     */
    public static OrderedMap<Vector2, Array<Vector2>> sampleRectangle(
            Vector2 minPosition, Vector2 maxPosition, float minimumDistance, int pointsPerIteration, Random rng)
    {
        return sample(minPosition, maxPosition, 0f, minimumDistance, maxPosition.x + 1, maxPosition.y + 1, pointsPerIteration, rng);
    }

    /**
     * Get a group of Vector2s, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Vector2 in the group.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Vector2 with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Vector2 with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Vector2s, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @param pointsPerIteration with small areas, this can be around 5; with larger ones, 30 is reasonable
     * @param rng a Random to use for all random sampling.
     * @return an OrderedMap of Vector2 keys to Array of Vector2 values (representing points the key connects to);
     *         keys will satisfy the minimum distance to other keys
     */
    public static OrderedMap<Vector2, Array<Vector2>> sampleRectangle(Vector2 minPosition, Vector2 maxPosition, float minimumDistance,
                                                    float maxX, float maxY, int pointsPerIteration, Random rng)
    {
        return sample(minPosition, maxPosition, 0f, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }

    private static OrderedMap<Vector2, Array<Vector2>> sample(Vector2 minPos, Vector2 maxPos,
                                                                   float maxSampleRadius, float radius,
                                                                   float xBound, float yBound,
                                                                   int pointsPerTry, Random random) {
        radius = Math.max(1.0001f, radius);
        maxSampleRadius *= maxSampleRadius;
        final float radius2 = radius * radius;
        final float iCellSize = 1f / (radius * inverseRootTwo);
        final float ik = MathUtils.PI2 / pointsPerTry;
        final float width = maxPos.x - minPos.x + 1, height = maxPos.y - minPos.y + 1;
        final Vector2 gridCenter = minPos.cpy().add(maxPos);
        gridCenter.x *= 0.5f;
        gridCenter.y *= 0.5f;
        final int gridWidth = MathUtils.ceilPositive(Math.min(width * iCellSize, xBound));
        final int gridHeight = MathUtils.ceilPositive(Math.min(height * iCellSize, yBound));
        final float[][] gridX = new float[gridWidth][gridHeight];
        final float[][] gridY = new float[gridWidth][gridHeight];
        final FloatArray qx = new FloatArray(false, gridWidth + gridHeight);
        final FloatArray qy = new FloatArray(false, gridWidth + gridHeight);
        final OrderedMap<Vector2, Array<Vector2>> graph = new OrderedMap<>(8 + (int) (gridWidth * gridHeight * iCellSize));
        // Pick the first sample.
        graph.put(sample(width * 0.5f, height * 0.5f, iCellSize, qx, qy, gridX, gridY, minPos), new Array<Vector2>(4));

        // Pick a random existing sample from the queue.
        final Vector2 parent = new Vector2(0, 0);
        PICKING:
        while (qx.notEmpty()) {
            final int i = random.nextInt(qx.size);
            final float px = qx.get(i);
            final float py = qy.get(i);
            parent.set(px, py);
            float seed = random.nextFloat() * MathUtils.PI2;
            // Make a new candidate.
            for (int j = 0; j < pointsPerTry; j++) {
                final float x = px + radius * MathUtils.cos(seed);
                final float y = py + radius * MathUtils.sin(seed);
                seed += ik;

                // Accept candidates that are inside the allowed extent
                // and farther than 2 * radius to all existing samples.
                if (x >= minPos.x && x < maxPos.x + 0.99999994f && y >= minPos.y && y < maxPos.y + 0.99999994f && far(x, y, iCellSize, radius2,
                        gridCenter, maxSampleRadius, gridX, gridY, minPos)) {
                    final Vector2 sam = sample(x, y, iCellSize, qx, qy, gridX, gridY, minPos);
                    graph.get(parent).add(sam);
                    graph.put(sam, new Array<Vector2>(4));
                    continue PICKING;
                }
            }

            // If none of k candidates were accepted, remove it from the queue.
            qx.removeIndex(i);
            qy.removeIndex(i);
        }
        return graph;
    }
    private static boolean far(float x, float y, float iCellSize, float radius2, Vector2 gridCenter, float maxSampleRadius, float[][] gridX, float[][] gridY, Vector2 minPos){
        if(maxSampleRadius != 0f && gridCenter.dst2(x, y) > maxSampleRadius) return false;
        final float i = ((x - minPos.x) * iCellSize);
        final float j = ((y - minPos.y) * iCellSize);
        final int gridWidth = gridX.length;
        final float i0 = Math.max(i - 2, 0);
        final float j0 = Math.max(j - 2, 0);
        final float i1 = Math.min(i + 2, gridWidth);
        final float j1 = Math.min(j + 2, gridX[0].length);
        int xx, yy;
        for (float ic = i0; ic <= i1; ic++) {
            xx = (int)ic;
            for (float jc = j0; jc <= j1; jc++) {
                yy = (int)jc;
                float dx = gridX[xx][yy];
                if(dx >= 0){
                    dx -= x;
                    final float dy = gridY[xx][yy] - y;
                    dx = dx * dx + dy * dy;
                    if(dx < radius2) return false;
                }
            }
        }
        return true;
    }
    private static Vector2 sample(float x, float y, float invCellSize, FloatArray qx, FloatArray qy, float[][] gridX, float[][] gridY, Vector2 minPos){
        final int gx = (int)((x - minPos.x) * invCellSize), gy = (int)((y - minPos.y) * invCellSize);
        gridX[gx][gy] = x;
        gridY[gx][gy] = y;
        qx.add(x);
        qy.add(y);
        return new Vector2(x, y);
    }
}
