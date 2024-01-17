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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class NoiseVisualizer extends ApplicationAdapter {

    RawNoise[] noises = new RawNoise[]{
            new ValueNoise(1),
            new FoamNoise(1),
            new PerlinNoise(1),
            new SimplexNoise(1),
            new HoneyNoise(1),
    };
    int noiseIndex = 4;
    private int dim = 0; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 0;
    private float freq = 0x1p-4f;
    private ContinuousNoise noise = new ContinuousNoise(noises[noiseIndex], 1, freq, 0, 1);
    private ImmediateModeRenderer20 renderer;

    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;

    private InputAdapter input;
    
    private Viewport view;
    private float ctr = -256;
    private boolean keepGoing = true;
    
    private AnimatedGif gif;
    private Array<Pixmap> frames = new Array<>(256);

    public static float basicPrepare(float n)
    {
//        return Math.max(0f, n);
        return n * 0.5f + 0.5f;
    }

    public static float circleInPrepare(float n)
    {
//        return Math.max(0f, n);
        return Interpolation.circleIn.apply(n * 0.5f + 0.5f);
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        noise.setFractalType(ContinuousNoise.FBM);
        int[] gray256 = new int[256];
        for (int i = 0; i < 256; i++) {
            gray256[i] = i * 0x01010100 + 0xFF;
        }

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.WREN);
        gif.setDitherStrength(0.2f);
        gif.palette = new PaletteReducer(gray256);
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case BACKSLASH:
                        noise.stringDeserialize(Gdx.app.getClipboard().getContents());
                        break;
                    case W:
                        for (int c = 0; c < 256; c++) {
                            int w = 256, h = 256;
                            float halfW = (w-1) * 0.5f, halfH = (h-1) * 0.5f, inv = 1f / w;
                            float cDeg = c * (360f/256f), cSin = MathUtils.sinDeg(cDeg) * 24, cCos = MathUtils.cosDeg(cDeg) * 24;
                            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < w; x++) {
                                for (int y = 0; y < h; y++) {
                                    float color = basicPrepare(noise.getNoise(x, y, cSin, cCos));
                                    // fisheye-like effect:
//                                    float color = basicPrepare(noise.getNoise(x, y, c - inv * ((x - halfW) * (x - halfW) + (y - halfH) * (y - halfH))));
                                    p.setColor(color, color, color, 1f);
                                    p.drawPixel(x, y);
                                }
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();

                        String ser = noise.stringSerialize() + "_" + System.currentTimeMillis();
                        System.out.println(ser);
                        gif.write(Gdx.files.local("out/" + ser + ".gif"), frames, 16);
                        for (int i = 0; i < frames.size; i++) {
                            frames.get(i).dispose();
                        }
                        frames.clear();
                        break;
                    case P: //pause
                        keepGoing = !keepGoing;
                    case SPACE:
                        ctr += 1f/60f;
                        break;
                    case E: //earlier seed
                        noise.setSeed( noise.getSeed() - 1);
                        break;
                    case S: //seed
                        noise.setSeed( noise.getSeed() + 1);
                        break;
                    case SLASH:
                        noise.setSeed( Scramblers.scrambleInt(noise.getSeed()));
                        break;
                    case N: // noise type
                    case EQUALS:
                    case ENTER:
                        noise.setWrapped(noises[noiseIndex = (noiseIndex + (UIUtils.shift() ? noises.length - 1 : 1)) % noises.length]);
                        break;
                    case M:
                    case MINUS:
                        noise.setWrapped(noises[noiseIndex = (noiseIndex + noises.length - 1) % noises.length]);
                        break;
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 4 : 1)) % 5;
                        break;
                    case F: // frequency
//                        noise.setFrequency(NumberTools.sin(freq += 0.125f) * 0.25f + 0.25f + 0x1p-7f);
//                        noise.setFrequency((float) Math.exp((System.currentTimeMillis() >>> 9 & 7) - 5));
                        noise.setFrequency(freq *= (UIUtils.shift() ? 1.25f : 0.8f));
                        break;
                    case R: // fRactal type
                        noise.setFractalType((noise.getFractalType() + (UIUtils.shift() ? 3 : 1)) % 4);
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        break;
                    case K: // sKip
                        ctr += 1000;
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
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr * 16f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getNoise(x + c, y + c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getNoise(x, y, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    float xc = MathUtils.cosDeg(360 * x * iWidth) * 64 + c, xs = MathUtils.sinDeg(360 * x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = MathUtils.cosDeg(360 * y * iHeight) * 64 + c, ys = MathUtils.sinDeg(360 * y * iHeight) * 64 + c;
                        bright = basicPrepare(noise.getNoise(xc, yc, xs, ys));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 3: {
                for (int x = 0; x < width; x++) {
                    float xc = MathUtils.cosDeg(360 * x * iWidth) * 64, xs = MathUtils.sinDeg(360 * x * iWidth) * 64;
                    for (int y = 0; y < height; y++) {
                        float yc = MathUtils.cosDeg(360 * y * iHeight) * 64, ys = MathUtils.sinDeg(360 * y * iHeight) * 64;
                        bright = basicPrepare(noise.getNoise(xc, yc, xs, ys, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
            case 4: {
                for (int x = 0; x < width; x++) {
                    float xc = MathUtils.cosDeg(360 * x * iWidth) * 64 + c, xs = MathUtils.sinDeg(360 * x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = MathUtils.cosDeg(360 * y * iHeight) * 64 + c, ys = MathUtils.sinDeg(360 * y * iHeight) * 64 + c,
                                zc = MathUtils.cosDeg(360 * (x - y) * 0.5f * iWidth) * 64 - c, zs = MathUtils.sinDeg(360 * (x - y) * 0.5f * iWidth) * 64 - c;
                        bright = basicPrepare(noise.getNoise(xc, yc, zc, xs, ys, zs));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
        }
        renderer.end();

    }

    @Override
    public void render() {
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            // standard clear the background routine for libGDX
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            ctr += Gdx.graphics.getDeltaTime();
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
        config.setTitle("SquidSquad Test: Noise Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new NoiseVisualizer(), config);
    }
}
