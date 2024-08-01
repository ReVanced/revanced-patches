package app.revanced.extension.youtube.patches.components;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.ByteTrieSearch;
import app.revanced.extension.youtube.StringTrieSearch;
import app.revanced.extension.youtube.TrieSearch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.PlayerType;

/**
 * <pre>
 * Allows hiding home feed and search results based on keywords and/or channel names.
 *
 * Limitations:
 * - Searching for a keyword phrase will give no search results.
 *   This is because the buffer for each video contains the text the user searched for, and everything
 *   will be filtered away (even if that video title/channel does not contain any keywords).
 * - Filtering a channel name can still show Shorts from that channel in the search results.
 *   The most common Shorts layouts do not include the channel name, so they will not be filtered.
 * - Some layout component residue will remain, such as the video chapter previews for some search results.
 *   These components do not include the video title or channel name, and they
 *   appear outside the filtered components so they are not caught.
 * - Keywords are case sensitive, but some casing variation is manually added.
 *   (ie: "mr beast" automatically filters "Mr Beast" and "MR BEAST").
 * - Keywords present in the layout or video data cannot be used as filters, otherwise all videos
 *   will always be hidden.  This patch checks for some words of these words.
 */
@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.N)
final class KeywordContentFilter extends Filter {

    /**
     * Minimum keyword/phrase length to prevent excessively broad content filtering.
     */
    private static final int MINIMUM_KEYWORD_LENGTH = 3;

    /**
     * Strings found in the buffer for every videos.
     * Full strings should be specified, as they are compared using {@link String#contains(CharSequence)}.
     *
     * This list does not include every common buffer string, and this can be added/changed as needed.
     * Words must be entered with the exact casing as found in the buffer.
     */
    private static final String[] STRINGS_IN_EVERY_BUFFER = {
            // Video playback data.
            "googlevideo.com/initplayback?source=youtube", // Video url.
            "ANDROID", // Video url parameter.
            "https://i.ytimg.com/vi/", // Thumbnail url.
            "mqdefault.jpg",
            "hqdefault.jpg",
            "sddefault.jpg",
            "hq720.jpg",
            "webp",
            "_custom_", // Custom thumbnail set by video creator.
            // Video decoders.
            "OMX.ffmpeg.vp9.decoder",
            "OMX.Intel.sw_vd.vp9",
            "OMX.MTK.VIDEO.DECODER.SW.VP9",
            "OMX.google.vp9.decoder",
            "OMX.google.av1.decoder",
            "OMX.sprd.av1.decoder",
            "c2.android.av1.decoder",
            "c2.android.av1-dav1d.decoder",
            "c2.android.vp9.decoder",
            "c2.mtk.sw.vp9.decoder",
            // Analytics.
            "searchR",
            "browse-feed",
            "FEwhat_to_watch",
            "FEsubscriptions",
            "search_vwc_description_transition_key",
            "g-high-recZ",
            // Text and litho components found in the buffer that belong to path filters.
            "metadata.eml",
            "thumbnail.eml",
            "avatar.eml",
            "overflow_button.eml",
            "shorts-lockup-image",
            "shorts-lockup.overlay-metadata.secondary-text",
            "YouTubeSans-SemiBold",
            "sans-serif"
    };

    /**
     * Substrings that are always first in the identifier.
     */
    private final StringFilterGroup startsWithFilter = new StringFilterGroup(
            null, // Multiple settings are used and must be individually checked if active.
            "home_video_with_context.eml",
            "search_video_with_context.eml",
            "video_with_context.eml", // Subscription tab videos.
            "related_video_with_context.eml",
            "video_lockup_with_attachment.eml", // A/B test for subscribed video.
            "compact_video.eml",
            "inline_shorts",
            "shorts_video_cell",
            "shorts_pivot_item.eml"
    );

    /**
     * Substrings that are never at the start of the path.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final StringFilterGroup containsFilter = new StringFilterGroup(
            null,
            "modern_type_shelf_header_content.eml",
            "shorts_lockup_cell.eml", // Part of 'shorts_shelf_carousel.eml'
            "video_card.eml" // Shorts that appear in a horizontal shelf.
    );

    /**
     * Path components to not filter.  Cannot filter the buffer when these are present,
     * otherwise text in UI controls can be filtered as a keyword (such as using "Playlist" as a keyword).
     *
     * This is also a small performance improvement since
     * the buffer of the parent component was already searched and passed.
     */
    private final StringTrieSearch exceptions = new StringTrieSearch(
            "metadata.eml",
            "thumbnail.eml",
            "avatar.eml",
            "overflow_button.eml"
    );

    /**
     * Threshold for {@link #filteredVideosPercentage}
     * that indicates all or nearly all videos have been filtered.
     * This should be close to 100% to reduce false positives.
     */
    private static final float ALL_VIDEOS_FILTERED_THRESHOLD = 0.95f;

