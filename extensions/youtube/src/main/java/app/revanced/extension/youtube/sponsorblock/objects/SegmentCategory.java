package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.shared.StringRef.sf;
import static app.revanced.extension.youtube.settings.Settings.*;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;

import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.StringRef;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.youtube.settings.Settings;

public enum SegmentCategory {
    SPONSOR("sponsor", sf("revanced_sb_segments_sponsor"), sf("revanced_sb_segments_sponsor_sum"), sf("revanced_sb_skip_button_sponsor"), sf("revanced_sb_skipped_sponsor"),
            SB_CATEGORY_SPONSOR, SB_CATEGORY_SPONSOR_COLOR),
    SELF_PROMO("selfpromo", sf("revanced_sb_segments_selfpromo"), sf("revanced_sb_segments_selfpromo_sum"), sf("revanced_sb_skip_button_selfpromo"), sf("revanced_sb_skipped_selfpromo"),
            SB_CATEGORY_SELF_PROMO, SB_CATEGORY_SELF_PROMO_COLOR),
    INTERACTION("interaction", sf("revanced_sb_segments_interaction"), sf("revanced_sb_segments_interaction_sum"), sf("revanced_sb_skip_button_interaction"), sf("revanced_sb_skipped_interaction"),
            SB_CATEGORY_INTERACTION, SB_CATEGORY_INTERACTION_COLOR),
    /**
     * Unique category that is treated differently than the rest.
     */
    HIGHLIGHT("poi_highlight", sf("revanced_sb_segments_highlight"), sf("revanced_sb_segments_highlight_sum"), sf("revanced_sb_skip_button_highlight"), sf("revanced_sb_skipped_highlight"),
            SB_CATEGORY_HIGHLIGHT, SB_CATEGORY_HIGHLIGHT_COLOR),
    INTRO("intro", sf("revanced_sb_segments_intro"), sf("revanced_sb_segments_intro_sum"),
            sf("revanced_sb_skip_button_intro_beginning"), sf("revanced_sb_skip_button_intro_middle"), sf("revanced_sb_skip_button_intro_end"),
            sf("revanced_sb_skipped_intro_beginning"), sf("revanced_sb_skipped_intro_middle"), sf("revanced_sb_skipped_intro_end"),
            SB_CATEGORY_INTRO, SB_CATEGORY_INTRO_COLOR),
    OUTRO("outro", sf("revanced_sb_segments_outro"), sf("revanced_sb_segments_outro_sum"), sf("revanced_sb_skip_button_outro"), sf("revanced_sb_skipped_outro"),
            SB_CATEGORY_OUTRO, SB_CATEGORY_OUTRO_COLOR),
    PREVIEW("preview", sf("revanced_sb_segments_preview"), sf("revanced_sb_segments_preview_sum"),
            sf("revanced_sb_skip_button_preview_beginning"), sf("revanced_sb_skip_button_preview_middle"), sf("revanced_sb_skip_button_preview_end"),
            sf("revanced_sb_skipped_preview_beginning"), sf("revanced_sb_skipped_preview_middle"), sf("revanced_sb_skipped_preview_end"),
            SB_CATEGORY_PREVIEW, SB_CATEGORY_PREVIEW_COLOR),
    HOOK("hook", sf("revanced_sb_segments_hook"), sf("revanced_sb_segments_hook_sum"), sf("revanced_sb_skip_button_hook"), sf("revanced_sb_skipped_hook"),
            SB_CATEGORY_HOOK, SB_CATEGORY_HOOK_COLOR),
    FILLER("filler", sf("revanced_sb_segments_filler"), sf("revanced_sb_segments_filler_sum"), sf("revanced_sb_skip_button_filler"), sf("revanced_sb_skipped_filler"),
            SB_CATEGORY_FILLER, SB_CATEGORY_FILLER_COLOR),
    MUSIC_OFFTOPIC("music_offtopic", sf("revanced_sb_segments_nomusic"), sf("revanced_sb_segments_nomusic_sum"), sf("revanced_sb_skip_button_nomusic"), sf("revanced_sb_skipped_nomusic"),
            SB_CATEGORY_MUSIC_OFFTOPIC, SB_CATEGORY_MUSIC_OFFTOPIC_COLOR),
    UNSUBMITTED("unsubmitted", StringRef.empty, StringRef.empty, sf("revanced_sb_skip_button_unsubmitted"), sf("revanced_sb_skipped_unsubmitted"),
            SB_CATEGORY_UNSUBMITTED, SB_CATEGORY_UNSUBMITTED_COLOR);

