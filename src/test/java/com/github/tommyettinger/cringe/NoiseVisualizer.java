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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;
import static com.github.tommyettinger.cringe.ColorSupport.*;
import static com.github.tommyettinger.cringe.ContinuousNoise.*;

/**
 */
public class NoiseVisualizer extends ApplicationAdapter {

    public interface FloatToFloatFunction {
        float applyAsFloat(float f);
    }

    static {
        Serializer.register(new BadgerNoise(1));
        Serializer.register(new SnakeNoise(1));
        Serializer.register(new WigglyNoise(1, 3));
    }
    RawNoise[] noises = new RawNoise[]{
            new ValueNoise(1),
            new FoamNoise(1),
            new PerlinNoise(1),
            new SimplexNoise(1),
            new HoneyNoise(1),
            new CyclicNoise(1, 3),
            new SorbetNoise(1, 3),
            new BadgerNoise(1),
            new SnakeNoise(1),
            new WigglyNoise(1, 3),
    };
    int noiseIndex = noises.length - 1;
    private int dim = 0; // this can be 0, 1, 2, 3, 4, OR 5; add 1 to get the actual dimensions
    private int octaves = 1; // starts at 1
    private float freq = 0x1p-4f;
    private float mulRaw = 1f, mul = RoughMath.pow2Rough(mulRaw);
    private float mixRaw = 0f, mix = RoughMath.logisticRough(mixRaw);
    private float biasRaw = 0f, bias = RoughMath.pow2Rough(mixRaw);
    private final ContinuousNoise noise = new ContinuousNoise(noises[noiseIndex], 1, freq, 0, octaves);
    private ImmediateModeRenderer20 renderer;

    private static final int width = 512, height = 512;
//    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;

    private Viewport view;
    private float ctr = -256;
    private boolean keepGoing = true;

    private final Color color = new Color(Color.WHITE);//new Color(Color.LIME);
    private final Color tempColor = new Color(Color.WHITE);
    private float hue = hue(color), sat = saturation(color), lit = lightness(color);
    
    private AnimatedGif gif;
    private final Array<Pixmap> frames = new Array<>(1024);

