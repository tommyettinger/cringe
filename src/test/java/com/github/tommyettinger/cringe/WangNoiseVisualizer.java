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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.badlogic.gdx.Input.Keys.*;

/**
 */
public class WangNoiseVisualizer extends ApplicationAdapter {

    public interface FloatToFloatFunction {
        float applyAsFloat(float f);
    }

    String[] noises = {
            "WangPerlinNoise",
            "WangPerlueNoise",
            "WangValueNoise",
    };

    int noiseIndex = 0;
    int fractalMode = 0;

    int seed = 0;

    private int octaves = 0; // starts at 0, goes up to 2 max

    private final TextureRegion[][][][] wangTiles = new TextureRegion[3][3][3][16];
    private final TextureRegion[][][][] omniTiles = new TextureRegion[3][3][3][64];
    private SpriteBatch renderer;

    private static final int width = 1024, height = 768;

    private Viewport view;
    private float ctr = -256;
    private boolean keepGoing = true;
    private boolean omni = false;

    @Override
    public void create() {
        renderer = new SpriteBatch(256);
        view = new ScreenViewport();

        for (int i = 0; i < 3; i++) {
            String type = noises[i];
            for (int j = 0; j < 3; j++) {
                int oct = j + 1;
                for (int k = 0; k < 3; k++) {
                    String mode = WangContinuousNoise.MODES[k];
                    Texture wang = new Texture(Gdx.files.local("docs/noiseWang/" + type + "_" + oct + "_" + mode + "_Wang.png"));
                    Texture omni = new Texture(Gdx.files.local("docs/noiseOmni/" + type + "_" + oct + "_" + mode + "_Omni.png"));
                    for (int l = 0; l < 16; l++) {
                        wangTiles[i][j][k][l] = new TextureRegion(wang, (l & 3) << 7, (l >>> 2 & 3) << 7, 128, 128);
                    }
                    for (int l = 0; l < 64; l++) {
                        omniTiles[i][j][k][l] = new TextureRegion(omni, (l & 7) << 7, (l >>> 3 & 7) << 7, 128, 128);
                    }
                }
            }
        }

        InputAdapter input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                FileHandle file;
                switch (keycode) {
                    case SPACE: //pause
                        keepGoing = !keepGoing;
                        if (keepGoing)
                            System.out.println("Now playing");
                        else
                            System.out.println("Now paused");
                        break;
                    case COMMA: // step, step, step
                        ctr += 1f / 60f;
                        System.out.println("Stepping ahead so ctr is " + ctr);
                        break;
                    case E: //earlier seed
                        seed--;
                        System.out.println("Seed is now " + seed);
                        break;
                    case S: //seed
                        seed++;
                        System.out.println("Seed is now " + seed);
                        break;
                    case SLASH:
                        seed = Scramblers.scrambleInt(seed);
                        System.out.println("Seed is now " + seed);
                        break;
                    case O:
                        omni = !omni;
                        if(omni) System.out.println("Using omni-tiling mode.");
                        else  System.out.println("Using Wang-tiling mode.");
                        break;
                    case N: // noise type
                    case EQUALS: // (also plus)
                    case RIGHT:
                    case ENTER:
                        noiseIndex = (noiseIndex + (UIUtils.shift() ? noises.length - 1 : 1)) % noises.length;
                        System.out.println("Switched to " + noises[noiseIndex]);
                        break;
                    case LEFT:
                    case MINUS:
                        noiseIndex = (noiseIndex + noises.length - 1) % noises.length;
                        System.out.println("Switched to " + noises[noiseIndex]);
                        break;
                    case R: // fRactal type/mode
                        fractalMode = ((fractalMode + (UIUtils.shift() ? 2 : 1)) % 3);
                        System.out.println("Fractal mode is now " + WangContinuousNoise.MODES[fractalMode]);
                        break;
                    case H: // higher octaves
                    case UP:
                        octaves = (octaves + 1) % 3;
                        System.out.println("Using " + (octaves + 1) + " octaves");
                        break;
                    case DOWN:
                    case L: // lower octaves
                        octaves = (octaves + 2) % 3;
                        System.out.println("Using " + (octaves + 1) + " octaves");
                        break;
                    case K: // sKip
                        ctr += 1000;
                        System.out.println("Skipping ahead so ctr is " + ctr);
                        break;
                    case Q:
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
        renderer.begin();
        int offsetPx = (int)(ctr), offsetGrid = offsetPx >> 7;
        if (omni) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 8; y++) {
                    // Using Cantor's pairing function means the same two values won't occur next to each other for
                    // a very long time. It forms right triangles of expanding numbers, and wraps when it hits 64.
//                    int index = WangTiler.selectSingleEdgeAlternate(x + offsetGrid, y + offsetGrid, seed);
                    // Rosenberg-Strong pairing function, an alternative to Cantor, defined for non-negative x and y.
                    int index = WangTiler.selectSingleEdgeSeparated(x + offsetGrid, y + offsetGrid, seed);
                    renderer.draw(omniTiles[noiseIndex][octaves][fractalMode][index], x * 128f - 128f - (offsetPx & 127), y * 128f - 128f - (offsetPx & 127));
                    // This is an alternative that doesn't keep the same values away from each other.
                    // Repeats will happen.
//                    renderer.draw(omniTiles[noiseIndex][octaves][fractalMode][PointHasher.hash256(x, y, seed) & 63], x * 128f, y * 128f);
                }
            }
        }
        else {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 8; y++) {
                    int index = WangTiler.select(x + offsetGrid, -y - offsetGrid, seed);
                    renderer.draw(wangTiles[noiseIndex][octaves][fractalMode][index], x * 128f - 128f - (offsetPx & 127), y * 128f - 128f - (offsetPx & 127));
                }
            }
        }
        renderer.end();
    }

    @Override
    public void render() {
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            ScreenUtils.clear(Color.BLACK);
            ctr += Gdx.graphics.getDeltaTime() * 30f;
            putMap();
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
        config.setTitle("Cringe Test: Wang Noise Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new WangNoiseVisualizer(), config);
    }
}
