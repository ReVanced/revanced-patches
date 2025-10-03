package app.revanced.extension.youtube.patches.components;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;
import static java.lang.Character.UnicodeBlock.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ByteTrieSearch;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.TrieSearch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.PlayerType;

/**
 * <pre>
 * Allows hiding home feed and search results based on video title keywords and/or channel names.
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
 * - When using whole word syntax, some keywords may need additional pluralized variations.
 */
@SuppressWarnings("unused")
final class KeywordContentFilter extends Filter {

    /**
     * Strings found in the buffer for every videos.  Full strings should be specified.
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
            "expandable_metadata.e",
            "thumbnail.e",
            "avatar.e",
            "overflow_button.e",
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
            "home_video_with_context.e",
            "search_video_with_context.e",
            "video_with_context.e", // Subscription tab videos.
            "related_video_with_context.e",
            // A/B test for subscribed video, and sometimes when tablet layout is enabled.
            "video_lockup_with_attachment.e",
            "compact_video.e",
            "inline_shorts",
            "shorts_video_cell",
            "shorts_pivot_item.e"
    );

    /**
     * Substrings that are never at the start of the path.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final StringFilterGroup containsFilter = new StringFilterGroup(
            null,
            "modern_type_shelf_header_content.e",
            "shorts_lockup_cell.e", // Part of 'shorts_shelf_carousel.e'
            "video_card.e" // Shorts that appear in a horizontal shelf.
    );

    /**
     * Path components to not filter.  Cannot filter the buffer when these are present,
     * otherwise text in UI controls can be filtered as a keyword (such as using "Playlist" as a keyword).
     *
     * This is also a small performance improvement since
     * the buffer of the parent component was already searched and passed.
     */
    private final StringTrieSearch exceptions = new StringTrieSearch(
            "metadata.e",
            "thumbnail.e",
            "avatar.e",
            "overflow_button.e"
    );

    /**
     * Minimum keyword/phrase length to prevent excessively broad content filtering.
     * Only applies when not using whole word syntax.
     */
    private static final int MINIMUM_KEYWORD_LENGTH = 3;

    /**
     * Threshold for {@link #filteredVideosPercentage}
     * that indicates all or nearly all videos have been filtered.
     * This should be close to 100% to reduce false positives.
     */
    private static final float ALL_VIDEOS_FILTERED_THRESHOLD = 0.95f;

    private static final float ALL_VIDEOS_FILTERED_SAMPLE_SIZE = 50;

    private static final long ALL_VIDEOS_FILTERED_BACKOFF_MILLISECONDS = 60 * 1000; // 60 seconds

    private static final int UTF8_MAX_BYTE_COUNT = 4;

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
     * @return If the string contains any characters from languages that do not use spaces between words.
     */
    private static boolean isLanguageWithNoSpaces(String text) {
        for (int i = 0, length = text.length(); i < length;) {
            final int codePoint = text.codePointAt(i);

            Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
            if (block == CJK_UNIFIED_IDEOGRAPHS // Chinese and Kanji
                    || block == HIRAGANA // Japanese Hiragana
                    || block == KATAKANA // Japanese Katakana
                    || block == THAI
                    || block == LAO
                    || block == MYANMAR
                    || block == KHMER
                    || block == TIBETAN) {
                return true;
            }

            i += Character.charCount(codePoint);
        }

        return false;
    }

