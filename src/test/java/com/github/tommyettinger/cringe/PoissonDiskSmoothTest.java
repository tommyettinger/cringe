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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
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
public class PoissonDiskSmoothTest extends ApplicationAdapter {

    private final RandomDistinct64 random = new RandomDistinct64(-5672513084691825730L);
    private ImmediateModeRenderer20 renderer;

    private Vector2 center = new Vector2();

    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;

    private Viewport view;
    private float ctr = -256;
    private boolean keepGoing = true;

    private Color color = new Color(Color.WHITE);//new Color(Color.LIME);
    private float hue = hue(color), sat = saturation(color), lit = lightness(color);

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();

        InputAdapter input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case P: //pause
                        keepGoing = !keepGoing;
                        if (keepGoing)
                            System.out.println("Now playing");
                        else
                            System.out.println("Now paused");
                        break;
                    case SPACE:
                        ctr += 1f / 60f;
                        System.out.println("Stepping ahead so ctr is " + ctr);
                        break;
                    case E: //earlier seed
                        random.setState(random.getState() - 1);
                        System.out.println("Seed is now " + random.getState());
                        break;
                    case S: //seed
                        random.setState(random.getState() + 1);
                        System.out.println("Seed is now " + random.getState());
                        break;
                    case SLASH:
                        random.setState(Scramblers.scramble(random.getState()));
                        System.out.println("Seed is now " + random.getState());
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
        long state = random.state;
        Array<Vector2> points = PoissonDiskSmooth.sampleCircle(center.set((float)random.nextGaussian() * 16f + width * 0.5f,
                (float)random.nextGaussian() * 16f + height * 0.5f), 100f, 10f, width, height, 40, random).orderedKeys();
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float c = ctr;
        for (int i = 0; i < points.size; i++) {
            renderer.color(ColorSupport.hsl2rgb(color, c + i / 16f, 0.8f, 0.6f, 1f));
            renderer.vertex(points.get(i).x, points.get(i).y, 0);
        }
        renderer.end();
        random.setState(state);
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
        config.setTitle("Cringe Test: PoissonDiskSmooth");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new PoissonDiskSmoothTest(), config);
    }
}
