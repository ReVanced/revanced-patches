package com.amazon.video.sdk.player;

public interface Player {
    float getPlaybackRate();
    
    void setPlaybackRate(float rate);

    void play();
    
    void pause();
} 