    /**
     * @return If the phrase will hide all videos. Not an exhaustive check.
     */
    private static boolean phrasesWillHideAllVideos(@NonNull String[] phrases, boolean matchWholeWords) {
        for (String phrase : phrases) {
            for (String commonString : STRINGS_IN_EVERY_BUFFER) {
                if (matchWholeWords) {
                    byte[] commonStringBytes = commonString.getBytes(StandardCharsets.UTF_8);
                    int matchIndex = 0;
                    while (true) {
                        matchIndex = commonString.indexOf(phrase, matchIndex);
                        if (matchIndex < 0) break;

                        if (keywordMatchIsWholeWord(commonStringBytes, matchIndex, phrase.length())) {
                            return true;
                        }

                        matchIndex++;
                    }
                } else if (Utils.containsAny(commonString, phrases)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return If the start and end indexes are not surrounded by other letters.
     *         If the indexes are surrounded by numbers/symbols/punctuation it is considered a whole word.
     */
    private static boolean keywordMatchIsWholeWord(byte[] text, int keywordStartIndex, int keywordLength) {
        final Integer codePointBefore = getUtf8CodePointBefore(text, keywordStartIndex);
        if (codePointBefore != null && Character.isLetter(codePointBefore)) {
            return false;
        }

        final Integer codePointAfter = getUtf8CodePointAt(text, keywordStartIndex + keywordLength);
        //noinspection RedundantIfStatement
        if (codePointAfter != null && Character.isLetter(codePointAfter)) {
            return false;
        }

        return true;
    }

    /**
     * @return The UTF8 character point immediately before the index,
     *         or null if the bytes before the index is not a valid UTF8 character.
     */
    @Nullable
    private static Integer getUtf8CodePointBefore(byte[] data, int index) {
        int characterByteCount = 0;
        while (--index >= 0 && ++characterByteCount <= UTF8_MAX_BYTE_COUNT) {
            if (isValidUtf8(data, index, characterByteCount)) {
                return decodeUtf8ToCodePoint(data, index, characterByteCount);
            }
        }

        return null;
    }

    /**
     * @return The UTF8 character point at the index,
     *         or null if the index holds no valid UTF8 character.
     */
    @Nullable
    private static Integer getUtf8CodePointAt(byte[] data, int index) {
        int characterByteCount = 0;
        final int dataLength = data.length;
        while (index + characterByteCount < dataLength && ++characterByteCount <= UTF8_MAX_BYTE_COUNT) {
            if (isValidUtf8(data, index, characterByteCount)) {
                return decodeUtf8ToCodePoint(data, index, characterByteCount);
            }
        }

        return null;
    }

    public static boolean isValidUtf8(byte[] data, int startIndex, int numberOfBytes) {
        switch (numberOfBytes) {
            case 1: // 0xxxxxxx (ASCII)
                return (data[startIndex] & 0x80) == 0;
            case 2: // 110xxxxx, 10xxxxxx
                return (data[startIndex] & 0xE0) == 0xC0
                        && (data[startIndex + 1] & 0xC0) == 0x80;
            case 3: // 1110xxxx, 10xxxxxx, 10xxxxxx
                return (data[startIndex] & 0xF0) == 0xE0
                        && (data[startIndex + 1] & 0xC0) == 0x80
                        && (data[startIndex + 2] & 0xC0) == 0x80;
            case 4: // 11110xxx, 10xxxxxx, 10xxxxxx, 10xxxxxx
                return (data[startIndex] & 0xF8) == 0xF0
                        && (data[startIndex + 1] & 0xC0) == 0x80
                        && (data[startIndex + 2] & 0xC0) == 0x80
                        && (data[startIndex + 3] & 0xC0) == 0x80;
        }

        throw new IllegalArgumentException("numberOfBytes: " + numberOfBytes);
    }

    public static int decodeUtf8ToCodePoint(byte[] data, int startIndex, int numberOfBytes) {
        switch (numberOfBytes) {
            case 1:
                return data[startIndex];
            case 2:
                return ((data[startIndex] & 0x1F) << 6) |
                        (data[startIndex + 1] & 0x3F);
            case 3:
                return ((data[startIndex] & 0x0F) << 12) |
                        ((data[startIndex + 1] & 0x3F) << 6) |
                        (data[startIndex + 2] & 0x3F);
            case 4:
                return ((data[startIndex] & 0x07) << 18) |
                        ((data[startIndex + 1] & 0x3F) << 12) |
                        ((data[startIndex + 2] & 0x3F) << 6) |
                        (data[startIndex + 3] & 0x3F);
        }
        throw new IllegalArgumentException("numberOfBytes: " + numberOfBytes);
    }

    private static boolean phraseUsesWholeWordSyntax(String phrase) {
        return phrase.startsWith("\"") && phrase.endsWith("\"");
    }

    private static String stripWholeWordSyntax(String phrase) {
        return phrase.substring(1, phrase.length() - 1);
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
            // Map is: Phrase -> isWholeWord
            Map<String, Boolean> keywords = new LinkedHashMap<>(10 * split.length);

            for (String phrase : split) {
                // Remove any trailing spaces the user may have accidentally included.
                phrase = phrase.stripTrailing();
                if (phrase.isBlank()) continue;

                final boolean wholeWordMatching;
                if (phraseUsesWholeWordSyntax(phrase)) {
                    if (phrase.length() == 2) {
                        continue; // Empty "" phrase
                    }
                    phrase = stripWholeWordSyntax(phrase);
                    wholeWordMatching = true;
                } else if (phrase.length() < MINIMUM_KEYWORD_LENGTH && !isLanguageWithNoSpaces(phrase)) {
                    // Allow phrases of 1 and 2 characters if using a
                    // language that does not use spaces between words.

                    // Do not reset the setting. Keep the invalid keywords so the user can fix the mistake.
                    Utils.showToastLong(str("revanced_hide_keyword_toast_invalid_length", phrase, MINIMUM_KEYWORD_LENGTH));
                    continue;
                } else {
                    wholeWordMatching = false;
                }

                // Common casing that might appear.
                //
                // This could be simplified by adding case insensitive search to the prefix search,
                // which is very simple to add to StringTreSearch for Unicode and ByteTrieSearch for ASCII.
                //
                // But to support Unicode with ByteTrieSearch would require major changes because
                // UTF-8 characters can be different byte lengths, which does
                // not allow comparing two different byte arrays using simple plain array indexes.
                //
                // Instead use all common case variations of the words.
                String[] phraseVariations = {
                        phrase,
                        phrase.toLowerCase(),
                        titleCaseFirstWordOnly(phrase),
                        capitalizeAllFirstLetters(phrase),
                        phrase.toUpperCase()
                };

                if (phrasesWillHideAllVideos(phraseVariations, wholeWordMatching)) {
                    String toastMessage;
                    // If whole word matching is off, but would pass with on, then show a different toast.
                    if (!wholeWordMatching && !phrasesWillHideAllVideos(phraseVariations, true)) {
                        toastMessage = "revanced_hide_keyword_toast_invalid_common_whole_word_required";
                    } else {
                        toastMessage = "revanced_hide_keyword_toast_invalid_common";
                    }

                    Utils.showToastLong(str(toastMessage, phrase));
                    continue;
                }

                for (String variation : phraseVariations) {
                    // Check if the same phrase is declared both with and without quotes.
                    Boolean existing = keywords.get(variation);
                    if (existing == null) {
                        keywords.put(variation, wholeWordMatching);
                    } else if (existing != wholeWordMatching) {
                        Utils.showToastLong(str("revanced_hide_keyword_toast_invalid_conflicting", phrase));
                        break;
                    }
                }
            }

            for (Map.Entry<String, Boolean> entry : keywords.entrySet()) {
                String keyword = entry.getKey();
                //noinspection ExtractMethodRecommender
                final boolean isWholeWord = entry.getValue();

                TrieSearch.TriePatternMatchedCallback<byte[]> callback =
                        (textSearched, startIndex, matchLength, callbackParameter) -> {
                            if (isWholeWord && !keywordMatchIsWholeWord(textSearched, startIndex, matchLength)) {
                                return false;
                            }

                            Logger.printDebug(() -> (isWholeWord ? "Matched whole keyword: '"
                                    : "Matched keyword: '") + keyword + "'");
                            // noinspection unchecked
                            ((MutableReference<String>) callbackParameter).value = keyword;
                            return true;
                        };
                byte[] stringBytes = keyword.getBytes(StandardCharsets.UTF_8);
                search.addPattern(stringBytes, callback);
            }

            Logger.printDebug(() -> "Search using: (" + search.getEstimatedMemorySize() + " KB) keywords: " + keywords.keySet());
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

        return switch (selectedNavButton) {
            case HOME, EXPLORE -> hideHome;
            case SUBSCRIPTIONS -> hideSubscriptions;
            // User is in the Library or notifications.
            default -> false;
        };
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
    boolean isFiltered(String identifier, String path, byte[] buffer,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentIndex != 0 && matchedGroup == startsWithFilter) {
            return false;
        }

        // Field is intentionally compared using reference equality.
        //noinspection StringEquality
        if (Settings.HIDE_KEYWORD_CONTENT_PHRASES.get() != lastKeywordPhrasesParsed) {
            // User changed the keywords or whole word setting.
            parseKeywords();
        }

        if (!hideKeywordSettingIsActive()) return false;

        if (exceptions.matches(path)) {
            return false; // Do not update statistics.
        }

        MutableReference<String> matchRef = new MutableReference<>();
        if (bufferSearch.matches(buffer, matchRef)) {
            updateStats(true, matchRef.value);
            return true;
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