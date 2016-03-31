package com.aispeech.speaker2;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class Player implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener{
    public MediaPlayer mediaPlayer;
    public Player()
    {
        try {
            mediaPlayer =new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            Log.e("mediaPlayer", "error", e);
        }
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void setURL(String url){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playUrl()
    {
        try {
            mediaPlayer.prepare();//prepare之后自动播放
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause()
    {
        mediaPlayer.pause();
    }

    public void stop()
    {
        mediaPlayer.stop();
    }

    @Override
    /**
     * 通过onPrepared播放
     */
    public void onPrepared(MediaPlayer arg0) {
        Log.e("mediaPlayer", "onPrepared");
        arg0.start();
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        Log.e("mediaPlayer", "onCompletion");
    }
}