package com.amazon.avod.media.playback;

public interface VideoPlayer {
    long getCurrentPosition();

    void seekTo(long positionMs);
    
    void pause();
    
    void play();
    
    boolean isPlaying();
}