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
    };
    int noiseIndex = 1;
    private int dim = 1; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 0x1p-4f;
    private ContinuousNoise noise = new ContinuousNoise(noises[noiseIndex], 1, freq, 0, 1);
    private ImmediateModeRenderer20 renderer;

    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
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
            gray256[i] = i * 0x010101 << 8 | 0xFF;
        }

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.WREN);
        gif.setDitherStrength(0.2f);
        gif.palette = new PaletteReducer(gray256);

//                0x00000000, 0x000000FF, 0x081820FF, 0x132C2DFF, 0x1E403BFF, 0x295447FF, 0x346856FF, 0x497E5BFF,
//                0x5E9463FF, 0x73AA69FF, 0x88C070FF, 0x9ECE88FF, 0xB4DCA0FF, 0xCAEAB8FF, 0xE0F8D0FF, 0xEFFBE7FF,
//                0xFFFFFFFF,

//                0x000000ff, 0x000033ff, 0x000066ff, 0x000099ff, 0x0000ccff, 0x0000ffff, 0x003300ff,
//                0x003333ff, 0x003366ff, 0x003399ff, 0x0033ccff, 0x0033ffff, 0x006600ff, 0x006633ff, 0x006666ff,
//                0x006699ff, 0x0066ccff, 0x0066ffff, 0x009900ff, 0x009933ff, 0x009966ff, 0x009999ff, 0x0099ccff,
//                0x0099ffff, 0x00cc00ff, 0x00cc33ff, 0x00cc66ff, 0x00cc99ff, 0x00ccccff, 0x00ccffff, 0x00ff00ff,
//                0x00ff33ff, 0x00ff66ff, 0x00ff99ff, 0x00ffccff, 0x00ffffff, 0x330000ff, 0x330033ff, 0x330066ff,
//                0x330099ff, 0x3300ccff, 0x3300ffff, 0x333300ff, 0x333333ff, 0x333366ff, 0x333399ff, 0x3333ccff,
//                0x3333ffff, 0x336600ff, 0x336633ff, 0x336666ff, 0x336699ff, 0x3366ccff, 0x3366ffff, 0x339900ff,
//                0x339933ff, 0x339966ff, 0x339999ff, 0x3399ccff, 0x3399ffff, 0x33cc00ff, 0x33cc33ff, 0x33cc66ff,
//                0x33cc99ff, 0x33ccccff, 0x33ccffff, 0x33ff00ff, 0x33ff33ff, 0x33ff66ff, 0x33ff99ff, 0x33ffccff,
//                0x33ffffff, 0x660000ff, 0x660033ff, 0x660066ff, 0x660099ff, 0x6600ccff, 0x6600ffff, 0x663300ff,
//                0x663333ff, 0x663366ff, 0x663399ff, 0x6633ccff, 0x6633ffff, 0x666600ff, 0x666633ff, 0x666666ff,
//                0x666699ff, 0x6666ccff, 0x6666ffff, 0x669900ff, 0x669933ff, 0x669966ff, 0x669999ff, 0x6699ccff,
//                0x6699ffff, 0x66cc00ff, 0x66cc33ff, 0x66cc66ff, 0x66cc99ff, 0x66ccccff, 0x66ccffff, 0x66ff00ff,
//                0x66ff33ff, 0x66ff66ff, 0x66ff99ff, 0x66ffccff, 0x66ffffff, 0x990000ff, 0x990033ff, 0x990066ff,
//                0x990099ff, 0x9900ccff, 0x9900ffff, 0x993300ff, 0x993333ff, 0x993366ff, 0x993399ff, 0x9933ccff,
//                0x9933ffff, 0x996600ff, 0x996633ff, 0x996666ff, 0x996699ff, 0x9966ccff, 0x9966ffff, 0x999900ff,
//                0x999933ff, 0x999966ff, 0x999999ff, 0x9999ccff, 0x9999ffff, 0x99cc00ff, 0x99cc33ff, 0x99cc66ff,
//                0x99cc99ff, 0x99ccccff, 0x99ccffff, 0x99ff00ff, 0x99ff33ff, 0x99ff66ff, 0x99ff99ff, 0x99ffccff,
//                0x99ffffff, 0xcc0000ff, 0xcc0033ff, 0xcc0066ff, 0xcc0099ff, 0xcc00ccff, 0xcc00ffff, 0xcc3300ff,
//                0xcc3333ff, 0xcc3366ff, 0xcc3399ff, 0xcc33ccff, 0xcc33ffff, 0xcc6600ff, 0xcc6633ff, 0xcc6666ff,
//                0xcc6699ff, 0xcc66ccff, 0xcc66ffff, 0xcc9900ff, 0xcc9933ff, 0xcc9966ff, 0xcc9999ff, 0xcc99ccff,
//                0xcc99ffff, 0xcccc00ff, 0xcccc33ff, 0xcccc66ff, 0xcccc99ff, 0xccccccff, 0xccccffff, 0xccff00ff,
//                0xccff33ff, 0xccff66ff, 0xccff99ff, 0xccffccff, 0xccffffff, 0xff0000ff, 0xff0033ff, 0xff0066ff,
//                0xff0099ff, 0xff00ccff, 0xff00ffff, 0xff3300ff, 0xff3333ff, 0xff3366ff, 0xff3399ff, 0xff33ccff,
//                0xff33ffff, 0xff6600ff, 0xff6633ff, 0xff6666ff, 0xff6699ff, 0xff66ccff, 0xff66ffff, 0xff9900ff,
//                0xff9933ff, 0xff9966ff, 0xff9999ff, 0xff99ccff, 0xff99ffff, 0xffcc00ff, 0xffcc33ff, 0xffcc66ff,
//                0xffcc99ff, 0xffccccff, 0xffccffff, 0xffff00ff, 0xffff33ff, 0xffff66ff, 0xffff99ff, 0xffffccff,
//                0xffffffff, 
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
                            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < w; x++) {
                                for (int y = 0; y < h; y++) {
//                                    float color = basicPrepare(noise.getNoise(x, y, c));
                                    float color = circleInPrepare(
                                            noise.getNoise(
                                            x, y, c - inv * ((x - halfW) * (x - halfW) + (y - halfH) * (y - halfH)))
                                    );
//                                            * 0.5f + 0.25f + MathUtils.sinDeg(360 * c * 0x1p-7f) * 0.25f;
//                                    color = color * 0x0.FFp0f + 0x1p-8f;
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
                        ctr++;
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
        float bright, c = ctr * 0.1f;
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
            ctr++;
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
