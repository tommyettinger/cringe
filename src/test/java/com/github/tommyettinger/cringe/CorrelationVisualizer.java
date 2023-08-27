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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class CorrelationVisualizer extends ApplicationAdapter {

    public String title = "";
    public BitmapFont font;
    public SpriteBatch fontBatch;
    private ImmediateModeRenderer20 renderer;
    public static final int width = 256, height = 256;
    private final float[][] previousGrid = new float[width][height];

    public final GdxRandom[][][] randoms;
    {
        GdxRandom[] rl = new GdxRandom[]{
                new RandomAce320(1), new RandomDistinct64(1), new RandomXMX256(1),
                new RandomRandomXS128(1), new RandomJava48(1)
        };
        randoms = new GdxRandom[rl.length][][];
        for (int i = 0; i < randoms.length; i++) {
            randoms[i] = makeGrid(rl[i], width, height);
        }
    }
    public int currentRandom = 0;
    public int randomCount = randoms.length;
    public int currentMode = 0;
    public int frame = 0;
    public int modeCount = 3;

    public GdxRandom[][] makeGrid(GdxRandom base, int width, int height){
        GdxRandom[][] g = new GdxRandom[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                g[x][y] = base.copy();
                g[x][y].setState(x << 16 ^ y);
//                switch (g[x][y].getStateCount()) {
//                    case 1:
//                        g[x][y].setState(x << 16 ^ y);
//                        break;
//                    case 2:
//                        g[x][y].setState(x, y);
//                        break;
//                    case 3:
//                        g[x][y].setState(x, y, 1L);
//                        break;
//                    case 4:
//                        g[x][y].setState(x, y, 1L, 1L);
//                        break;
//                    case 5:
//                        g[x][y].setState(x, y, 1L, 1L, 1L);
//                        break;
//                }
            }
        }
        return g;
    }

    public void refreshGrid() {
        for (int i = 0, n = randoms.length; i < n; i++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    randoms[i][x][y].setState(x << 16 ^ y);
//                    switch (randoms[i][x][y].getStateCount()) {
//                        case 1:
//                            break;
//                        case 2:
//                            randoms[i][x][y].setState(x, y);
//                            break;
//                        case 3:
//                            randoms[i][x][y].setState(x, y, 1L);
//                            break;
//                        case 4:
//                            randoms[i][x][y].setState(x, y, 1L, 1L);
//                            break;
//                        case 5:
//                            randoms[i][x][y].setState(x, y, 1L, 1L, 1L);
//                            break;
//                    }
                }
            }
        }
        frame = 0;
    }

    public void seedGrid() {
        for (int i = 0, n = randoms.length; i < n; i++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    randoms[i][x][y].setSeed(x << 16 ^ y);
                }
            }
        }
        frame = 0;
    }

    private Viewport view;
    private boolean keepGoing = true;

    @Override
    public void create() {
        font = new BitmapFont();
        fontBatch = new SpriteBatch();
        title = randoms[currentRandom][0][0].getClass().getSimpleName() + " on mode " + currentMode;
        System.out.println(title);
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        InputAdapter input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case SPACE:
                    case P: // pause
                        keepGoing = !keepGoing;
                        break;
                    case S: // step
                        System.out.println("Frame " + frame);
                        putMap();
                        break;
                    case V: // vertical
                        System.out.println("Viewing column 0:");
                        System.out.println(randoms[currentRandom][0][0]);
                        System.out.println(randoms[currentRandom][0][1]);
                        System.out.println(randoms[currentRandom][0][2]);
                        System.out.println(randoms[currentRandom][0][3]);
                        break;
                    case H: // horizontal
                        System.out.println("Viewing row 0:");
                        System.out.println(randoms[currentRandom][0][0]);
                        System.out.println(randoms[currentRandom][1][0]);
                        System.out.println(randoms[currentRandom][2][0]);
                        System.out.println(randoms[currentRandom][3][0]);
                        break;
                    case NUM_1:
                    case NUMPAD_1:
                        seedGrid();
                        putMap();
                        break;
                    case N: // next
                    case EQUALS:
                    case ENTER:
                        currentRandom = ((currentRandom + (UIUtils.shift() ? randomCount - 1 : 1)) % randomCount);
                        refreshGrid();
                        title = randoms[currentRandom][0][0].getClass().getSimpleName()
                                + " on mode " + currentMode;
                        System.out.println(title);
                        if (!keepGoing) putMap();
                        break;
                    case M: // mode
                        currentMode = ((currentMode + (UIUtils.shift() ? modeCount - 1 : 1)) % modeCount);
                        refreshGrid();
                        title = randoms[currentRandom][0][0].getClass().getSimpleName()
                                + " on mode " + currentMode;
                        System.out.println(title);
                        if (!keepGoing) putMap();
                        break;
                    case Q: // quit
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        ++frame;
        renderer.begin(view.getCamera().combined, GL_POINTS);
        int bt;
        switch (currentMode) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bt = (int) randoms[currentRandom][x][y].nextLong() & 255;
                        renderer.color(previousGrid[x][y] = NumberUtils.intBitsToFloat(0xFE000000 | bt << 16 | bt << 8 | bt));
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bt = -((int) randoms[currentRandom][x][y].nextLong() & 1) >>> 8;
                        renderer.color(previousGrid[x][y] = NumberUtils.intBitsToFloat(0xFE000000 | bt));
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bt = -(int) (randoms[currentRandom][x][y].nextLong() >>> 63) >>> 8;
                        renderer.color(previousGrid[x][y] = NumberUtils.intBitsToFloat(0xFE000000 | bt));
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
        }
        renderer.end();
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS showing " + title);
        fontBatch.begin();
        font.draw(fontBatch, title, 0f, height + 19f, width, Align.center, false);
        fontBatch.end();
        if (keepGoing) {
            putMap();
        }
        else {
            renderer.begin(view.getCamera().combined, GL_POINTS);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    renderer.color(previousGrid[x][y]);
                    renderer.vertex(x, y, 0);
                }
            }
            renderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.setResizable(false);
        config.setWindowedMode(width, height + 20);
        config.disableAudio(true);
        new Lwjgl3Application(new CorrelationVisualizer(), config);
    }
}
