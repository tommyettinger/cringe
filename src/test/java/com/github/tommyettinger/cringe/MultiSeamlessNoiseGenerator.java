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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.FastPNG;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.PI2;
import static com.github.tommyettinger.cringe.ContinuousNoise.FBM;

/**
 * {@code cd out/noise/}
 * {@code oxipng -o 6 -s *.png}
 * {@code cd ../gif/}
 * {@code gifsicle -O=99 --use-colormap gray --resize 128x128 -b *.gif}
 */
public class MultiSeamlessNoiseGenerator extends ApplicationAdapter {

    private static final boolean ACTUALLY_RENDER_PNG = true;
    private static final boolean TIME_SEED = false;

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
    private static final float scale = 1f;
    private final ContinuousNoise noise = new ContinuousNoise(noises[noiseIndex], 1, scale, FBM, 1);

//    private static final int width = 512, height = 512;
    private static final int width = 256, height = 256;

    private Viewport view;

    private FastPNG png;

    public static float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    private static final FloatToFloatFunction prepare = MultiSeamlessNoiseGenerator::basicPrepare;

    /**
     * Has no seams! Is quite varied in the middle quarter of the image.
     * This has an alpha of 0.5 .
     * @param n between 0 and PI2
     * @return between 0 (at the edges) and 1 (in the center)
     */
    private static float tukeyWindow(float n){
        if(n > PI) n = PI2 - n;
        if(n > PI * 0.5f) return 1f;
        return 0.5f * (1f - MathUtils.cos(2 * n));
    }

    /**
     * Has no seams, but does squash the edges a bit. Is quite varied in the middle quarter of the image.
     * This has an alpha of 0.25 .
     * @param n between 0 and PI2
     * @return between 0 (at the edges) and 1 (in the center)
     */
    private static float tukeyWindowQuarter(float n){
        if(n > PI) n = PI2 - n;
        if(n > PI * 0.25f) return 1f;
        return 0.5f * (1f - MathUtils.cos(4 * n));
    }

    /**
     * Doesn't really have seams, but also doesn't have much variation.
     * @param n between 0 and PI2
     * @return between 0 (at the edges) and 1 (in the center)
     */
    private static float sineWindow(float n){
        return MathUtils.sin(0.5f * n);
    }

    /**
     * Has seams!
     * @param n between 0 and PI2
     * @return between 0 (at the edges) and 1 (in the center)
     */
    private static float sineSqrtWindow(float n){
        return (float)Math.sqrt(MathUtils.sin(0.5f * n));
    }

    private static final OrderedMap<String, FloatToFloatFunction> windowingFunctions = new OrderedMap<>(3);

    static {
//        windowingFunctions.put("Tukey", MultiSeamlessNoiseGenerator::tukeyWindow);
        windowingFunctions.put("TukeyQuarter", MultiSeamlessNoiseGenerator::tukeyWindowQuarter);
//        windowingFunctions.put("Sine", MultiSeamlessNoiseGenerator::sineWindow);
//        windowingFunctions.put("SineSqrt", MultiSeamlessNoiseGenerator::sineSqrtWindow);
    }

    @Override
    public void create() {
        view = new ScreenViewport();

        png = new FastPNG();
        int seed = (TIME_SEED) ? (int)Scramblers.scramble(System.nanoTime()) : 1;

        for(RawNoise rn : noises) {
            noise.setWrapped(rn);
            noise.setSeed(seed);
            for (int m = 0; m < ContinuousNoise.WARP; m++) {
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

        Gdx.files.local("out/noiseMultiSeamless/").mkdirs();

        if (ACTUALLY_RENDER_PNG) {
            int w = width, h = height;
            for (ObjectMap.Entry<String, FloatToFloatFunction> ent : windowingFunctions) {

                FloatToFloatFunction window = ent.value;
                FileHandle mainPngFile = Gdx.files.local("out/noiseMultiSeamless/" + noise.stringSerialize().replace('`', '-') + "_" + ent.key + "_main.png");
                Pixmap mp = new Pixmap(w * 4, h * 4, Pixmap.Format.RGBA8888);
                for (int z = 0; z < 16; z++) {

                    FileHandle pngFile = Gdx.files.local("out/noiseMultiSeamless/" + noise.stringSerialize().replace('`', '-') + "_" + ent.key + "_" + z + ".png");
                    Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            float xx = x * PI2 / w;
                            float yy = y * PI2 / h;
                            float limiter = Math.min(window.applyAsFloat(xx), window.applyAsFloat(yy));
                            float color = prepare.applyAsFloat(noise.getNoise(
                                    MathUtils.cos(xx), MathUtils.sin(xx),
                                    MathUtils.cos(yy), MathUtils.sin(yy),
                                    limiter * LineWobble.bicubicWobble(xx, z),
                                    limiter * LineWobble.bicubicWobble(yy, z)));
                            p.setColor(color, color, color, 1f);
                            p.drawPixel(x, y);
                        }
                    }
                    mp.drawPixmap(p, (z & 3) * w, (z >>> 2) * h);
                    png.write(pngFile, p);

                    p.dispose();
                }
                png.write(mainPngFile, mp);
                mp.dispose();
            }
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
        new Lwjgl3Application(new MultiSeamlessNoiseGenerator(), config);
    }
}
