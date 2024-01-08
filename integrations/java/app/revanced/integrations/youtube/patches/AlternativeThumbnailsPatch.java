package app.revanced.integrations.youtube.patches;

import android.net.Uri;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.chromium.net.impl.CronetUrlRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static app.revanced.integrations.shared.StringRef.str;

/**
 * Alternative YouTube thumbnails.
 * <p>
 * Can show YouTube provided screen captures of beginning/middle/end of the video.
 * (ie: sd1.jpg, sd2.jpg, sd3.jpg).
 * <p>
 * Or can show crowd-sourced thumbnails provided by DeArrow (<a href="http://dearrow.ajay.app">...</a>).
 * <p>
 * Or can use DeArrow and fall back to screen captures if DeArrow is not available.
 * <p>
 * Has an additional option to use 'fast' video still thumbnails,
 * where it forces sd thumbnail quality and skips verifying if the alt thumbnail image exists.
 * The UI loading time will be the same or better than using original thumbnails,
 * but thumbnails will initially fail to load for all live streams, unreleased, and occasionally very old videos.
 * If a failed thumbnail load is reloaded (ie: scroll off, then on screen), then the original thumbnail
 * is reloaded instead.  Fast thumbnails requires using SD or lower thumbnail resolution,
 * because a noticeable number of videos do not have hq720 and too much fail to load.
 * <p>
 * Ideas for improvements:
 * - Selectively allow using original thumbnails in some situations,
 *   such as videos subscription feed, watch history, or in search results.
 * - Save to a temporary file the video id's verified to have alt thumbnails.
 *   This would speed up loading the watch history and users saved playlists.
 */
@SuppressWarnings("unused")
public final class AlternativeThumbnailsPatch {

    private static final Uri dearrowApiUri;

    /**
     * The scheme and host of {@link #dearrowApiUri}.
     */
    private static final String deArrowApiUrlPrefix;

    /**
     * How long to temporarily turn off DeArrow if it fails for any reason.
     */
    private static final long DEARROW_FAILURE_API_BACKOFF_MILLISECONDS = 5 * 60 * 1000; // 5 Minutes.

    /**
     * If non zero, then the system time of when DeArrow API calls can resume.
     */
    private static volatile long timeToResumeDeArrowAPICalls;

    static {
        dearrowApiUri = validateSettings();
        final int port = dearrowApiUri.getPort();
        String portString = port == -1 ? "" : (":" + port);
        deArrowApiUrlPrefix = dearrowApiUri.getScheme() + "://" + dearrowApiUri.getHost() + portString + "/";
        Logger.printDebug(() -> "Using DeArrow API address: " + deArrowApiUrlPrefix);
    }

    /**
     * Fix any bad imported data.
     */
    private static Uri validateSettings() {
        final int altThumbnailType = Settings.ALT_THUMBNAIL_STILLS_TIME.get();
        if (altThumbnailType < 1 || altThumbnailType > 3) {
            Utils.showToastLong("Invalid Alternative still thumbnail type: "
                    + altThumbnailType + ". Using default");
            Settings.ALT_THUMBNAIL_STILLS_TIME.resetToDefault();
        }

        Uri apiUri = Uri.parse(Settings.ALT_THUMBNAIL_DEARROW_API_URL.get());
        // Cannot use unsecured 'http', otherwise the connections fail to start and no callbacks hooks are made.
        String scheme = apiUri.getScheme();
        if (scheme == null || scheme.equals("http") || apiUri.getHost() == null) {
            Utils.showToastLong("Invalid DeArrow API URL. Using default");
            Settings.ALT_THUMBNAIL_DEARROW_API_URL.resetToDefault();
            return validateSettings();
        }
        return apiUri;
    }

    private static boolean usingDeArrow() {
        return Settings.ALT_THUMBNAIL_DEARROW.get();
    }

    private static boolean usingVideoStills() {
        return Settings.ALT_THUMBNAIL_STILLS.get();
    }

    /**
     * Build the alternative thumbnail url using YouTube provided still video captures.
     *
     * @param decodedUrl Decoded original thumbnail request url.
     * @return The alternative thumbnail url, or the original url. Both without tracking parameters.
     */
    @NonNull
    private static String buildYoutubeVideoStillURL(@NonNull DecodedThumbnailUrl decodedUrl,
                                                    @NonNull ThumbnailQuality qualityToUse) {
        String sanitizedReplacement = decodedUrl.createStillsUrl(qualityToUse, false);
        if (VerifiedQualities.verifyAltThumbnailExist(decodedUrl.videoId, qualityToUse, sanitizedReplacement)) {
            return sanitizedReplacement;
        }
        return decodedUrl.sanitizedUrl;
    }

