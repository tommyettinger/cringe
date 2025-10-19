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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.FastPNG;
import com.github.tommyettinger.anim8.PaletteReducer;

import static com.github.tommyettinger.cringe.ContinuousNoise.*;

/**
 * {@code cd out/noise/}
 * {@code oxipng -o 6 -s *.png}
 * {@code cd ../gif/}
 * {@code gifsicle -O=99 --use-colormap gray --resize 128x128 -b *.gif}
 */
public class NoisePreviewGenerator extends ApplicationAdapter {

    private static final boolean ACTUALLY_RENDER_GIF = false;
    private static final boolean ACTUALLY_RENDER_PNG_1D = false;
    private static final boolean ACTUALLY_RENDER_PNG = false;
    private static final boolean ACTUALLY_RENDER_SLICES = true;

    public interface FloatToFloatFunction {
        float applyAsFloat(float f);
    }
    RawNoise[] noises =
            new RawNoise[]{
//                    new CellularNoise(1),
                    new CyclicNoise(1, 3),
                    new FoamNoise(1),
                    new HoneyNoise(1),
//                    new OpenSimplex2FastNoise(1),
//                    new OpenSimplex2SmoothNoise(1),
                    new PerlinNoise(1),
                    new PerlueNoise(1),
                    new SimplexNoise(1),
                    new SorbetNoise(1, 3),
                    new ValueNoise(1),
            };

//            Serializer.getAll().toArray();

    int noiseIndex = 0;
    private int dim = 0; // this can be 0, 1, 2, 3, 4, OR 5; add 1 to get the actual dimensions
    private int octaves = 1; // starts at 1
    private float freq = 0x1p-4f;
    private final ContinuousNoise noise = new ContinuousNoise(noises[noiseIndex], 1, freq, FBM, octaves);

//    private static final int width = 512, height = 512;
    private static final int width = 256, height = 256;

    private Viewport view;

    private AnimatedGif gif;
    private FastPNG png;
    private final Array<Pixmap> frames = new Array<>(80);

    public float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    private final FloatToFloatFunction prepare = this::basicPrepare;

    @Override
    public void create() {
        view = new ScreenViewport();
        int[] gray256 = new int[256];
        for (int i = 0; i < 256; i++) {
            gray256[i] = i * 0x01010100 + 0xFF;
        }

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.NONE);
        gif.setDitherStrength(1f);
        gif.palette = new PaletteReducer(gray256);

        png = new FastPNG();

        System.out.println("# Noise Previews\n");
        for(RawNoise rn : noises) {
            noise.setWrapped(rn);
            for (int m = 0; m <= EXO; m++) {
                noise.setMode(m);
                for (int o = 1; o <= 3; o++) {
                    noise.setOctaves(o);
                    putMap();
                }
            }
        }

        Gdx.app.exit();
    }

    public void putMap() {
        Gdx.files.local("out/gif/").mkdirs();
        Gdx.files.local("out/noise/").mkdirs();
        Gdx.files.local("out/noise1d/").mkdirs();

        FileHandle gifFile = Gdx.files.local("out/gif/" + noise.stringSerialize().replace('`', '_') + ".gif");
        FileHandle pngFile = Gdx.files.local("out/noise/" + noise.stringSerialize().replace('`', '_') + ".png");
        FileHandle png1DFile = Gdx.files.local("out/noise1d/" + noise.stringSerialize().replace('`', '_') + ".png");
        System.out.println(noise.toHumanReadableString()+":\n\n"+"![1D Noise Preview](noise1d/"+png1DFile.name() + ")"+" ![Noise Preview](noise/"+pngFile.name() + ")"+" ([Animated GIF link](gif/"+gifFile.name()+"))"+"\n");

        if(ACTUALLY_RENDER_GIF) {
            for (int c = 0; c < 80; c++) {
                int w = width, h = height;
                float time = c * 0.25f;
                Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        float color = prepare.applyAsFloat(noise.getNoise(x, y, time));
                        p.setColor(color, color, color, 1f);
                        p.drawPixel(x, y);
                    }
                }
                frames.add(p);
            }
            gif.write(gifFile, frames, 16);
            for (int i = 0; i < frames.size; i++) {
                frames.get(i).dispose();
            }
            frames.clear();
        }
        if (ACTUALLY_RENDER_PNG_1D) {
            int w = width, h = height, lastHeight, y;
            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            for (int row = 0; row < 4; row++) {
                int sd = row * 0x12345 + 0x98765;
                y = (int)(prepare.applyAsFloat(noise.getNoiseWithSeed(0, sd)) * 63.999f) + 64 * row;
                p.setColor(Color.BLACK);
                p.drawPixel(0, y);
                for (int x = 1; x < w; x++) {
                    lastHeight = y;
                    y = (int)(prepare.applyAsFloat(noise.getNoiseWithSeed(x * 0.25f, sd)) * 63.999f) + 64 * row;
                    p.setColor(Color.BLACK);
                    p.drawLine(x - 1, lastHeight, x, y);
                }
            }
            png.write(png1DFile, p);

            p.dispose();
        }
        if (ACTUALLY_RENDER_PNG) {
            int w = width, h = height;
            float time = 1.25f;
            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    float color = prepare.applyAsFloat(noise.getNoise(x, y, time));
                    p.setColor(color, color, color, 1f);
                    p.drawPixel(x, y);
                }
            }
            png.write(pngFile, p);

            p.dispose();
        }
        if (ACTUALLY_RENDER_SLICES) {
            int w = width, h = height;
            Pixmap p2 = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            Pixmap p3 = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            Pixmap p4 = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            Pixmap p5 = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            Pixmap p6 = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            FileHandle d2 = Gdx.files.local("out/noise2D/" + noise.stringSerialize().replace('`', '_') + ".png");
            FileHandle d3 = Gdx.files.local("out/noise3D/" + noise.stringSerialize().replace('`', '_') + ".png");
            FileHandle d4 = Gdx.files.local("out/noise4D/" + noise.stringSerialize().replace('`', '_') + ".png");
            FileHandle d5 = Gdx.files.local("out/noise5D/" + noise.stringSerialize().replace('`', '_') + ".png");
            FileHandle d6 = Gdx.files.local("out/noise6D/" + noise.stringSerialize().replace('`', '_') + ".png");
            float color;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    color = prepare.applyAsFloat(noise.getNoise(x, y));
                    p2.setColor(color, color, color, 1f);
                    p2.drawPixel(x, y);
                    color = prepare.applyAsFloat(noise.getNoise(x, y, 1f));
                    p3.setColor(color, color, color, 1f);
                    p3.drawPixel(x, y);
                    color = prepare.applyAsFloat(noise.getNoise(x, y, 1f, 1f));
                    p4.setColor(color, color, color, 1f);
                    p4.drawPixel(x, y);
                    color = prepare.applyAsFloat(noise.getNoise(x, y, 1f, 1f, 1f));
                    p5.setColor(color, color, color, 1f);
                    p5.drawPixel(x, y);
                    color = prepare.applyAsFloat(noise.getNoise(x, y, 1f, 1f, 1f, 1f));
                    p6.setColor(color, color, color, 1f);
                    p6.drawPixel(x, y);
                }
            }
            png.write(d2, p2);
            png.write(d3, p3);
            png.write(d4, p4);
            png.write(d5, p5);
            png.write(d6, p6);

            p2.dispose();
            p3.dispose();
            p4.dispose();
            p5.dispose();
            p6.dispose();
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
        config.setTitle("Noise Preview Generator");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new NoisePreviewGenerator(), config);
    }
}