    private static final StringRef skipSponsorTextCompact = sf("revanced_sb_skip_button_compact");
    private static final StringRef skipSponsorTextCompactHighlight = sf("revanced_sb_skip_button_compact_highlight");

    private static final SegmentCategory[] categoriesWithoutHighlights = new SegmentCategory[]{
            SPONSOR,
            SELF_PROMO,
            INTERACTION,
            INTRO,
            OUTRO,
            PREVIEW,
            HOOK,
            FILLER,
            MUSIC_OFFTOPIC,
    };

    private static final SegmentCategory[] categoriesWithoutUnsubmitted = new SegmentCategory[]{
            SPONSOR,
            SELF_PROMO,
            INTERACTION,
            HIGHLIGHT,
            INTRO,
            OUTRO,
            PREVIEW,
            HOOK,
            FILLER,
            MUSIC_OFFTOPIC,
    };

    public static final String COLOR_DOT_STRING = "⬤";

    public static final float CATEGORY_DEFAULT_OPACITY = 0.7f;

    private static final Map<String, SegmentCategory> mValuesMap = new HashMap<>(2 * categoriesWithoutUnsubmitted.length);

    /**
     * Categories currently enabled, formatted for an API call.
     */
    public static String sponsorBlockAPIFetchCategories = "[]";

    static {
        for (SegmentCategory value : categoriesWithoutUnsubmitted)
            mValuesMap.put(value.keyValue, value);
    }

    /**
     * Returns an array of categories excluding the unsubmitted category.
     */
    public static SegmentCategory[] categoriesWithoutUnsubmitted() {
        return categoriesWithoutUnsubmitted;
    }

    /**
     * Returns an array of categories excluding the highlight category.
     */
    public static SegmentCategory[] categoriesWithoutHighlights() {
        return categoriesWithoutHighlights;
    }

    /**
     * Retrieves a category by its key.
     */
    @Nullable
    public static SegmentCategory byCategoryKey(@NonNull String key) {
        return mValuesMap.get(key);
    }

    /**
     * Updates the list of enabled categories for API calls. Must be called when any category's behavior changes.
     */
    public static void updateEnabledCategories() {
        Utils.verifyOnMainThread();
        Logger.printDebug(() -> "updateEnabledCategories");
        SegmentCategory[] categories = categoriesWithoutUnsubmitted();
        List<String> enabledCategories = new ArrayList<>(categories.length);
        for (SegmentCategory category : categories) {
            if (category.behaviour != CategoryBehaviour.IGNORE) {
                enabledCategories.add(category.keyValue);
            }
        }

        //"[%22sponsor%22,%22outro%22,%22music_offtopic%22,%22intro%22,%22selfpromo%22,%22interaction%22,%22preview%22]";
        if (enabledCategories.isEmpty())
            sponsorBlockAPIFetchCategories = "[]";
        else
            sponsorBlockAPIFetchCategories = "[%22" + TextUtils.join("%22,%22", enabledCategories) + "%22]";
    }

    /**
     * Loads all category settings from persistent storage.
     */
    public static void loadAllCategoriesFromSettings() {
        for (SegmentCategory category : values()) {
            category.loadFromSettings();
        }
        updateEnabledCategories();
    }

    public final String keyValue;
    public final StringSetting behaviorSetting;
    public final StringSetting colorSetting;

    public final StringRef title;
    public final StringRef description;