    /**
     * Build the alternative thumbnail url using DeArrow thumbnail cache.
     *
     * @param videoId ID of the video to get a thumbnail of.  Can be any video (regular or Short).
     * @param fallbackUrl URL to fall back to in case.
     * @return The alternative thumbnail url, without tracking parameters.
     */
    @NonNull
    private static String buildDeArrowThumbnailURL(String videoId, String fallbackUrl) {
        // Build thumbnail request url.
        // See https://github.com/ajayyy/DeArrowThumbnailCache/blob/29eb4359ebdf823626c79d944a901492d760bbbc/app.py#L29.
        return dearrowApiUri
                .buildUpon()
                .appendQueryParameter("videoID", videoId)
                .appendQueryParameter("redirectUrl", fallbackUrl)
                .build()
                .toString();
    }

    private static boolean urlIsDeArrow(@NonNull String imageUrl) {
        return imageUrl.startsWith(deArrowApiUrlPrefix);
    }

    /**
     * @return If this client has not recently experienced any DeArrow API errors.
     */
    private static boolean canUseDeArrowAPI() {
        if (timeToResumeDeArrowAPICalls == 0) {
            return true;
        }
        if (timeToResumeDeArrowAPICalls < System.currentTimeMillis()) {
            Logger.printDebug(() -> "Resuming DeArrow API calls");
            timeToResumeDeArrowAPICalls = 0;
            return true;
        }
        return false;
    }

    private static void handleDeArrowError(@NonNull String url, int statusCode) {
        Logger.printDebug(() -> "Encountered DeArrow error.  Url: " + url);
        final long now = System.currentTimeMillis();
        if (timeToResumeDeArrowAPICalls < now) {
            timeToResumeDeArrowAPICalls = now + DEARROW_FAILURE_API_BACKOFF_MILLISECONDS;
            if (Settings.ALT_THUMBNAIL_DEARROW_CONNECTION_TOAST.get()) {
                String toastMessage = (statusCode != 0)
                        ? str("revanced_alt_thumbnail_dearrow_error", statusCode)
                        : str("revanced_alt_thumbnail_dearrow_error_generic");
                Utils.showToastLong(toastMessage);
            }
        }
    }

    /**
     * Injection point.  Called off the main thread and by multiple threads at the same time.
     *
     * @param originalUrl Image url for all url images loaded, including video thumbnails.
     */
    public static String overrideImageURL(String originalUrl) {
        try {
            final boolean usingDeArrow = usingDeArrow();
            final boolean usingVideoStills = usingVideoStills();
            if (!usingDeArrow && !usingVideoStills) {
                return originalUrl;
            }

            final var decodedUrl = DecodedThumbnailUrl.decodeImageUrl(originalUrl);
            if (decodedUrl == null) {
                return originalUrl; // Not a thumbnail.
            }

            Logger.printDebug(() -> "Original url: " + decodedUrl.sanitizedUrl);

            ThumbnailQuality qualityToUse = ThumbnailQuality.getQualityToUse(decodedUrl.imageQuality);
            if (qualityToUse == null) {
                // Thumbnail is a Short or a Storyboard image used for seekbar thumbnails (must not replace these).
                return originalUrl;
            }

            String sanitizedReplacementUrl;
            final boolean includeTracking;
            if (usingDeArrow && canUseDeArrowAPI()) {
                includeTracking = false; // Do not include view tracking parameters with API call.
                final String fallbackUrl = usingVideoStills
                        ? buildYoutubeVideoStillURL(decodedUrl, qualityToUse)
                        : decodedUrl.sanitizedUrl;

                sanitizedReplacementUrl = buildDeArrowThumbnailURL(decodedUrl.videoId, fallbackUrl);
            } else if (usingVideoStills) {
                includeTracking = true; // Include view tracking parameters if present.
                sanitizedReplacementUrl = buildYoutubeVideoStillURL(decodedUrl, qualityToUse);
            } else {
                return originalUrl; // Recently experienced DeArrow failure and video stills are not enabled.
            }

            // Do not log any tracking parameters.
            Logger.printDebug(() -> "Replacement url: " + sanitizedReplacementUrl);

            return includeTracking
                    ? sanitizedReplacementUrl + decodedUrl.viewTrackingParameters
                    : sanitizedReplacementUrl;
        } catch (Exception ex) {
            Logger.printException(() -> "overrideImageURL failure", ex);
            return originalUrl;
        }
    }

