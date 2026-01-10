package com.strava.core.data;

import java.io.Serializable;

public final class MediaDimension implements Comparable<MediaDimension>, Serializable {
    private final int height;
    private final int width;

    public MediaDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public float getHeightScale() {
        if (width <= 0 || height <= 0) {
            return 1f;
        }
        return height / width;
    }

    public int getWidth() {
        return width;
    }

    public float getWidthScale() {
        if (width <= 0 || height <= 0) {
            return 1f;
        }
        return width / height;
    }

    public boolean isLandscape() {
        return width > 0 && width >= height;
    }

    @Override
    public int compareTo(MediaDimension other) {
        return 0;
    }
}
