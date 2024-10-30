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
 */
public class NoisePreviewGenerator extends ApplicationAdapter {

    private static final boolean ACTUALLY_RENDER_GIF = true;
    private static final boolean ACTUALLY_RENDER_PNG = true;

    public interface FloatToFloatFunction {
        float applyAsFloat(float f);
    }
    RawNoise[] noises = new RawNoise[]{
            new CellularNoise(1),
            new CyclicNoise(1, 3),
            new FoamNoise(1),
            new HoneyNoise(1),
            new OpenSimplex2FastNoise(1),
            new OpenSimplex2SmoothNoise(1),
            new PerlinNoise(1),
            new PerlueNoise(1),
            new SimplexNoise(1),
            new SorbetNoise(1, 3),
            new ValueNoise(1),
    };
    int noiseIndex = noises.length - 1;
    private int dim = 1; // this can be 0, 1, 2, 3, 4, OR 5; add 1 to get the actual dimensions
    private int octaves = 1; // starts at 1
    private float freq = 0x1p-4f;
    private final ContinuousNoise noise = new ContinuousNoise(noises[noiseIndex], 1, freq, FBM, octaves);

    private static final int width = 512, height = 512;
//    private static final int width = 256, height = 256;

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

        FileHandle gifFile = Gdx.files.local("out/gif/" + noise.stringSerialize().replace('`', '_') + ".gif");
        FileHandle pngFile = Gdx.files.local("out/noise/" + noise.stringSerialize().replace('`', '_') + ".png");
        System.out.println(noise.toHumanReadableString()+": ([Animated GIF link](gif/"+gifFile.name()+")) ![Noise Preview](noise/"+pngFile.name() + ")\n");

        if(ACTUALLY_RENDER_GIF) {
            for (int c = 0; c < 80; c++) {
                int w = 256, h = 256;
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
        if (ACTUALLY_RENDER_PNG) {
            int w = 256, h = 256;
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
    }

    @Override
    public void render() {
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