    /**
     * Skip button text, if the skip occurs in the first quarter of the video.
     */
    public final StringRef skipButtonTextBeginning;
    /**
     * Skip button text, if the skip occurs in the middle half of the video.
     */
    public final StringRef skipButtonTextMiddle;
    /**
     * Skip button text, if the skip occurs in the last quarter of the video.
     */
    public final StringRef skipButtonTextEnd;
    /**
     * Skipped segment toast, if the skip occurred in the first quarter of the video.
     */
    public final StringRef skippedToastBeginning;
    /**
     * Skipped segment toast, if the skip occurred in the middle half of the video.
     */
    public final StringRef skippedToastMiddle;
    /**
     * Skipped segment toast, if the skip occurred in the last quarter of the video.
     */
    public final StringRef skippedToastEnd;

    public final Paint paint;

    /**
     * Category color with opacity applied.
     */
    @ColorInt
    private int color;

    /**
     * Value must be changed using {@link #setBehaviour(CategoryBehaviour)}.
     * Caller must also call {@link #updateEnabledCategories()}.
     */
    public CategoryBehaviour behaviour = CategoryBehaviour.IGNORE;

    SegmentCategory(String keyValue, StringRef title, StringRef description,
                    StringRef skipButtonText,
                    StringRef skippedToastText,
                    StringSetting behavior,
                    StringSetting color) {
        this(keyValue, title, description,
                skipButtonText, skipButtonText, skipButtonText,
                skippedToastText, skippedToastText, skippedToastText,
                behavior,
                color);
    }

    SegmentCategory(String keyValue, StringRef title, StringRef description,
                    StringRef skipButtonTextBeginning, StringRef skipButtonTextMiddle, StringRef skipButtonTextEnd,
                    StringRef skippedToastBeginning, StringRef skippedToastMiddle, StringRef skippedToastEnd,
                    StringSetting behavior,
                    StringSetting color) {
        this.keyValue = Objects.requireNonNull(keyValue);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.skipButtonTextBeginning = Objects.requireNonNull(skipButtonTextBeginning);
        this.skipButtonTextMiddle = Objects.requireNonNull(skipButtonTextMiddle);
        this.skipButtonTextEnd = Objects.requireNonNull(skipButtonTextEnd);
        this.skippedToastBeginning = Objects.requireNonNull(skippedToastBeginning);
        this.skippedToastMiddle = Objects.requireNonNull(skippedToastMiddle);
        this.skippedToastEnd = Objects.requireNonNull(skippedToastEnd);
        this.behaviorSetting = Objects.requireNonNull(behavior);
        this.colorSetting = Objects.requireNonNull(color);
        this.paint = new Paint();
        loadFromSettings();
    }

    /**
     * Loads the category's behavior and color from settings.
     */
    private void loadFromSettings() {
        String behaviorString = behaviorSetting.get();
        CategoryBehaviour savedBehavior = CategoryBehaviour.byReVancedKeyValue(behaviorString);
        if (savedBehavior == null) {
            Logger.printException(() -> "Invalid behavior: " + behaviorString);
            behaviorSetting.resetToDefault();
            loadFromSettings();
            return;
        }
        this.behaviour = savedBehavior;

        String colorString = colorSetting.get();
        try {
            setColorWithOpacity(colorString);
        } catch (Exception ex) {
            Logger.printException(() -> "Invalid color: " + colorString, ex);
            colorSetting.resetToDefault();
            loadFromSettings();
        }
    }

    /**
     * Sets the behavior of the category and saves it to settings.
     */
    public void setBehaviour(CategoryBehaviour behaviour) {
        this.behaviour = Objects.requireNonNull(behaviour);
        this.behaviorSetting.save(behaviour.reVancedKeyValue);
    }

    /**
     * Sets the segment color with opacity from a color string in #AARRGGBB format.
     */
    public void setColorWithOpacity(String colorString) throws IllegalArgumentException {
        int colorWithOpacity = Color.parseColor(colorString);
        colorSetting.save(String.format(Locale.US, "#%08X", colorWithOpacity));
        color = colorWithOpacity;
        paint.setColor(color);
    }

