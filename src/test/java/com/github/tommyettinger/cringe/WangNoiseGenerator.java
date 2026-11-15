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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.FastPNG;

import static com.github.tommyettinger.cringe.ContinuousNoise.FBM;

/**
 * Generates 16 Wang-tiling (or omni-tiling) noise textures using a variety of {@link WangRawNoise} types and modes.
 * This has several options that can be changed at the start of the code:
 * <ul>
 *     <li>If DRY_RUN is true, this will only set up the rendering, but won't save any files.</li>
 *     <li>If TIME_SEED is true, the seed will be changed based on the current time in milliseconds. If it is false, the
 *     seeds will simply range from 0 to 15, inclusive.</li>
 *     <li>If SINGLE_EDGE is true, this will only make omni-tiling noise; otherwise, it makes Wang tiles.</li>
 *     <li>If CANON is true, this will name the noise textures differently and make them suitable for use in a
 *     TextureAtlas. This is meant to generate the noise textures in docs/noiseWang/ and docs/noiseOmni/ .</li>
 *     <li>The SIZE is the pixel width and pixel height of each tile of noise. It should usually be a power of two, but
 *     doesn't need to be.</li>
 *     <li>The SCALE is the frequency of the noise to generate (at one octave). It should usually be a negative power of
 *     two, such as the default 1/16f. It can be higher for more "busy" noise, such as using 1/4f, or lower for noise
 *     that changes more gradually, such as using 1/64f. SIZE times SCALE should be at minimum 2.</li>
 * </ul>
 */
public class WangNoiseGenerator extends ApplicationAdapter {

    public static final boolean DRY_RUN = false;
    public static final boolean TIME_SEED = false;
    public static final boolean SINGLE_EDGE = false;
    public static final boolean CANON = true;
    public static final int SIZE = 128;
    public static final float SCALE = 0x1p-4f;

    private static final String TYPE_NAME = SINGLE_EDGE ? "Omni" : "Wang";
    private static final String OUTPUT_FOLDER = CANON ? "out/canon"+SIZE+"/" : "out/";

    public interface FloatToFloatFunction {
        float applyAsFloat(float f);
    }
    WangRawNoise[] noises = {
            new WangPerlinNoise(123),
            new WangPerlueNoise(123),
            new WangValueNoise(123),
    };

    int noiseIndex = 0;

    int seed = 0;
    private final WangContinuousNoise noise = new WangContinuousNoise(noises[noiseIndex], seed, SCALE, FBM, 1);

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
        png.setFlipY(false); // useful if using TextureRegion.split() on a composite texture
        seed = (TIME_SEED) ? (int)Math.abs(Scramblers.scramble(System.nanoTime())) : 0;
        noise.setTileSize(SIZE);
        noise.setSingleEdge(SINGLE_EDGE);

        for (noiseIndex = 0; noiseIndex < noises.length; noiseIndex++) {
            noise.setWrapped(noises[noiseIndex]);
            for (int m = 0; m <= WangContinuousNoise.RIDGED; m++) {
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
        if (!DRY_RUN) {
            Gdx.files.local(OUTPUT_FOLDER+"noise"+TYPE_NAME+"/").mkdirs();
            int w = SIZE, h = SIZE;
            noise.setSeed(seed);
            FileHandle mainPngFile = Gdx.files.local(CANON
                    ? OUTPUT_FOLDER+"noise"+TYPE_NAME+"/"+noise.getWrapped().getTag()+"_"+noise.octaves+"_"+WangContinuousNoise.MODES[noise.mode]+"_"+TYPE_NAME+".png"
                    : OUTPUT_FOLDER+"noise"+TYPE_NAME+"/" + noise.stringSerialize().replace('`', '_') + TYPE_NAME+".png");
            Pixmap mp = new Pixmap(w * 4, h * 4, Pixmap.Format.RGBA8888);
            for (int s = 0; s < 16; s++) {
                noise.setSeed((seed & -16) + s);

                FileHandle pngFile = Gdx.files.local(
                        CANON
                                ? OUTPUT_FOLDER+"noise"+TYPE_NAME+"/"+noise.getWrapped().getTag()+"_"+noise.octaves+"_"+WangContinuousNoise.MODES[noise.mode]+"_"+s+".png"
                                : OUTPUT_FOLDER+"noise"+TYPE_NAME+"/"+noise.stringSerialize().replace('`', '_') + ".png"
                );

                Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        float color = prepare.applyAsFloat(noise.getNoise(x, y, SIZE * 0.35f));
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
        config.setTitle(TYPE_NAME+" Noise Preview Generator");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(SIZE, SIZE);
        config.disableAudio(true);
        new Lwjgl3Application(new WangNoiseGenerator(), config);
    }
}
