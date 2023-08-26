package app.revanced.integrations.patches;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.chromium.net.UrlResponseInfo;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Alternative YouTube thumbnails, showing the beginning/middle/end of the video.
 * (ie: sd1.jpg, sd2.jpg, sd3.jpg).
 *
 * Has an additional option to use 'fast' thumbnails,
 * where it forces sd thumbnail quality and skips verifying if the alt thumbnail image exists.
 * The UI loading time will be the same or better than using the the original thumbnails,
 * but thumbnails will initially fail to load for all live streams, unreleased, and occasionally very old videos.
 * If a failed thumbnail load is reloaded (ie: scroll off, then on screen), then the original thumbnail
 * is reloaded instead.  Fast thumbnails requires using SD or lower thumbnail resolution,
 * because a noticeable number of videos do not have hq720 and too many fail to load.
 *
 * Ideas for improvements:
 * - Selectively allow using original thumbnails in some situations,
 *   such as videos subscription feed, watch history, or in search results.
 * - Save to a temporary file the video id's verified to have alt thumbnails.
 *   This would speed up loading the watch history and users saved playlists.
 */
public final class AlternativeThumbnailsPatch {

    private enum ThumbnailQuality {
        // In order of lowest to highest resolution.
        DEFAULT("default", ""), // effective alt name is 1.jpg, 2.jpg, 3.jpg
        MQDEFAULT("mqdefault", "mq"),
        HQDEFAULT("hqdefault", "hq"),
        SDDEFAULT("sddefault", "sd"),
        HQ720("hq720", "hq720_"),
        MAXRESDEFAULT("maxresdefault", "maxres");

        /**
         * Lookup map of original name to enum.
         */
        private static final Map<String, ThumbnailQuality> originalNameToEnum = new HashMap<>();

        /**
         * Lookup map of alt name to enum.  ie: "hq720_1" to {@link #HQ720}.
         */
        private static final Map<String, ThumbnailQuality> altNameToEnum = new HashMap<>();

        static {
            for (ThumbnailQuality quality : values()) {
                originalNameToEnum.put(quality.originalName, quality);

                for (int i = 1; i <= 3; i++) {
                    altNameToEnum.put(quality.altImageName + i, quality);
                }
            }
        }

        /**
         * Convert an alt image name to enum.
         * ie: "hq720_2" returns {@link #HQ720}.
         */
        @Nullable
        static ThumbnailQuality altImageNameToQuality(@NonNull String altImageName) {
            return altNameToEnum.get(altImageName);
        }

        /**
         * Original quality to effective alt quality to use.
         * ie: If fast alt image is enabled, then "hq720" returns {@link #SDDEFAULT}.
         */
        @Nullable
        static ThumbnailQuality getQualityToUse(@NonNull String originalSize) {
            ThumbnailQuality quality = originalNameToEnum.get(originalSize);
            if (quality == null) {
                return null; // Not a thumbnail for a regular video.
            }

            final boolean useFastQuality = SettingsEnum.ALT_THUMBNAIL_FAST_QUALITY.getBoolean();
            switch (quality) {
                case SDDEFAULT:
                    // SD alt images have somewhat worse quality with washed out color and poor contrast.
                    // But the 720 images look much better and don't suffer from these issues.
                    // For unknown reasons, the 720 thumbnails are used only for the home feed,
                    // while SD is used for the search and subscription feed
                    // (even though search and subscriptions use the exact same layout as the home feed).
                    // Of note, this image quality issue only appears with the alt thumbnail images,
                    // and the regular thumbnails have identical color/contrast quality for all sizes.
                    // Fix this by falling thru and upgrading SD to 720.
                case HQ720:
                    if (useFastQuality) {
                        return SDDEFAULT; // SD is max resolution for fast alt images.
                    }
                    return HQ720;
                case MAXRESDEFAULT:
                    if (useFastQuality) {
                        return SDDEFAULT;
                    }
                    return MAXRESDEFAULT;
                default:
                    return quality;
            }
        }

        final String originalName;
        final String altImageName;

        ThumbnailQuality(String originalName, String altImageName) {
            this.originalName = originalName;
            this.altImageName = altImageName;
        }

