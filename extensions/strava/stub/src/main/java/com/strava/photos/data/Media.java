package com.strava.photos.data;

import com.strava.mediamodels.data.MediaDimension;
import com.strava.mediamodels.data.MediaType;
import com.strava.mediamodels.data.RemoteMediaContent;
import com.strava.mediamodels.data.RemoteMediaStatus;

import java.util.SortedMap;

public abstract class Media implements RemoteMediaContent {
    public static final class Photo extends Media {
        private final Long activityId;
        private final String activityName;
        private final long athleteId;
        private String caption;
        private final String createdAt;
        private final String createdAtLocal;
        private final String cursor;
        private final String id;
        private final SortedMap<Integer, MediaDimension> sizes;
        private final RemoteMediaStatus status;
        private final String tag;
        private final MediaType type;
        private final SortedMap<Integer, String> urls;

        @Override
        public Long getActivityId() {
            return activityId;
        }

        @Override
        public String getActivityName() {
            return activityName;
        }

        @Override
        public long getAthleteId() {
            return athleteId;
        }

        @Override
        public String getCaption() {
            return caption;
        }

        @Override
        public String getCreatedAt() {
            return createdAt;
        }

        @Override
        public String getCreatedAtLocal() {
            return createdAtLocal;
        }

        @Override
        public String getCursor() {
            return cursor;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public SortedMap<Integer, MediaDimension> getSizes() {
            return sizes;
        }

        @Override
        public RemoteMediaStatus getStatus() {
            return status;
        }

        @Override
        public String getTag() {
            return tag;
        }

        @Override
        public MediaType getType() {
            return type;
        }

        @Override
        public SortedMap<Integer, String> getUrls() {
            return urls;
        }

        @Override
        public void setCaption(String caption) {
            this.caption = caption;
        }

        public Photo(String id,
                     String caption,
                     SortedMap<Integer, String> urls,
                     SortedMap<Integer, MediaDimension> sizes,
                     long athleteId,
                     String createdAt,
                     String createdAtLocal,
                     Long activityId,
                     String activityName,
                     RemoteMediaStatus status,
                     String tag,
                     String cursor) {
            this.id = id;
            this.caption = caption;
            this.urls = urls;
            this.sizes = sizes;
            this.athleteId = athleteId;
            this.createdAt = createdAt;
            this.createdAtLocal = createdAtLocal;
            this.activityId = activityId;
            this.activityName = activityName;
            this.status = status;
            this.tag = tag;
            this.cursor = cursor;
            this.type = MediaType.PHOTO;
        }
    }

    public static final class Video extends Media {
        private final Long activityId;
        private final String activityName;
        private final long athleteId;
        private String caption;
        private final String createdAt;
        private final String createdAtLocal;
        private final String cursor;
        private final Float durationSeconds;
        private final String id;
        private final SortedMap<Integer, MediaDimension> sizes;
        private final RemoteMediaStatus status;
        private final String tag;
        private final MediaType type;
        private final SortedMap<Integer, String> urls;
        private final String videoUrl;

        @Override
        public Long getActivityId() {
            return activityId;
        }

        @Override
        public String getActivityName() {
            return activityName;
        }

        @Override
        public long getAthleteId() {
            return athleteId;
        }

        @Override
        public String getCaption() {
            return caption;
        }

        @Override
        public String getCreatedAt() {
            return createdAt;
        }

        @Override
        public String getCreatedAtLocal() {
            return createdAtLocal;
        }

        @Override
        public String getCursor() {
            return cursor;
        }

        public final Float getDurationSeconds() {
            return durationSeconds;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public SortedMap<Integer, MediaDimension> getSizes() {
            return sizes;
        }

        @Override
        public RemoteMediaStatus getStatus() {
            return status;
        }

        @Override
        public String getTag() {
            return tag;
        }

        @Override
        public MediaType getType() {
            return type;
        }

        @Override
        public SortedMap<Integer, String> getUrls() {
            return urls;
        }

        public final String getVideoUrl() {
            return videoUrl;
        }

        @Override
        public void setCaption(String caption) {
            this.caption = caption;
        }

        public Video(String id,
                     String caption,
                     SortedMap<Integer, String> urls,
                     SortedMap<Integer, MediaDimension> sizes,
                     long athleteId,
                     String createdAt,
                     String createdAtLocal,
                     Long activityId,
                     String activityName,
                     RemoteMediaStatus status,
                     String videoUrl,
                     Float durationSeconds,
                     String tag,
                     String cursor) {
            this.id = id;
            this.caption = caption;
            this.urls = urls;
            this.sizes = sizes;
            this.athleteId = athleteId;
            this.createdAt = createdAt;
            this.createdAtLocal = createdAtLocal;
            this.activityId = activityId;
            this.activityName = activityName;
            this.status = status;
            this.videoUrl = videoUrl;
            this.durationSeconds = durationSeconds;
            this.tag = tag;
            this.cursor = cursor;
            this.type = MediaType.VIDEO;
        }
    }

    public abstract Long getActivityId();

    public abstract String getActivityName();

    public abstract long getAthleteId();

    public abstract String getCreatedAt();

    public abstract String getCreatedAtLocal();

    public abstract String getCursor();

    @Override
    public MediaDimension getLargestSize() {
        return null;
    }

    @Override
    public String getLargestUrl() {
        return null;
    }

    @Override
    public String getReferenceId() {
        return null;
    }

    @Override
    public String getSmallestUrl() {
        return null;
    }

    public abstract String getTag();

    private Media() {
    }
}
