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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.FastPNG;

import static com.badlogic.gdx.math.MathUtils.PI2;
import static com.github.tommyettinger.cringe.ContinuousNoise.FBM;

/**
 * {@code cd out/noise/}
 * {@code oxipng -o 6 -s *.png}
 * {@code cd ../gif/}
 * {@code gifsicle -O=99 --use-colormap gray --resize 128x128 -b *.gif}
 */
public class WangNoiseGenerator extends ApplicationAdapter {

    private static final boolean ACTUALLY_RENDER_PNG = true;
    private static final boolean TIME_SEED = false;

    public interface FloatToFloatFunction {
        float applyAsFloat(float f);
    }
    MultiSeedPerlinNoise raw = new MultiSeedPerlinNoise(123);

    int seed = 0;
    private static final float scale = 0x1p-6f;
    private final WangContinuousNoise noise = new WangContinuousNoise(raw, seed, scale, FBM, 1);

//    private static final int width = 512, height = 512;
    private static final int width = 256, height = 256;

    private Viewport view;

    private FastPNG png;

    public float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    private final FloatToFloatFunction prepare = this::basicPrepare;

    @Override
    public void create() {
        view = new ScreenViewport();

        png = new FastPNG();
        seed = (TIME_SEED) ? (int)Math.abs(Scramblers.scramble(System.nanoTime())) : 0;
        noise.setWrapped(raw);
            for (int m = 0; m <= WangContinuousNoise.RIDGED; m++) {
                noise.setMode(m);
                for (int o = 1; o <= 3; o++) {
                    noise.setOctaves(o);
                    putMap();
                }
            }

        Gdx.app.exit();
    }

    public void putMap() {
        if (ACTUALLY_RENDER_PNG) {
            Gdx.files.local("out/noiseWang/").mkdirs();
            int w = width, h = height;
            FileHandle mainPngFile = Gdx.files.local("out/noiseWang/" + noise.stringSerialize().replace('`', '_') + "_Wang.png");
            Pixmap mp = new Pixmap(w * 4, h * 4, Pixmap.Format.RGBA8888);
            for (int s = 0; s < 16; s++) {
                noise.setSeed(seed + s);

                FileHandle pngFile = Gdx.files.local("out/noiseWang/" + noise.stringSerialize().replace('`', '_') + ".png");

                Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        float color = prepare.applyAsFloat(noise.getNoise(x, y));
                        p.setColor(color, color, color, 1f);
                        p.drawPixel(x, y);
                    }
                }
                mp.drawPixmap(p, (s & 3) * w, (s >>> 2) * h);
                png.write(pngFile, p);

                p.dispose();
            }
            png.write(mainPngFile, mp);
            mp.dispose();
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
        config.setTitle("Wang Noise Preview Generator");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new WangNoiseGenerator(), config);
    }
}