    public float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }
    public float redistributedPrepare(float n)
    {
        return RawNoise.redistribute(n, mul, mix, bias) * 0.5f + 0.5f;
    }

    private final FloatToFloatFunction[] PREPARATIONS = {this::basicPrepare, this::redistributedPrepare};
    private int preparationIndex = 0;
    private FloatToFloatFunction prepare = PREPARATIONS[preparationIndex];

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        noise.setFractalType(FBM);
        int[] gray256 = new int[256];
        for (int i = 0; i < 256; i++) {
            gray256[i] = i * 0x01010100 + 0xFF;
        }

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.WREN);
        gif.setDitherStrength(0.2f);
        gif.palette = new PaletteReducer(gray256);

        InputAdapter input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                FileHandle file;
                switch (keycode) {
                    case C: // copy out
                        String seri = noise.stringSerialize() + "_" + System.currentTimeMillis();
                        System.out.println("Copying data:\n" + seri);
                        Gdx.app.getClipboard().setContents(seri);
                        break;
                    case V: // paste in
                        String pasted = Gdx.app.getClipboard().getContents();
                        System.out.println("Pasting in data:\n" + pasted);
                        if (pasted != null)
                        {
                            try {
                                noise.stringDeserialize(pasted);
                            } catch (Exception ignored){
                            }
                        }
                        break;
                    case G:
                        for (int c = 0; c < 1024; c++) {
                            Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                            p.setColor(Color.BLACK);
                            p.fill();
                            float cc = c * 0.2f;
                            for (int d = 4096; d < 10000; d++) {
                                float da = d * 0.05f;
                                float x = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.7548776662466927f + cc, noise.seed)) * da * width   * 0x1p-9f;
                                float y = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.5698402909980532f + cc, ~noise.seed)) * da * height * 0x1p-9f;
                                float bright = d / 9999f;
                                ColorSupport.hsl2rgb(tempColor, hue + bright + cc * 0.02f, 1f, (bright - 0.2f) * 0.3f, 1f);
                                p.setColor(tempColor);
                                p.drawCircle((int) x, (int)y, 3);
                            }
                            for (int d = 4096; d < 10000; d++) {
                                float da = d * 0.05f;
                                float x = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.7548776662466927f + cc, noise.seed)) * da * width   * 0x1p-9f;
                                float y = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.5698402909980532f + cc, ~noise.seed)) * da * height * 0x1p-9f;
                                float bright = d / 9999f;
                                ColorSupport.hsl2rgb(tempColor, hue + bright + cc * 0.02f, 1f, (bright - 0.2f) * 0.55f, 1f);
                                p.setColor(tempColor);
                                p.drawCircle((int) x, (int)y, 2);
                            }
                            for (int d = 4096; d < 10000; d++) {
                                float da = d * 0.05f;
                                float x = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.7548776662466927f + cc, noise.seed)) * da * width   * 0x1p-9f;
                                float y = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.5698402909980532f + cc, ~noise.seed)) * da * height * 0x1p-9f;
                                float bright = d / 9999f;
                                ColorSupport.hsl2rgb(tempColor, hue + bright + cc * 0.02f, 1f, (bright - 0.2f), 1f);
                                p.setColor(tempColor);
                                p.drawPixel((int) x, (int)y);
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();

                        file = Gdx.files.local("out/" + noise.stringSerialize() + "_" + System.currentTimeMillis() + "_1D.gif");
                        System.out.println("Writing to file:\n" + file);
                        gif.setFastAnalysis(true);
                        gif.palette.analyze(frames);
                        gif.write(file, frames, 60);
                        for (int i = 0; i < frames.size; i++) {
                            frames.get(i).dispose();
                        }
                        frames.clear();
                    break;
                    case W:
                        if(dim == 0) {
                            for (int c = 0; c < 1024; c++) {
                                Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                                p.setColor(Color.BLACK);
                                p.fill();
                                float cc = c * 0.2f;
                                for (int d = 4096; d < 10000; d++) {
                                    float da = d * 0.05f;
                                    float x = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.7548776662466927f + cc, noise.seed)) * da * width   * 0x1p-9f;
                                    float y = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.5698402909980532f + cc, ~noise.seed)) * da * height * 0x1p-9f;
                                    float bright = d / 9999f;
                                    ColorSupport.hsl2rgb(tempColor, hue + bright + cc * 0.02f, 1f, (bright - 0.2f), 1f);
                                    p.setColor(tempColor);
                                    p.drawCircle((int) x, (int)y, 2);
                                }
                                frames.add(p);
                            }
                            Gdx.files.local("out/").mkdirs();

                            file = Gdx.files.local("out/" + noise.stringSerialize() + "_" + System.currentTimeMillis() + "_1D.gif");
                            System.out.println("Writing to file:\n" + file);
                            gif.setFastAnalysis(true);
                            gif.palette.analyze(frames);
                            gif.write(file, frames, 60);
                            for (int i = 0; i < frames.size; i++) {
                                frames.get(i).dispose();
                            }
                            frames.clear();
                        } else {
                            Color t = tempColor;
                            for (int i = 1; i < 256; i++) {
                                t.set(color).mul(i / 255f);
                                gif.palette.paletteArray[i] = Color.rgba8888(t);
                            }
                            for (int c = 0; c < 256; c++) {
                                int w = 256, h = 256;
//                            float halfW = (w-1) * 0.5f, halfH = (h-1) * 0.5f, inv = 1f / w;
                                float cDeg = c * (360f / 256f), cSin = MathUtils.sinDeg(cDeg) * 40, cCos = MathUtils.cosDeg(cDeg) * 40;
                                Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                                for (int x = 0; x < w; x++) {
                                    for (int y = 0; y < h; y++) {
                                        float color = prepare.applyAsFloat(noise.getNoiseWithSeed(x, y, cSin, cCos, noise.seed));
                                        // fisheye-like effect:
//                                    float color = prepare.applyAsFloat(noise.getNoiseWithSeed(x, y, c - inv * ((x - halfW) * (x - halfW) + (y - halfH) * (y - halfH)), noise.seed));
                                        p.setColor(color, color, color, 1f);
                                        p.drawPixel(x, y);
                                    }
                                }
                                frames.add(p);
                            }
                            Gdx.files.local("out/").mkdirs();

                            file = Gdx.files.local("out/" + noise.stringSerialize() + "_" + System.currentTimeMillis() + ".gif");
                            System.out.println("Writing to file:\n" + file);
                            gif.write(file, frames, 16);
                            for (int i = 0; i < frames.size; i++) {
                                frames.get(i).dispose();
                            }
                            frames.clear();
                        }
                        break;
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
                        noise.setSeed(noise.getSeed() - 1);
                        System.out.println("Seed is now " + noise.getSeed());
                        break;
                    case S: //seed
                        noise.setSeed(noise.getSeed() + 1);
                        System.out.println("Seed is now " + noise.getSeed());
                        break;
                    case SLASH:
                        noise.setSeed(Scramblers.scrambleInt(noise.getSeed()));
                        System.out.println("Seed is now " + noise.getSeed());
                        break;
                    case N: // noise type
                    case EQUALS: // (also plus)
                    case ENTER:
                        noise.setWrapped(noises[noiseIndex = (noiseIndex + (UIUtils.shift() ? noises.length - 1 : 1)) % noises.length]);
                        System.out.println("Switched to " + noises[noiseIndex].getTag());
                        break;
                    case MINUS:
                        noise.setWrapped(noises[noiseIndex = (noiseIndex + noises.length - 1) % noises.length]);
                        System.out.println("Switched to " + noises[noiseIndex].getTag());
                        break;
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 5 : 1)) % 6;
                        System.out.println("Now producing " + (dim + 1) + "-dimensional noise.");
                        break;
                    case F: // frequency
                        noise.setFrequency(freq *= (UIUtils.shift() ? 1.25f : 0.8f));
                        System.out.println("Frequency is now " + freq);
                        break;
                    case R: // fRactal type/mode
                        noise.setFractalType((noise.getFractalType() + (UIUtils.shift() ? 3 : 1)) % 4);
                        switch (noise.getFractalType()) {
                            case FBM:
                                System.out.println("Fractal Type/Mode is now FBM");
                                break;
                            case BILLOW:
                                System.out.println("Fractal Type/Mode is now BILLOW");
                                break;
                            case RIDGED:
                                System.out.println("Fractal Type/Mode is now RIDGED");
                                break;
                            case WARP:
                                System.out.println("Fractal Type/Mode is now WARP");
                                break;
                        }
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        System.out.println("Using " + (octaves + 1) + " octaves");
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        System.out.println("Using " + (octaves + 1) + " octaves");
                        break;
                    case P:
                        prepare = PREPARATIONS[preparationIndex = (preparationIndex + (UIUtils.shift() ? PREPARATIONS.length - 1 : 1)) % PREPARATIONS.length];
                        System.out.println("Switched to prepare function #" + preparationIndex);
                        break;
                    case O:
                        System.out.println("mulRaw: " + mulRaw + ", mul: " + mul +
                                ", mixRaw: " + mixRaw + ", mix: " + mix + ", biasRaw: " + biasRaw + ", bias: " + bias);
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
        if(Gdx.input.isKeyPressed(LEFT)) {
            hue -= Gdx.graphics.getDeltaTime() * 0.25f;
            hue -= MathUtils.floor(hue);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        }
        else if(Gdx.input.isKeyPressed(RIGHT)) {
            hue += Gdx.graphics.getDeltaTime() * 0.25f;
            hue -= MathUtils.floor(hue);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        }
        else if(Gdx.input.isKeyPressed(DOWN)) {
            lit = Math.max(0f, lit - Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        }
        else if(Gdx.input.isKeyPressed(UP)) {
            lit = Math.min(1f, lit + Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        }
        else if(Gdx.input.isKeyPressed(LEFT_BRACKET)) {
            sat = Math.max(0f, sat - Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        }
        else if(Gdx.input.isKeyPressed(RIGHT_BRACKET)) {
            sat = Math.min(1f, sat + Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        }
        else if(Gdx.input.isKeyPressed(I)) {
            mixRaw += Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? 0.25f : -0.25f);
            mix = RoughMath.logisticRough(mixRaw);
        }
        else if(Gdx.input.isKeyPressed(U)) {
            mulRaw += Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? 0.25f : -0.25f);
            mul = RoughMath.pow2Rough(mulRaw);
        }
        else if(Gdx.input.isKeyPressed(Y)) {
            biasRaw += Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? 0.25f : -0.25f);
            bias = RoughMath.pow2Rough(biasRaw);
        }



        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr * 16f;
        switch (dim) {
            case 0:
                c *= 0.5f;
                // c and ctr are both about the same counter, they advance by a tiny float every frame.
                for (int d = 4096; d < 8192; d++) {
                    float da = d * 0.0625f; // 1/16f
                    float x = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.7548776662466927f + c, noise.seed)) * da * width   * 0x1p-9f;
                    float y = prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.5698402909980532f + c, ~noise.seed)) * da * height * 0x1p-9f;
                    bright = d / 8191f; // takes bright into the 0.5 to 1.0 range, roughly
                    // this rotates hue over time and as bright changes (so, as the current dot, d, changes).
                    // saturation is always vivid, so 1, and the lightness gets brighter towards newer d (higher d).
                    ColorSupport.hsl2rgb(tempColor, hue + bright + ctr * 0.3f, 1f, bright - 0.2f, 1f);
                    renderer.color(tempColor.r, tempColor.g, tempColor.b, 1f);
                    renderer.vertex(x, y, 0);
                }
