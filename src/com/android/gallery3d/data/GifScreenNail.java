/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import com.android.gallery3d.data.GifHelper.GifFrame;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.GifHelper;
import com.android.gallery3d.common.Utils;


public class GifScreenNail {
    private static final String TAG = "GifScreenNail";
    private static final int PLACEHOLDER_COLOR = 0xFF222222;
    // The duration of the fading animation in milliseconds
    private static final int DURATION = 180;

    private static final int MAX_SIDE = 640;
    // These are special values for mAnimationStartTime
    private static final long ANIMATION_NOT_NEEDED = -1;
    private static final long ANIMATION_NEEDED = -2;
    private static final long ANIMATION_DONE = -3;
    private String mPath;
    private int mWidth;
    private int mHeight;
    private Bitmap mBitmap;
    private long mAnimationStartTime = ANIMATION_NOT_NEEDED;
    private TiledScreenNail mTempScreenNail;
    private BitmapScreenNail mBitmapScreenNail;
    private long mTime = -1;
    private GifHelper gifhelp = null;

    public GifScreenNail(String path) {
        mPath = path;
        gifhelp = new GifHelper();
    }

    public void stop() {
        gifhelp.stop();
        gifhelp.recycle();
    }

    public String getPath() {
        return mPath;
    }

    public void Gifdecoder() {
        gifhelp.start();
        gifhelp.startDecode(mPath);
        mTempScreenNail = null;
    }

    public ScreenNail getNextGif() {
        if ((gifhelp.getState() > 0) && gifhelp.getFrame(gifhelp.getFrameindex()) != null) {
            if (mTempScreenNail == null) {
                return mTempScreenNail = new TiledScreenNail(gifhelp.nextBitmap(), true);
            }
            if (mTime < 0) {
                mTime = System.currentTimeMillis();
                return mTempScreenNail.combine(new TiledScreenNail(gifhelp.nextBitmap(), true));
            } else {
                if ((System.currentTimeMillis() - mTime) > gifhelp.nextDelay()) {
                    mTime = System.currentTimeMillis();
                    return mTempScreenNail.combine(new TiledScreenNail(gifhelp.nextBitmap(), true));
                } else {
                    return mTempScreenNail.combine(new TiledScreenNail(gifhelp.getFrame(gifhelp.getFrameindex()), true));
                }
            }
        } else {
            return null;
        }
    }

    public ScreenNail getNextBitmapGif() {
        if ((gifhelp.getState() > 0) && gifhelp.getFrame(gifhelp.getFrameindex()) != null) {
            if (mBitmapScreenNail == null) {
                return mBitmapScreenNail = new BitmapScreenNail(gifhelp.nextBitmap().copy(Bitmap.Config.ARGB_8888, true));
            }
            if (mTime < 0) {
                mTime = System.currentTimeMillis();
                return mBitmapScreenNail = new BitmapScreenNail(gifhelp.nextBitmap().copy(Bitmap.Config.ARGB_8888, true));
            } else {
                if ((System.currentTimeMillis() - mTime) > gifhelp.nextDelay()) {
                    mTime = System.currentTimeMillis();
                    return mBitmapScreenNail = new BitmapScreenNail(gifhelp.nextBitmap().copy(Bitmap.Config.ARGB_8888, true));
                } else {
                    return mBitmapScreenNail = new BitmapScreenNail(gifhelp.getFrame(gifhelp.getFrameindex()).copy(Bitmap.Config.ARGB_8888, true));
                }
            }
        } else{
            return null;
        }
    }
}
