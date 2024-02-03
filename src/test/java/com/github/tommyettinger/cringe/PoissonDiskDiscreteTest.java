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

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * A test for the randomized spraying of GridPoint2 values in the PoissonDiskDiscrete class.
 */
public class PoissonDiskDiscreteTest {

    public static void main(String[] args) {
        RandomAce320 rng = new RandomAce320();
        char[][] dun = new char[80][80];
        for (int y = 1; y < dun.length - 1; y++) {
            for (int x = 1; x < dun[y].length - 1; x++) {
                dun[y][x] = '.';
            }
        }
        for (int y = 0; y < dun.length; y++) {
            dun[y][0] = '#';
            dun[y][dun[y].length - 1] = '#';
        }
        for (int x = 1; x < dun.length - 1; x++) {
            dun[0][x] = '#';
            dun[dun.length-1][x] = '#';
        }

        OrderedMap<GridPoint2, Array<GridPoint2>> points = PoissonDiskDiscrete.sampleRectangle(
                new GridPoint2(1, 1), new GridPoint2(78, 78), 2.5f,
                80, 80, 30, rng);
        for (int i = 0; i < points.size; i++) {
            GridPoint2 c = points.orderedKeys().get(i);
            dun[c.x][c.y] = (char) ('0' + points.get(c).size);
        }
        System.out.println();
        for (int y = 0; y < dun.length; y++) {
            System.out.println(dun[y]);
        }
    }

}