    private static final float ALL_VIDEOS_FILTERED_SAMPLE_SIZE = 50;

    private static final long ALL_VIDEOS_FILTERED_BACKOFF_MILLISECONDS = 60 * 1000; // 60 seconds

    /**
     * Rolling average of how many videos were filtered by a keyword.
     * Used to detect if a keyword passes the initial check against {@link #STRINGS_IN_EVERY_BUFFER}
     * but a keyword is still hiding all videos.
     *
     * This check can still fail if some extra UI elements pass the keywords,
     * such as the video chapter preview or any other elements.
     *
     * To test this, add a filter that appears in all videos (such as 'ovd='),
     * and open the subscription feed. In practice this does not always identify problems
     * in the home feed and search, because the home feed has a finite amount of content and
     * search results have a lot of extra video junk that is not hidden and interferes with the detection.
     */
    private volatile float filteredVideosPercentage;

    /**
     * If filtering is temporarily turned off, the time to resume filtering.
     * Field is zero if no backoff is in effect.
     */
    private volatile long timeToResumeFiltering;

    /**
     * The last value of {@link Settings#HIDE_KEYWORD_CONTENT_PHRASES}
     * parsed and loaded into {@link #bufferSearch}.
     * Allows changing the keywords without restarting the app.
     */
    private volatile String lastKeywordPhrasesParsed;

    private volatile ByteTrieSearch bufferSearch;

    /**
     * Change first letter of the first word to use title case.
     */
    private static String titleCaseFirstWordOnly(String sentence) {
        if (sentence.isEmpty()) {
            return sentence;
        }
        final int firstCodePoint = sentence.codePointAt(0);
        // In some non English languages title case is different than uppercase.
        return new StringBuilder()
                .appendCodePoint(Character.toTitleCase(firstCodePoint))
                .append(sentence, Character.charCount(firstCodePoint), sentence.length())
                .toString();
    }

