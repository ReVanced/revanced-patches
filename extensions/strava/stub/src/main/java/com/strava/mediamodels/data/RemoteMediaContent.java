package com.strava.mediamodels.data;

import java.util.SortedMap;

public interface RemoteMediaContent extends MediaContent {
    MediaDimension getLargestSize();

    String getLargestUrl();

    SortedMap<Integer, MediaDimension> getSizes();

    String getSmallestUrl();

    RemoteMediaStatus getStatus();

    SortedMap<Integer, String> getUrls();
}
