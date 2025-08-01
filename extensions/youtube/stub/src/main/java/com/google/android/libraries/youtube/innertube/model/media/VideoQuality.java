package com.google.android.libraries.youtube.innertube.model.media;

public abstract class VideoQuality implements Comparable<VideoQuality> {
    public abstract String patch_getQualityName();

    public abstract int patch_getResolution();
}