//                for (int x = 0; x < width; x++) {
//                    for (int y = 0; y < height; y++) {
//                        bright = prepare.applyAsFloat(noise.getNoiseWithSeed(Math.abs(x - width * 0.5f) + Math.abs(y - height * 0.5f) - c, noise.seed));
//                        renderer.color(color.r * bright, color.g * bright, color.b * bright, 1f);
//                        renderer.vertex(x, y, 0);
//                    }
//                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = prepare.applyAsFloat(noise.getNoiseWithSeed(x + c, y + c, noise.seed));
                        renderer.color(color.r * bright, color.g * bright, color.b * bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = prepare.applyAsFloat(noise.getNoiseWithSeed(x, y, c, noise.seed));
                        renderer.color(color.r * bright, color.g * bright, color.b * bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 3:
                for (int x = 0; x < width; x++) {
                    float xc = MathUtils.cosDeg(360 * x * iWidth) * 64 + c, xs = MathUtils.sinDeg(360 * x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = MathUtils.cosDeg(360 * y * iHeight) * 64 + c, ys = MathUtils.sinDeg(360 * y * iHeight) * 64 + c;
                        bright = prepare.applyAsFloat(noise.getNoiseWithSeed(xc, yc, xs, ys, noise.seed));
                        renderer.color(color.r * bright, color.g * bright, color.b * bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 4: {
                for (int x = 0; x < width; x++) {
                    float xc = MathUtils.cosDeg(360 * x * iWidth) * 64, xs = MathUtils.sinDeg(360 * x * iWidth) * 64;
                    for (int y = 0; y < height; y++) {
                        float yc = MathUtils.cosDeg(360 * y * iHeight) * 64, ys = MathUtils.sinDeg(360 * y * iHeight) * 64;
                        bright = prepare.applyAsFloat(noise.getNoiseWithSeed(xc, yc, xs, ys, c, noise.seed));
                        renderer.color(color.r * bright, color.g * bright, color.b * bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
            case 5: {
                for (int x = 0; x < width; x++) {
                    float xc = MathUtils.cosDeg(360 * x * iWidth) * 64 + c, xs = MathUtils.sinDeg(360 * x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = MathUtils.cosDeg(360 * y * iHeight) * 64 + c, ys = MathUtils.sinDeg(360 * y * iHeight) * 64 + c,
                                zc = MathUtils.cosDeg(360 * (x - y) * 0.5f * iWidth) * 64 - c, zs = MathUtils.sinDeg(360 * (x - y) * 0.5f * iWidth) * 64 - c;
                        bright = prepare.applyAsFloat(noise.getNoiseWithSeed(xc, yc, zc, xs, ys, zs, noise.seed));
                        renderer.color(color.r * bright, color.g * bright, color.b * bright, 1f);
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
            ScreenUtils.clear(Color.BLACK);
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