        String getAltImageNameToUse() {
            return altImageName + SettingsEnum.ALT_THUMBNAIL_TYPE.getInt();
        }
    }

    /**
     * Uses HTTP HEAD requests to verify and keep track of which thumbnail sizes
     * are available and not available.
     */
    private static class VerifiedQualities {
        /**
         * After a quality is verified as not available, how long until the quality is re-verified again.
         * Used only if fast mode is not enabled. Intended for live streams and unreleased videos
         * that are now finished and available (and thus, the alt thumbnails are also now available).
         */
        private static final long NOT_AVAILABLE_TIMEOUT_MILLISECONDS = 10 * 60 * 1000; // 10 minutes.

        /**
         * Cache used to verify if an alternative thumbnails exists for a given video id.
         */
        @GuardedBy("itself")
        private static final Map<String, VerifiedQualities> altVideoIdLookup = new LinkedHashMap<>(100) {
            private static final int CACHE_LIMIT = 1000;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_LIMIT; // Evict oldest entry if over the cache limit.
            }
        };

        private static VerifiedQualities getVerifiedQualities(@NonNull String videoId, boolean returnNullIfDoesNotExist) {
            synchronized (altVideoIdLookup) {
                VerifiedQualities verified = altVideoIdLookup.get(videoId);
                if (verified == null) {
                    if (returnNullIfDoesNotExist) {
                        return null;
                    }
                    verified = new VerifiedQualities();
                    altVideoIdLookup.put(videoId, verified);
                }
                return verified;
            }
        }

        static boolean verifyAltThumbnailExist(@NonNull String videoId, @NonNull ThumbnailQuality quality,
                                               @NonNull String imageUrl) {
            VerifiedQualities verified = getVerifiedQualities(videoId, SettingsEnum.ALT_THUMBNAIL_FAST_QUALITY.getBoolean());
            if (verified == null) return true; // Fast alt thumbnails is enabled.
            return verified.verifyYouTubeThumbnailExists(videoId, quality, imageUrl);
        }

        static void setAltThumbnailDoesNotExist(@NonNull String videoId, @NonNull ThumbnailQuality quality) {
            VerifiedQualities verified = getVerifiedQualities(videoId, false);
            verified.setQualityVerified(videoId, quality, false);
        }

        /**
         * Highest quality verified as existing.
         */
        @Nullable
        ThumbnailQuality highestQualityVerified;
        /**
         * Lowest quality verified as not existing.
         */
        @Nullable
        ThumbnailQuality lowestQualityNotAvailable;

        /**
         * System time, of when to invalidate {@link #lowestQualityNotAvailable}.
         * Used only if fast mode is not enabled.
         */
        long timeToReVerifyLowestQuality;

        synchronized void setQualityVerified(String videoId, ThumbnailQuality quality, boolean isVerified) {
            if (isVerified) {
                if (highestQualityVerified == null || highestQualityVerified.ordinal() < quality.ordinal()) {
                    highestQualityVerified = quality;
                }
            } else {
                if (lowestQualityNotAvailable == null || lowestQualityNotAvailable.ordinal() > quality.ordinal()) {
                    lowestQualityNotAvailable = quality;
                    timeToReVerifyLowestQuality = System.currentTimeMillis() + NOT_AVAILABLE_TIMEOUT_MILLISECONDS;
                }
                LogHelper.printDebug(() -> quality + " not available for video: " + videoId);
            }
        }

