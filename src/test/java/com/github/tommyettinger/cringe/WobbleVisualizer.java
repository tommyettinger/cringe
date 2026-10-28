/*
 * Copyright (c) 2026 See AUTHORS file.
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
import static com.github.tommyettinger.cringe.NoiseVisualizer.xFreq;
import static com.github.tommyettinger.cringe.NoiseVisualizer.yFreq;

/**
 */
public class WobbleVisualizer extends ApplicationAdapter {

    public interface FloatToFloatFunction {
        float applyAsFloat(float f);
    }

    RawNoise[] noises = new RawNoise[]{
            /*  0 */ new LineWobble.BicubicWobbleNoise(1),
            /*  1 */ new LineWobble.SmoothWobbleNoise(1),
            /*  2 */ new LineWobble.SplineWobbleNoise(1),
            /*  3 */ new LineWobble.TrigWobbleNoise(1),
            /*  4 */ new LineWobble.WobbleNoise(1),
    };
    int noiseIndex = 0;
    private int octaves = 1; // starts at 1
    private float freq = 0x1p-4f;
    private float mulRaw = 1f, mul = RoughMath.pow2Rough(mulRaw);
    private float mixRaw = 0f, mix = RoughMath.logisticRough(mixRaw);
    private float biasRaw = 0f, bias = RoughMath.pow2Rough(mixRaw);
    private final ContinuousNoise noise = new ContinuousNoise(noises[noiseIndex], 1, freq, FBM, octaves);
    private ImmediateModeRenderer20 renderer;

    private static final int width = 512, height = 512;


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