    /**
     * Uppercase the first letter of each word.
     */
    private static String capitalizeAllFirstLetters(String sentence) {
        if (sentence.isEmpty()) {
            return sentence;
        }

        final int delimiter = ' ';
        // Use code points and not characters to handle unicode surrogates.
        int[] codePoints = sentence.codePoints().toArray();
        boolean capitalizeNext = true;
        for (int i = 0, length = codePoints.length; i < length; i++) {
            final int codePoint = codePoints[i];
            if (codePoint == delimiter) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                codePoints[i] = Character.toUpperCase(codePoint);
                capitalizeNext = false;
            }
        }
        return new String(codePoints, 0, codePoints.length);
    }

    /**
     * @return If the phrase will will hide all videos. Not an exhaustive check.
     */
    private static boolean phrasesWillHideAllVideos(@NonNull String[] phrases) {
        for (String commonString : STRINGS_IN_EVERY_BUFFER) {
            if (Utils.containsAny(commonString, phrases)) {
                return true;
            }
        }
        return false;
    }

    private synchronized void parseKeywords() { // Must be synchronized since Litho is multi-threaded.
        String rawKeywords = Settings.HIDE_KEYWORD_CONTENT_PHRASES.get();
        //noinspection StringEquality
        if (rawKeywords == lastKeywordPhrasesParsed) {
            Logger.printDebug(() -> "Using previously initialized search");
            return; // Another thread won the race, and search is already initialized.
        }

        ByteTrieSearch search = new ByteTrieSearch();
        String[] split = rawKeywords.split("\n");
        if (split.length != 0) {
            // Linked Set so log statement are more organized and easier to read.
            Set<String> keywords = new LinkedHashSet<>(10 * split.length);

            for (String phrase : split) {
                // Remove any trailing white space the user may have accidentally included.
                phrase = phrase.stripTrailing();
                if (phrase.isBlank()) continue;

                if (phrase.length() < MINIMUM_KEYWORD_LENGTH) {
                    // Do not reset the setting. Keep the invalid keywords so the user can fix the mistake.
                    Utils.showToastLong(str("revanced_hide_keyword_toast_invalid_length", phrase, MINIMUM_KEYWORD_LENGTH));
                    continue;
                }

                // Add common casing that might appear.
                //
                // This could be simplified by adding case insensitive search to the prefix search,
                // which is very simple to add to StringTreSearch for Unicode and ByteTrieSearch for ASCII.
                //
                // But to support Unicode with ByteTrieSearch would require major changes because
                // UTF-8 characters can be different byte lengths, which does
                // not allow comparing two different byte arrays using simple plain array indexes.
                //
                // Instead add all common case variations of the words.
                String[] phraseVariations = {
                        phrase,
                        phrase.toLowerCase(),
                        titleCaseFirstWordOnly(phrase),
                        capitalizeAllFirstLetters(phrase),
                        phrase.toUpperCase()
                };
                if (phrasesWillHideAllVideos(phraseVariations)) {
                    Utils.showToastLong(str("revanced_hide_keyword_toast_invalid_common", phrase));
                    continue;
                }

                keywords.addAll(Arrays.asList(phraseVariations));
            }

            for (String keyword : keywords) {
                // Use a callback to get the keyword that matched.
                // TrieSearch could have this built in, but that's slightly more complicated since
                // the strings are stored as a byte array and embedded in the search tree.
                TrieSearch.TriePatternMatchedCallback<byte[]> callback =
                        (textSearched, matchedStartIndex, matchedLength, callbackParameter) -> {
                            // noinspection unchecked
                            ((MutableReference<String>) callbackParameter).value = keyword;
                            return true;
                        };
                byte[] stringBytes = keyword.getBytes(StandardCharsets.UTF_8);
                search.addPattern(stringBytes, callback);
            }

            Logger.printDebug(() -> "Search using: (" + search.getEstimatedMemorySize() + " KB) keywords: " + keywords);
        }

        bufferSearch = search;
        timeToResumeFiltering = 0;
        filteredVideosPercentage = 0;
        lastKeywordPhrasesParsed = rawKeywords; // Must set last.
    }

    public KeywordContentFilter() {
        // Keywords are parsed on first call to isFiltered()
        addPathCallbacks(startsWithFilter, containsFilter);
    }

    private boolean hideKeywordSettingIsActive() {
        if (timeToResumeFiltering != 0) {
            if (System.currentTimeMillis() < timeToResumeFiltering) {
                return false;
            }

            timeToResumeFiltering = 0;
            filteredVideosPercentage = 0;
            Logger.printDebug(() -> "Resuming keyword filtering");
        }

        // Must check player type first, as search bar can be active behind the player.
        if (PlayerType.getCurrent().isMaximizedOrFullscreen()) {
            // For now, consider the under video results the same as the home feed.
            return Settings.HIDE_KEYWORD_CONTENT_HOME.get();
        }

        // Must check second, as search can be from any tab.
        if (NavigationBar.isSearchBarActive()) {
            return Settings.HIDE_KEYWORD_CONTENT_SEARCH.get();
        }

        // Avoid checking navigation button status if all other settings are off.
        final boolean hideHome = Settings.HIDE_KEYWORD_CONTENT_HOME.get();
        final boolean hideSubscriptions = Settings.HIDE_KEYWORD_CONTENT_SUBSCRIPTIONS.get();
        if (!hideHome && !hideSubscriptions) {
            return false;
        }

        NavigationButton selectedNavButton = NavigationButton.getSelectedNavigationButton();
        if (selectedNavButton == null) {
            return hideHome; // Unknown tab, treat the same as home.
        }
        if (selectedNavButton == NavigationButton.HOME) {
            return hideHome;
        }
        if (selectedNavButton == NavigationButton.SUBSCRIPTIONS) {
            return hideSubscriptions;
        }
        // User is in the Library or Notifications tab.
        return false;
    }

    private void updateStats(boolean videoWasHidden, @Nullable String keyword) {
        float updatedAverage = filteredVideosPercentage
                * ((ALL_VIDEOS_FILTERED_SAMPLE_SIZE - 1) / ALL_VIDEOS_FILTERED_SAMPLE_SIZE);
        if (videoWasHidden) {
            updatedAverage += 1 / ALL_VIDEOS_FILTERED_SAMPLE_SIZE;
        }

        if (updatedAverage <= ALL_VIDEOS_FILTERED_THRESHOLD) {
            filteredVideosPercentage = updatedAverage;
            return;
        }

        // A keyword is hiding everything.
        // Inform the user, and temporarily turn off filtering.
        timeToResumeFiltering = System.currentTimeMillis() + ALL_VIDEOS_FILTERED_BACKOFF_MILLISECONDS;

        Logger.printDebug(() -> "Temporarily turning off filtering due to excessively broad filter: " + keyword);
        Utils.showToastLong(str("revanced_hide_keyword_toast_invalid_broad", keyword));
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentIndex != 0 && matchedGroup == startsWithFilter) {
            return false;
        }

        // Field is intentionally compared using reference equality.
        //noinspection StringEquality
        if (Settings.HIDE_KEYWORD_CONTENT_PHRASES.get() != lastKeywordPhrasesParsed) {
            // User changed the keywords.
            parseKeywords();
        }

        if (!hideKeywordSettingIsActive()) return false;

        if (exceptions.matches(path)) {
            return false; // Do not update statistics.
        }

        MutableReference<String> matchRef = new MutableReference<>();
        if (bufferSearch.matches(protobufBufferArray, matchRef)) {
            updateStats(true, matchRef.value);
            return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
        }

        updateStats(false, null);
        return false;
    }
}

/**
 * Simple non-atomic wrapper since {@link AtomicReference#setPlain(Object)} is not available with Android 8.0.
 */
final class MutableReference<T> {
    T value;
}