        /**
         * Verify if a video alt thumbnail exists.  Does so by making a minimal HEAD http request.
         */
        synchronized boolean verifyYouTubeThumbnailExists(@NonNull String videoId, @NonNull ThumbnailQuality quality,
                                                          @NonNull String imageUrl) {
            if (highestQualityVerified != null && highestQualityVerified.ordinal() >= quality.ordinal()) {
                return true; // Previously verified as existing.
            }

            final boolean fastQuality = SettingsEnum.ALT_THUMBNAIL_FAST_QUALITY.getBoolean();
            if (lowestQualityNotAvailable != null && lowestQualityNotAvailable.ordinal() <= quality.ordinal()) {
                if (fastQuality || System.currentTimeMillis() < timeToReVerifyLowestQuality) {
                    return false; // Previously verified as not existing.
                }
                // Enough time has passed, and should re-verify again.
                LogHelper.printDebug(() -> "Resetting lowest verified quality for: " + videoId);
                lowestQualityNotAvailable = null;
            }

            if (fastQuality) {
                return true; // Unknown if it exists or not.  Use the URL anyways and update afterwards if loading fails.
            }

            boolean imageFileFound;
            try {
                LogHelper.printDebug(() -> "Verifying image: " + imageUrl);
                // This hooked code is running on a low priority thread, and it's slightly faster
                // to run the url connection thru the integrations thread pool which runs at the highest priority.
                final long start = System.currentTimeMillis();
                imageFileFound = ReVancedUtils.submitOnBackgroundThread(() -> {
                    final int connectionTimeoutMillis = 5000;
                    HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    connection.setConnectTimeout(connectionTimeoutMillis);
                    connection.setReadTimeout(connectionTimeoutMillis);
                    connection.setRequestMethod("HEAD");
                    // Even with a HEAD request, the response is the same size as a full GET request.
                    // Using an empty range fixes this.
                    connection.setRequestProperty("Range", "bytes=0-0");
                    final int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        String contentType = connection.getContentType();
                        return (contentType != null && contentType.startsWith("image"));
                    }
                    if (responseCode != HttpURLConnection.HTTP_NOT_FOUND) {
                        LogHelper.printDebug(() -> "Unexpected response code: " + responseCode + " for url: " + imageUrl);
                    }
                    return false;
                }).get();
                LogHelper.printDebug(() -> "Alt verification took: " + (System.currentTimeMillis() - start) + "ms");
            } catch (ExecutionException | InterruptedException ex) {
                LogHelper.printInfo(() -> "Could not verify alt url: " + imageUrl, ex);
                imageFileFound = false;
            }

