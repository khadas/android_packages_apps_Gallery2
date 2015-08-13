package com.android.gallery3d.data;

import com.droidlogic.app.GIFDecodesManager;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Vector;

import android.graphics.Movie;
import android.graphics.Bitmap;
import android.util.Log;

public class GifHelper {

    /**
     * Specify the minimal frame duration in GIF file, unit is ms.
     * Set as 100, then gif animation hehaves mostly as other gif viewer.
     */
    private static final int MINIMAL_DURATION = 100;

    /**
     * Invalid returned value of some memeber function, such as getWidth()
     * getTotalFrameCount(), getFrameDuration()
     */
    public static final int INVALID_VALUE = 0;

    /**
     * Movie object maitained by GifHelper, it contains raw GIF info
     * like graphic control informations, color table, pixel indexes,
     * called when application is no longer interested in gif info.
     * It is contains GIF frame bitmap, 8-bits per pixel,
     * using SkColorTable to specify the colors, which is much
     * memory efficient than ARGB_8888 config. This is why we
     * maintain a Movie object rather than a set of ARGB_8888 Bitmaps.
     */
    private static final int STATE_ERROR = -1;
    private static final int STATE_START = 0;
    private static final int STATE_END = 1;

    private GIFDecodesManager mGIF;
    private Vector<GifFrame> mFrames;

    private static int mFrameindex = INVALID_VALUE;
    private static int mFrameCount = INVALID_VALUE;
    private static int mDecoderState = STATE_START;

    private static int mWidth = INVALID_VALUE;
    private static int mHeight = INVALID_VALUE;
    /**
     * Constructor of GifHelper, which receives InputStream as
     * parameter. Decode an InputStream into Movie object.
     * If the InputStream is null, no decoding will be performed
     *
     * @param is InputStream representing the file to be decoded.
     */
    public GifHelper() {
        mGIF = new GIFDecodesManager();
    }

    /**
     * Constructor of GifHelper, which receives file path name as
     * parameter. Decode a file path into Movie object.
     * If the specified file name is null, no decoding will be performed
     *
     * @param pathName complete path name for the file to be decoded.
     */
    public void GifDecoder(InputStream is) {
        if (is == null) return;
        mGIF.decodeStream(is);
    }

    /**
     * Close gif file, release all informations like frame count,
     * graphic control informations, color table, pixel indexes,
     * called when application is no longer interested in gif info.
     * It will release all the memory mMovie occupies. After close()
     * is call, GifHelper should no longer been used.
     */
    public synchronized void close(){
        if (mGIF == null) return;
        mGIF.destructor();
    }

    private InputStream getIStream(String path) {
        try {
            return new FileInputStream(path);
        } catch (java.io.FileNotFoundException e) {
            return null;
        }
    }