    public float fbm(float x, final int seed) {
        float totalPower = (1 << octaves) - 1f, accrued = 0f, frequencyChange = 1 << (octaves - 1);
        //0x91E10DA5, 0xD1B54A35, and 0xA0F2EC75 are all sections of numbers from MathTools, in some R* sequence.
        //0x142543 is a subsequence of the bits of 0xd1342543de82ef95, which was found to be a strong LCG multiplier.
        //XOR with a constant that ends in the bits 101, then multiplying by a constant that ends in the bits 011, is
        //called an XLCG sometimes, and it is comparable to a normal LCG but has advantages on GWT. Note that the
        //low-order bits of an (X)LCG are trash, but for generating floats it doesn't matter; they are discarded.
        //0x1.9E3779B9p-31f is the golden ratio divided by (2 to the 31).
        float slide = (((seed ^ 0x91E10DA5) * 0x142543 ^ 0xD1B54A35) * 0x142543 ^ 0xA0F2EC75) * 0x1.9E3779B9p-31f + 0.25f;
//        float slide = (((seed ^ (seed << 19 | seed >>> 13) ^ (seed << 5 | seed >>> 27)) * 0x1D2BC3 ^ 0xD1B54A35) * 0x1D2BC3 ^ 0xD1B54A35) * 0x1p-31f;
        int power = 1;
        for (int i = octaves; i > 0; i--, power += power, frequencyChange *= 0.5f) {
            accrued += noise.wrapped.getNoiseWithSeed(x * frequencyChange + (slide *= 1.6180339887498949f), seed ^ 0x9E3779B9 * octaves) * power;
        }
        return accrued / totalPower;
    }


    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        int[] gray256 = new int[256];
        for (int i = 0; i < 256; i++) {
            gray256[i] = i * 0x01010100 + 0xFF;
        }

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.LOAF);
        gif.setDitherStrength(0.25f);
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
                        if (pasted != null) {
                            try {
                                noise.stringDeserialize(pasted);
                            } catch (Exception ignored) {
                            }
                        }
                        break;
                    case G:
                        for (int c = 0; c < 1024; c++) {
                            Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                            p.setColor(Color.BLACK);
                            p.fill();
                            float cc = c * 0.2f, ccc = cc * 0.02f;
//                            for (int d = 4096; d < 10000; d++) {
//                                float da = d * 0.05f;
//                                float bright = d / 9999f;
//                                float x = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.7548776662466927f + cc, noise.getSeed())) - 0.5f) * bright * width + width * 0.5f;
//                                float y = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * 0.5698402909980532f + cc, ~noise.getSeed())) - 0.5f) * bright * height + height * 0.5f;
//                                ColorSupport.hsb2rgb(tempColor, hue + bright + ccc, 1f, bright * 0.6f - 0.2f, 1f);
//                                p.setColor(tempColor);
//                                p.fillCircle((int) x, (int) y, 3);
//                            }
                            for (int d = 4096; d < 10000; d++) {
                                float da = d * 0.05f;
                                float bright = d / 9999f;
//                                float x = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * xFreq + cc, noise.getSeed())) - 0.5f) * bright * width + width * 0.5f;
//                                float y = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * yFreq + cc, ~noise.getSeed())) - 0.5f) * bright * height + height * 0.5f;
                                float x = ((fbm(da * xFreq + cc, noise.getSeed())) * 0.5f) * bright * width + width * 0.5f;
                                float y = ((fbm(da * yFreq + cc, ~noise.getSeed())) * 0.5f) * bright * height + height * 0.5f;
                                ColorSupport.hsb2rgb(tempColor, hue + bright + ccc, 1f, bright * 0.9f - 0.2f, 1f);
                                p.setColor(tempColor);
                                p.fillCircle((int) x, (int) y, 2);
                            }
                            for (int d = 4096; d < 10000; d++) {
                                float da = d * 0.05f;
                                float bright = d / 9999f;
//                                float x = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * xFreq + cc, noise.getSeed())) - 0.5f) * bright * width + width * 0.5f;
//                                float y = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * yFreq + cc, ~noise.getSeed())) - 0.5f) * bright * height + height * 0.5f;
                                float x = ((fbm(da * xFreq + cc, noise.getSeed())) * 0.5f) * bright * width + width * 0.5f;
                                float y = ((fbm(da * yFreq + cc, ~noise.getSeed())) * 0.5f) * bright * height + height * 0.5f;
                                ColorSupport.hsb2rgb(tempColor, hue + bright + ccc, 1f, Math.min(1f, bright * 1.4f), 1f);
                                p.setColor(tempColor);
                                p.drawPixel((int) x, (int) y);
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();

                        file = Gdx.files.local("out/" + noise.stringSerialize() + "_" + System.currentTimeMillis() + "_1D.gif");
                        System.out.println("Writing to file:\n" + file);
                        gif.palette.analyze(frames);
                        gif.write(file, frames, 60);
                        for (int i = 0; i < frames.size; i++) {
                            frames.get(i).dispose();
                        }
                        frames.clear();
                        break;
                    case W:
                        // c is counter, it's our time, effectively, but also the current frame.
                        for (int c = 0; c < 1024; c++) {
                            Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                            p.setColor(Color.BLACK);
                            p.fill();
                            // slower change than the counter
                            float cc = c * 0.2f, ccc = ctr * 0.3f;
                            // roughly 6K points per frame to form the curvy line
                            for (int d = 4096; d < 10000; d++) {
                                // d adjustment; applies to both x and y
                                float da = d * 0.05f;
                                // never more dim than about 0.41 lightness
                                float bright = d / 9999f;
                                // x and y are independent 1D noise calls with different seeds and frequencies
//                                float x = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * xFreq + cc, noise.getSeed())) - 0.5f) * bright * width + width * 0.5f;
//                                float y = (prepare.applyAsFloat(noise.getNoiseWithSeed(da * yFreq + cc, ~noise.getSeed())) - 0.5f) * bright * height + height * 0.5f;
                                float x = ((fbm(da * xFreq + cc, noise.getSeed())) * 0.5f) * bright * width + width * 0.5f;
                                float y = ((fbm(da * yFreq + cc, ~noise.getSeed())) * 0.5f) * bright * height + height * 0.5f;

                                ColorSupport.hsb2rgb(tempColor, hue + bright + ccc, 1f, bright * 1.2f - 0.2f, 1f);
                                p.setColor(tempColor);
                                p.fillCircle((int) x, (int) y, 2);
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();

                        file = Gdx.files.local("out/" + noise.stringSerialize() + "_" + System.currentTimeMillis() + "_1D.gif");
                        System.out.println("Writing to file:\n" + file);
                        gif.palette.analyze(frames);
                        gif.write(file, frames, 60);
                        for (int i = 0; i < frames.size; i++) {
                            frames.get(i).dispose();
                        }
                        frames.clear();
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
                    case F: // frequency
                        noise.setFrequency(freq *= (UIUtils.shift() ? 1.25f : 0.8f));
                        System.out.println("Frequency is now " + freq);
                        break;
                    case R: // fRactal type/mode
                        noise.setFractalType((noise.getFractalType() + (UIUtils.shift() ? 4 : 1)) % 5);
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
                            case EXO:
                                System.out.println("Fractal Type/Mode is now EXO");
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
                    case BACKSLASH:
                        if (noises[noiseIndex] instanceof CellularNoise) {
                            CellularNoise cn = (CellularNoise) noises[noiseIndex];
                            cn.setNoiseType(cn.getNoiseType().ordinal() + (UIUtils.shift() ? -1 : 1));
                            System.out.println("Using CellularNoise.NoiseType: " + cn.getNoiseType());
                        }
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
        if (Gdx.input.isKeyPressed(LEFT)) {
            hue -= Gdx.graphics.getDeltaTime() * 0.25f;
            hue -= MathUtils.floor(hue);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        } else if (Gdx.input.isKeyPressed(RIGHT)) {
            hue += Gdx.graphics.getDeltaTime() * 0.25f;
            hue -= MathUtils.floor(hue);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        } else if (Gdx.input.isKeyPressed(DOWN)) {
            lit = Math.max(0f, lit - Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        } else if (Gdx.input.isKeyPressed(UP)) {
            lit = Math.min(1f, lit + Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        } else if (Gdx.input.isKeyPressed(LEFT_BRACKET)) {
            sat = Math.max(0f, sat - Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        } else if (Gdx.input.isKeyPressed(RIGHT_BRACKET)) {
            sat = Math.min(1f, sat + Gdx.graphics.getDeltaTime() * 0.25f);
            ColorSupport.hsl2rgb(color, hue, sat, lit, 1f);
        } else if (Gdx.input.isKeyPressed(I)) {
            mixRaw += Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? 0.25f : -0.25f);
            mix = RoughMath.logisticRough(mixRaw);
        } else if (Gdx.input.isKeyPressed(U)) {
            mulRaw += Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? 0.25f : -0.25f);
            mul = RoughMath.pow2Rough(mulRaw);
        } else if (Gdx.input.isKeyPressed(Y)) {
            biasRaw += Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? 0.25f : -0.25f);
            bias = RoughMath.pow2Rough(biasRaw);
        }


        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr * 0.75f, ccc = c * 0.3f;
        // c and ctr are both about the same counter, they advance by a tiny float every frame.
        for (int d = 4096; d < 8192; d++) {
            bright = d / 8191f; // takes bright into the 0.5 to 1.0 range, roughly
            float da = d * 0x1p-8f;
            float x = ((fbm(da * xFreq + c, noise.getSeed())) * 0.5f) * bright * width + width * 0.5f;
            float y = ((fbm(da * yFreq + c, ~noise.getSeed())) * 0.5f) * bright * height + height * 0.5f;
            // this rotates hue over time and as bright changes (so, as the current dot, d, changes).
            // saturation is always vivid, so 1, and the lightness gets brighter towards newer d (higher d).
            // packing tempColor to its float bits avoids recalculating that conversion 45056 extra times.
            float packed = ColorSupport.hsb2rgb(tempColor, hue + bright + ccc, 1f, bright * 0.9f - 0.2f, 1f).toFloatBits();
            renderer.vertex(x - 1, y, 0);
            renderer.color(packed);
            renderer.vertex(x + 1, y, 0);
            renderer.color(packed);
            renderer.vertex(x, y - 1, 0);
            renderer.color(packed);
            renderer.vertex(x, y + 1, 0);
            renderer.color(packed);
            renderer.vertex(x - 1, y - 1, 0);
            renderer.color(packed);
            renderer.vertex(x + 1, y + 1, 0);
            renderer.color(packed);
            renderer.vertex(x + 1, y - 1, 0);
            renderer.color(packed);
            renderer.vertex(x - 1, y + 1, 0);
            renderer.color(packed);
            renderer.vertex(x - 2, y, 0);
            renderer.color(packed);
            renderer.vertex(x + 2, y, 0);
            renderer.color(packed);
            renderer.vertex(x, y - 2, 0);
            renderer.color(packed);
            renderer.vertex(x, y + 2, 0);
        }
        for (int d = 4096; d < 8192; d++) {
            bright = d / 8191f; // takes bright into the 0.5 to 1.0 range, roughly
            float da = d * 0x1p-8f;
            float x = ((fbm(da * xFreq + c, noise.getSeed())) * 0.5f) * bright * width + width * 0.5f;
            float y = ((fbm(da * yFreq + c, ~noise.getSeed())) * 0.5f) * bright * height + height * 0.5f;
            // this rotates hue over time and as bright changes (so, as the current dot, d, changes).
            // saturation is always vivid, so 1, and the lightness gets brighter towards newer d (higher d).
            ColorSupport.hsb2rgb(tempColor, hue + bright + ccc, 1f, Math.min(1f, bright * 1.4f), 1f);
            renderer.color(tempColor);
            renderer.vertex(x, y, 0);
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
        config.setTitle("SquidSquad Test: Wobble Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new WobbleVisualizer(), config);
    }
}