            setQualityVerified(videoId, quality, imageFileFound);
            return imageFileFound;
        }
    }

    /**
     * YouTube video thumbnail url, decoded into it's relevant parts.
     */
    private static class DecodedThumbnailUrl {
        /**
         * YouTube thumbnail URL prefix. Can be '/vi/' or '/vi_webp/'
         */
        private static final String YOUTUBE_THUMBNAIL_PREFIX = "https://i.ytimg.com/vi";

        @Nullable
        static DecodedThumbnailUrl decodeImageUrl(String url) {
            final int videoIdStartIndex = url.indexOf('/', YOUTUBE_THUMBNAIL_PREFIX.length()) + 1;
            if (videoIdStartIndex <= 0) return null;
            final int videoIdEndIndex = url.indexOf('/', videoIdStartIndex);
            if (videoIdEndIndex < 0) return null;
            final int imageSizeStartIndex = videoIdEndIndex + 1;
            final int imageSizeEndIndex = url.indexOf('.', imageSizeStartIndex);
            if (imageSizeEndIndex < 0) return null;
            int imageExtensionEndIndex = url.indexOf('?', imageSizeEndIndex);
            if (imageExtensionEndIndex < 0) imageExtensionEndIndex = url.length();
            return new DecodedThumbnailUrl(url, videoIdStartIndex, videoIdEndIndex,
                    imageSizeStartIndex, imageSizeEndIndex, imageExtensionEndIndex);
        }

        /** Full usable url, but stripped of any tracking information. */
        final String sanitizedUrl;
        /** Url up to the video id. */
        final String urlPrefix;
        final String videoId;
        /** Quality, such as hq720 or sddefault. */
        final String imageQuality;
        /** jpg or webp */
        final String imageExtension;
        /** User view tracking parameters, only present on some images. */
        final String urlTrackingParameters;

        private DecodedThumbnailUrl(String fullUrl, int videoIdStartIndex, int videoIdEndIndex,
                                    int imageSizeStartIndex, int imageSizeEndIndex, int imageExtensionEndIndex) {
            sanitizedUrl = fullUrl.substring(0, imageExtensionEndIndex);
            urlPrefix = fullUrl.substring(0, videoIdStartIndex);
            videoId = fullUrl.substring(videoIdStartIndex, videoIdEndIndex);
            imageQuality = fullUrl.substring(imageSizeStartIndex, imageSizeEndIndex);
            imageExtension = fullUrl.substring(imageSizeEndIndex + 1, imageExtensionEndIndex);
            urlTrackingParameters = (imageExtensionEndIndex == fullUrl.length())
                    ? "" : fullUrl.substring(imageExtensionEndIndex);
        }
    }

    static {
        // Fix any bad imported data.
        final int altThumbnailType = SettingsEnum.ALT_THUMBNAIL_TYPE.getInt();
        if (altThumbnailType < 1 || altThumbnailType > 3) {
            LogHelper.printException(() -> "Invalid alt thumbnail type: " + altThumbnailType);
            SettingsEnum.ALT_THUMBNAIL_TYPE.saveValue(SettingsEnum.ALT_THUMBNAIL_TYPE.defaultValue);
        }
    }

    /**
     * Injection point.  Called off the main thread and by multiple threads at the same time.
     *
     * @param originalUrl Image url for all url images loaded, including video thumbnails.
     */
    public static String overrideImageURL(String originalUrl) {
        try {
            if (!SettingsEnum.ALT_THUMBNAIL.getBoolean()) {
                return originalUrl;
            }
            DecodedThumbnailUrl decodedUrl = DecodedThumbnailUrl.decodeImageUrl(originalUrl);
            if (decodedUrl == null) {
                return originalUrl; // Not a thumbnail.
            }

            // Keep any tracking parameters out of the logs, and log only the base url.
            LogHelper.printDebug(() -> "Original url: " + decodedUrl.sanitizedUrl);

            ThumbnailQuality qualityToUse = ThumbnailQuality.getQualityToUse(decodedUrl.imageQuality);
            if (qualityToUse == null) return originalUrl; // Video is a short.

            // Images could be upgraded to webp if they are not already, but this fails quite often,
            // especially for new videos uploaded in the last hour.
            // And even if alt webp images do exist, sometimes they can load much slower than the original jpg alt images.
            // (as much as 4x slower has been observed, despite the alt webp image being a smaller file).

            StringBuilder builder = new StringBuilder(originalUrl.length() + 2);
            builder.append(decodedUrl.urlPrefix);
            builder.append(decodedUrl.videoId).append('/');
            builder.append(qualityToUse.getAltImageNameToUse());
            builder.append('.').append(decodedUrl.imageExtension);

            String sanitizedReplacement = builder.toString();
            if (!VerifiedQualities.verifyAltThumbnailExist(decodedUrl.videoId, qualityToUse, sanitizedReplacement)) {
                return originalUrl;
            }

            LogHelper.printDebug(() -> "Replaced url: " + sanitizedReplacement);

            // URL tracking parameters. Presumably they are to determine if a user has viewed a thumbnail.
            // This likely is used for recommendations, so they are retained if present.
            builder.append(decodedUrl.urlTrackingParameters);
            return builder.toString();
        } catch (Exception ex) {
            LogHelper.printException(() -> "Alt thumbnails failure", ex);
            return originalUrl;
        }
    }

    /**
     * Injection point.
     *
     * Cronet considers all completed connections as a success, even if the response is 404 or 5xx.
     */
    public static void handleCronetSuccess(@NonNull UrlResponseInfo responseInfo) {
        try {
            if (responseInfo.getHttpStatusCode() == 404 && SettingsEnum.ALT_THUMBNAIL.getBoolean()) {
                // Fast alt thumbnails is enabled and the thumbnail is not available.
                // The video is:
                // - live stream
                // - upcoming unreleased video
                // - very old
                // - very low view count
                // Take note of this, so if the image reloads the original thumbnail will be used.
                DecodedThumbnailUrl decodedUrl = DecodedThumbnailUrl.decodeImageUrl(responseInfo.getUrl());
                if (decodedUrl == null) {
                    return; // Not a thumbnail.
                }

                ThumbnailQuality quality = ThumbnailQuality.altImageNameToQuality(decodedUrl.imageQuality);
                if (quality == null) {
                    // Video is a short or unknown quality, but the url returned 404.  Should never happen.
                    LogHelper.printDebug(() -> "Failed to load unknown url: " + decodedUrl.sanitizedUrl);
                    return;
                }

                VerifiedQualities.setAltThumbnailDoesNotExist(decodedUrl.videoId, quality);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "Alt thumbnails callback failure", ex);
        }
    }

}
