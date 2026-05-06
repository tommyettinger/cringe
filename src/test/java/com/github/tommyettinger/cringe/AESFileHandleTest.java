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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.badlogic.gdx.Input.Keys.*;

public class AESFileHandleTest extends ApplicationAdapter {
    public static String title = "";
    private static final int width = 459, height = 816;

    private Viewport view;
    private SpriteBatch renderer;

    private Texture texture = null;
    private byte[] k = AESFileHandle.expandKeyphrase(123L, "I sure do love that cat!");

    @Override
    public void create() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
        renderer = new SpriteBatch();
        view = new ScreenViewport();
        FileHandle cat = Gdx.files.internal("Cat_Portrait.png");
        FileHandle foam = Gdx.files.internal("Foam_Noise.png");
        FileHandle catEncrypted = Gdx.files.local("out/Cat_Portrait.png");
        FileHandle foamEncrypted = Gdx.files.local("out/Foam_Noise.png");
        texture = new Texture(cat);

        InputAdapter input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                Texture t;
                switch (keycode) {
                    case NUM_1:
                    case NUMPAD_1:
                        k[1] += UIUtils.shift() ? -1 : 1;
                        break;
                    case NUM_2:
                    case NUMPAD_2:
                        k[2] += UIUtils.shift() ? -1 : 1;
                        break;
                    case NUM_3:
                    case NUMPAD_3:
                        k[3] += UIUtils.shift() ? -1 : 1;
                        break;
                    case NUM_4:
                    case NUMPAD_4:
                        k[4] += UIUtils.shift() ? -1 : 1;
                        break;
                    case P:
                        System.out.println(Base64Coder.encode(k));
                        break;
                    case R:
                        if(MathUtils.randomBoolean()) {
                            if (catEncrypted.exists()) {
                                try {
                                    t = new Texture(new AESFileHandle(catEncrypted, k));
                                } catch (Exception e) {
                                    System.out.println("Failed to read " + catEncrypted);
                                    break;
                                }
                                texture.dispose();
                                texture = t;
                            }
                        } else {
                            if (foamEncrypted.exists()) {
                                try {
                                    t = new Texture(new AESFileHandle(foamEncrypted, k));
                                } catch (Exception e) {
                                    System.out.println("Failed to read " + foamEncrypted);
                                    break;
                                }
                                texture.dispose();
                                texture = t;
                            }

                        }
                        break;
                    case C: {
                        AESFileHandle ciphered = new AESFileHandle(catEncrypted, k);
                        ciphered.writeBytes(cat.readBytes(), false);
                        break;
                    }
                    case F: {
                        AESFileHandle ciphered = new AESFileHandle(foamEncrypted, k);
                        ciphered.writeBytes(foam.readBytes(), false);
                        break;
                    }
                    case Q: // quit
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS showing " + title);
        renderer.begin();
        renderer.draw(texture, 0, 0);
        renderer.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("AES Cipher on a FileHandle");
        config.setWindowedMode(width, height);
        config.useVsync(false);
        config.setForegroundFPS(10);
        config.setResizable(false);
        new Lwjgl3Application(new AESFileHandleTest(), config);
    }
}