    private void closeIStream(InputStream is) {
        try {
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int read(InputStream is) {
        int curByte = 0;
        try {
            curByte = is.read();
        } catch (Exception e) {
            mDecoderState = STATE_ERROR;
            e.printStackTrace();
        }
        return curByte;
    }

    private void readHeader(InputStream is) {
        String id = "";
        for (int i = 0; i < 6; i++) {
            id += (char) read(is);
        }
        if (!id.toUpperCase().startsWith("GIF")) {
            mDecoderState = STATE_ERROR;
            return;
        }
        mWidth = read(is) | (read(is) << 8);
        mHeight = read(is) | (read(is) << 8);
        closeIStream(is);
    }

    /**
     * Get width of images in gif file.
     * if member mMovie is null, returns INVALID_VALUE
     *
     * @return The total frame count of gif file,
     *         or INVALID_VALUE if the mMovie is null
     */
    public synchronized int getWidth() {
        if (mGIF == null) return INVALID_VALUE;
        return mWidth;
    }

    /**
     * Get height of images in gif file.
     * if member mMovie is null, returns INVALID_VALUE
     *
     * @return The total frame count of gif file,
     *         or INVALID_VALUE if the mMovie is null
     */
    public synchronized int getHeight() {
        if (mGIF == null) return INVALID_VALUE;
        return mHeight;
    }

    /**
     * Get total duration of gif file.
     * if member mMovie is null, returns INVALID_VALUE
     *
     * @return The total duration of gif file,
     *         or INVALID_VALUE if the mMovie is null
     */
    public synchronized int getTotalDuration() {
        if (mGIF == null) return INVALID_VALUE;
        return mGIF.getTotalDuration();
    }

    /**
     * Get total frame count of gif file.
     * if member mMovie is null, returns INVALID_VALUE
     *
     * @return The total frame count of gif file,
     *         or INVALID_VALUE if the mMovie is null
     */
    public synchronized int getTotalFrameCount() {
        if (mGIF == null) return INVALID_VALUE;
        return mGIF.getFrameCount();
    }

    /**
     * Get frame duration specified with frame index of gif file.
     * if member mMovie is null, returns INVALID_VALUE
     *
     * @param frameIndex index of frame interested.
     * @return The duration of the specified frame,
     *         or INVALID_VALUE if the mMovie is null
     */
    public synchronized int getFrameDuration(int frameIndex) {
        if (mGIF == null) return INVALID_VALUE;
        int duration = mGIF.getFrameDuration(frameIndex);
        if (duration <= INVALID_VALUE) {
            duration = MINIMAL_DURATION;
        }
        return duration;
    }

    /**
     * Get frame bitmap specified with frame index of gif file.
     * if member mMovie is null, returns null
     *
     * @param frameIndex index of frame interested.
     * @return The decoded bitmap, or null if the mMovie is null
     */
    public synchronized Bitmap getFrameBitmap(int frameIndex) {
        if (mGIF == null) return null;
        return mGIF.getFrameBitmap(frameIndex);
    }

    /**
     * GifScreenNail API
     */
    class DecodeGifThread extends Thread {
        private String mPath=null;

        public DecodeGifThread(String path) {
            if (path != null) {
                mPath=path;
            }
        }

        @Override
        public void run() {
            if (getHeight() < 1080 && getWidth() < 1920) {
                recycle();

                InputStream is = getIStream(mPath);
                GifDecoder(is);
                mFrameCount = getTotalFrameCount();
                for (int i = 0; i < mFrameCount; i++) {
                    int delay = getFrameDuration(i);
                    Bitmap b = getFrameBitmap(i);
                    if (delay <=0) {
                        mFrames.addElement(new GifFrame(b, 100)); // add image to frame, default set to 10fps
                    } else {
                        mFrames.addElement(new GifFrame(b, delay)); // add image to frame
                    }
                }
                try {
                    Thread.sleep(100);
                    mDecoderState = STATE_END;
                } catch (Throwable e) {
                    e.printStackTrace();
                    mDecoderState = STATE_ERROR;
                }  finally {
                    closeIStream(is);
                }
            }
        }
    }

    public void start() {
        mFrames = new Vector<GifFrame>();
        mFrameCount = 0;
        mFrameindex = 0;
        mDecoderState = STATE_START;
    }

    public void stop(){
        if (mFrames != null) {
            mFrames = null;
        }
    }

    public void startDecode(String mPath){
        readHeader(getIStream(mPath));

        DecodeGifThread decodeGifThread = new DecodeGifThread(mPath);
        decodeGifThread.start();
    }

    public void recycle() {
        if (mFrames != null && mFrames.size() != 0) {
            for (int i = 0; i < mFrames.size(); i++) {
                try {
                    Bitmap b = mFrames.get(i).image;
                    if (b != null && b.isRecycled()) {
                        b.recycle();
                        b = null;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    break;
                }
            }
        }
    }

    public int getState() {
        if (mGIF == null || mFrames == null) {
            return STATE_ERROR;
        }
        return mDecoderState;
    }

    public int getFrameindex() {
        return mFrameindex;
    }

    public void setFrameindex(int frameindex) {
        this.mFrameindex = frameindex;
        if (frameindex > (mFrameCount - 1)) {
            frameindex = 0;
        }
    }

    public int nextDelay() {
        return ((GifFrame) mFrames.elementAt(mFrameindex)).delay;
    }

    public Bitmap getFrame(int n) {
        Bitmap b = null;
        if ((n >= 0) && (n < mFrameCount)) {
            b = ((GifFrame) mFrames.elementAt(n)).image;
        }
        return b;
    }

    public Bitmap nextBitmap() {
        mFrameindex++;
        if (mFrameindex > (mFrameCount - 1)) {
            mFrameindex = 0;
        }
        return ((GifFrame) mFrames.elementAt(mFrameindex)).image;
    }

    public static class GifFrame {
        public Bitmap image;
        public int delay;

        public GifFrame(Bitmap im, int del) {
            image = im;
            delay = del;
        }
        public GifFrame() {
        }
    }
}