    /**
     * Injection point.
     * <p>
     * Cronet considers all completed connections as a success, even if the response is 404 or 5xx.
     */
    public static void handleCronetSuccess(UrlRequest request, @NonNull UrlResponseInfo responseInfo) {
        try {
            final int statusCode = responseInfo.getHttpStatusCode();
            if (statusCode == 200) {
                return;
            }

            String url = responseInfo.getUrl();

            if (usingDeArrow() && urlIsDeArrow(url)) {
                Logger.printDebug(() -> "handleCronetSuccess, statusCode: " + statusCode);
                if (statusCode == 304) {
                    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/304
                    return; // Normal response.
                }
                handleDeArrowError(url, statusCode);
                return;
            }

            if (usingVideoStills() && statusCode == 404) {
                // Fast alt thumbnails is enabled and the thumbnail is not available.
                // The video is:
                // - live stream
                // - upcoming unreleased video
                // - very old
                // - very low view count
                // Take note of this, so if the image reloads the original thumbnail will be used.
                DecodedThumbnailUrl decodedUrl = DecodedThumbnailUrl.decodeImageUrl(url);
                if (decodedUrl == null) {
                    return; // Not a thumbnail.
                }

                Logger.printDebug(() -> "handleCronetSuccess, image not available: " + url);

                ThumbnailQuality quality = ThumbnailQuality.altImageNameToQuality(decodedUrl.imageQuality);
                if (quality == null) {
                    // Video is a short or a seekbar thumbnail, but somehow did not load.  Should not happen.
                    Logger.printDebug(() -> "Failed to recognize image quality of url: " + decodedUrl.sanitizedUrl);
                    return;
                }

                VerifiedQualities.setAltThumbnailDoesNotExist(decodedUrl.videoId, quality);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "Callback success error", ex);
        }
    }

    /**
     * Injection point.
     * <p>
     * To test failure cases, try changing the API URL to each of:
     * - A non-existent domain.
     * - A url path of something incorrect (ie: /v1/nonExistentEndPoint).
     * <p>
     * Cronet uses a very timeout (several minutes), so if the API never responds this hook can take a while to be called.
     * But this does not appear to be a problem, as the DeArrow API has not been observed to 'go silent'
     * Instead if there's a problem it returns an error code status response, which is handled in this patch.
     */
    public static void handleCronetFailure(UrlRequest request,
                                           @Nullable UrlResponseInfo responseInfo,
                                           IOException exception) {
        try {
            if (usingDeArrow()) {
                String url = ((CronetUrlRequest) request).getHookedUrl();
                if (urlIsDeArrow(url)) {
                    Logger.printDebug(() -> "handleCronetFailure, exception: " + exception);
                    final int statusCode = (responseInfo != null)
                            ? responseInfo.getHttpStatusCode()
                            : 0;
                    handleDeArrowError(url, statusCode);
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "Callback failure error", ex);
        }
    }

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
                    // 'custom' thumbnails set by the content creator.
                    // These show up in place of regular thumbnails
                    // and seem to be limited to [1, 3] range.
                    originalNameToEnum.put(quality.originalName + "_custom_" + i, quality);

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

            final boolean useFastQuality = Settings.ALT_THUMBNAIL_STILLS_FAST.get();
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
            return altImageName + Settings.ALT_THUMBNAIL_STILLS_TIME.get();
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
                return size() > CACHE_LIMIT; // Evict the oldest entry if over the cache limit.
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
            VerifiedQualities verified = getVerifiedQualities(videoId, Settings.ALT_THUMBNAIL_STILLS_FAST.get());
            if (verified == null) return true; // Fast alt thumbnails is enabled.
            return verified.verifyYouTubeThumbnailExists(videoId, quality, imageUrl);
        }

        static void setAltThumbnailDoesNotExist(@NonNull String videoId, @NonNull ThumbnailQuality quality) {
            VerifiedQualities verified = getVerifiedQualities(videoId, false);
            //noinspection ConstantConditions
            verified.setQualityVerified(videoId, quality, false);
        }

        /**
         * Highest quality verified as existing.
         */
        @Nullable
        private ThumbnailQuality highestQualityVerified;
        /**
         * Lowest quality verified as not existing.
         */
        @Nullable
        private ThumbnailQuality lowestQualityNotAvailable;