    /**
     * @param opacity [0, 1] opacity value.
     */
    public void setOpacity(double opacity) {
        color = Color.argb((int) (opacity * 255), Color.red(color), Color.green(color), Color.blue(color));
        paint.setColor(color);
    }

    /**
     * Gets the color with opacity applied (ARGB).
     */
    @ColorInt
    public int getColorWithOpacity() {
        return color;
    }

    /**
     * @return The default color with opacity applied.
     */
    @ColorInt
    public int getDefaultColorWithOpacity() {
        return Color.parseColor(colorSetting.defaultValue);
    }

    /**
     * Gets the color as a hex string with opacity (#AARRGGBB).
     */
    public String getColorStringWithOpacity() {
        return String.format(Locale.US, "#%08X", getColorWithOpacity());
    }

    /**
     * @return The color as a hex string without opacity (#RRGGBB).
     */
    public String getColorStringWithoutOpacity() {
        final int colorNoOpacity = getColorWithOpacity() & 0x00FFFFFF;
        return String.format(Locale.US, "#%06X", colorNoOpacity);
    }

    /**
     * @return [0, 1] opacity value.
     */
    public double getOpacity() {
        double opacity = Color.alpha(color) / 255.0;
        return Math.round(opacity * 100.0) / 100.0; // Round to 2 decimal digits.
    }

    /**
     * Gets the title of the category.
     */
    public StringRef getTitle() {
        return title;
    }

    /**
     * Creates a {@link SpannableString} that starts with a colored dot followed by the provided text.
     */
    private static SpannableString getCategoryColorDotSpan(String text, @ColorInt int color) {
        SpannableString dotSpan = new SpannableString(COLOR_DOT_STRING + text);
        dotSpan.setSpan(new ForegroundColorSpan(color), 0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        dotSpan.setSpan(new RelativeSizeSpan(1.5f), 0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return dotSpan;
    }

    /**
     * Returns the category title with a colored dot.
     */
    public SpannableString getTitleWithColorDot(@ColorInt int categoryColor) {
        return getCategoryColorDotSpan(" " + title, categoryColor);
    }

    /**
     * Returns the category title with a colored dot.
     */
    public SpannableString getTitleWithColorDot() {
        return getTitleWithColorDot(color);
    }

    /**
     * Gets the skip button text based on segment position.
     *
     * @param segmentStartTime Video time the segment category started.
     * @param videoLength      Length of the video.
     * @return The skip button text.
     */
    StringRef getSkipButtonText(long segmentStartTime, long videoLength) {
        if (Settings.SB_COMPACT_SKIP_BUTTON.get()) {
            return (this == SegmentCategory.HIGHLIGHT)
                    ? skipSponsorTextCompactHighlight
                    : skipSponsorTextCompact;
        }

        if (videoLength == 0) {
            return skipButtonTextBeginning; // Video is still loading. Assume it's the beginning.
        }
        final float position = segmentStartTime / (float) videoLength;
        if (position < 0.25f) {
            return skipButtonTextBeginning;
        } else if (position < 0.75f) {
            return skipButtonTextMiddle;
        }
        return skipButtonTextEnd;
    }

    /**
     * Gets the skipped segment toast message based on segment position.
     *
     * @param segmentStartTime Video time the segment category started.
     * @param videoLength      Length of the video.
     * @return The skipped segment toast message.
     */
    StringRef getSkippedToastText(long segmentStartTime, long videoLength) {
        if (videoLength == 0) {
            return skippedToastBeginning; // Video is still loading. Assume it's the beginning.
        }
        final float position = segmentStartTime / (float) videoLength;
        if (position < 0.25f) {
            return skippedToastBeginning;
        } else if (position < 0.75f) {
            return skippedToastMiddle;
        }
        return skippedToastEnd;
    }
}
