
package com.android.gallery3d.app;

import android.widget.VideoView;
import android.media.MediaPlayer;
import android.util.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.hardware.display.DisplayManager; 
import android.view.Display; 
import android.view.IWindowManager;
import android.os.ServiceManager;

public class VideoViewMediaPlayer extends VideoView {

    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private MovieActivity  movieActivity;

    public VideoViewMediaPlayer(Context context) {
        super(context);
        mContext = context;
    }

    public VideoViewMediaPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public VideoViewMediaPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
     
    public void setMediaPlayer(MediaPlayer play) {
        mMediaPlayer = play;
    }

    public void setMovieActivity(MovieActivity activity) {
        movieActivity = activity;
    }
 
    public void getDiplay(){ 
        DisplayManager  displayManager = new DisplayManager(mContext); 
        Display[] displays = displayManager .getDisplays(null); 
        IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
        if(movieActivity == null) {
            return;
        }
        for (Display display : displays) { 
            if(wm != null) {
                try{
                    if(movieActivity.getTaskId() == wm.getSecondDisplayTaskId()) {
                        if(display.getDisplayId() != 0){ 
                            int layerstack = display.getLayerStack(); 
                            if(mMediaPlayer != null) {
                                mMediaPlayer.setSubtitleLayerStack(layerstack);
                                return;
                            }
                        }    
                    } else {
                        if(display.getDisplayId() == 0){ 
                            int layerstack = display.getLayerStack(); 
                            if(mMediaPlayer != null) {
                                mMediaPlayer.setSubtitleLayerStack(layerstack);
                                return;
                            }
                        }   
                    }
                } catch(Exception e) {

                }           
            }    
        } 
    }
 
    @Override
    public void layout(int l, int t, int r, int b) {
        if(mMediaPlayer != null) {
            Log.v("Test","--------------layout-----------------");
        }
        super.layout(l, t, r, b);
        getDiplay();
    }

};