        /**
         * System time, of when to invalidate {@link #lowestQualityNotAvailable}.
         * Used only if fast mode is not enabled.
         */
        private long timeToReVerifyLowestQuality;

        private synchronized void setQualityVerified(String videoId, ThumbnailQuality quality, boolean isVerified) {
            if (isVerified) {
                if (highestQualityVerified == null || highestQualityVerified.ordinal() < quality.ordinal()) {
                    highestQualityVerified = quality;
                }
            } else {
                if (lowestQualityNotAvailable == null || lowestQualityNotAvailable.ordinal() > quality.ordinal()) {
                    lowestQualityNotAvailable = quality;
                    timeToReVerifyLowestQuality = System.currentTimeMillis() + NOT_AVAILABLE_TIMEOUT_MILLISECONDS;
                }
                Logger.printDebug(() -> quality + " not available for video: " + videoId);
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

            final boolean fastQuality = Settings.ALT_THUMBNAIL_STILLS_FAST.get();
            if (lowestQualityNotAvailable != null && lowestQualityNotAvailable.ordinal() <= quality.ordinal()) {
                if (fastQuality || System.currentTimeMillis() < timeToReVerifyLowestQuality) {
                    return false; // Previously verified as not existing.
                }
                // Enough time has passed, and should re-verify again.
                Logger.printDebug(() -> "Resetting lowest verified quality for: " + videoId);
                lowestQualityNotAvailable = null;
            }

            if (fastQuality) {
                return true; // Unknown if it exists or not.  Use the URL anyways and update afterwards if loading fails.
            }

            boolean imageFileFound;
            try {
                Logger.printDebug(() -> "Verifying image: " + imageUrl);
                // This hooked code is running on a low priority thread, and it's slightly faster
                // to run the url connection thru the integrations thread pool which runs at the highest priority.
                final long start = System.currentTimeMillis();
                imageFileFound = Utils.submitOnBackgroundThread(() -> {
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
                        Logger.printDebug(() -> "Unexpected response code: " + responseCode + " for url: " + imageUrl);
                    }
                    return false;
                }).get();
                Logger.printDebug(() -> "Alt verification took: " + (System.currentTimeMillis() - start) + "ms");
            } catch (ExecutionException | InterruptedException ex) {
                Logger.printInfo(() -> "Could not verify alt url: " + imageUrl, ex);
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

        final String originalFullUrl;
        /** Full usable url, but stripped of any tracking information. */
        final String sanitizedUrl;
        /** Url up to the video ID. */
        final String urlPrefix;
        final String videoId;
        /** Quality, such as hq720 or sddefault. */
        final String imageQuality;
        /** JPG or WEBP */
        final String imageExtension;
        /** User view tracking parameters, only present on some images. */
        final String viewTrackingParameters;

        DecodedThumbnailUrl(String fullUrl, int videoIdStartIndex, int videoIdEndIndex,
                            int imageSizeStartIndex, int imageSizeEndIndex, int imageExtensionEndIndex) {
            originalFullUrl = fullUrl;
            sanitizedUrl = fullUrl.substring(0, imageExtensionEndIndex);
            urlPrefix = fullUrl.substring(0, videoIdStartIndex);
            videoId = fullUrl.substring(videoIdStartIndex, videoIdEndIndex);
            imageQuality = fullUrl.substring(imageSizeStartIndex, imageSizeEndIndex);
            imageExtension = fullUrl.substring(imageSizeEndIndex + 1, imageExtensionEndIndex);
            viewTrackingParameters = (imageExtensionEndIndex == fullUrl.length())
                    ? "" : fullUrl.substring(imageExtensionEndIndex);
        }

        /** @noinspection SameParameterValue*/
        String createStillsUrl(@NonNull ThumbnailQuality qualityToUse, boolean includeViewTracking) {
            // Images could be upgraded to webp if they are not already, but this fails quite often,
            // especially for new videos uploaded in the last hour.
            // And even if alt webp images do exist, sometimes they can load much slower than the original jpg alt images.
            // (as much as 4x slower has been observed, despite the alt webp image being a smaller file).
            StringBuilder builder = new StringBuilder(originalFullUrl.length() + 2);
            builder.append(urlPrefix);
            builder.append(videoId).append('/');
            builder.append(qualityToUse.getAltImageNameToUse());
            builder.append('.').append(imageExtension);
            if (includeViewTracking) {
                builder.append(viewTrackingParameters);
            }
            return builder.toString();
        }
    }
}
