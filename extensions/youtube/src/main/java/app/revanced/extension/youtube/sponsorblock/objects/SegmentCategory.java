package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.youtube.settings.Settings.*;
import static app.revanced.extension.shared.StringRef.sf;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

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
            FILLER,
            MUSIC_OFFTOPIC,
    };
    private static final Map<String, SegmentCategory> mValuesMap = new HashMap<>(2 * categoriesWithoutUnsubmitted.length);

    /**
     * Categories currently enabled, formatted for an API call
     */
    public static String sponsorBlockAPIFetchCategories = "[]";

    static {
        for (SegmentCategory value : categoriesWithoutUnsubmitted)
            mValuesMap.put(value.keyValue, value);
    }

    @NonNull
    public static SegmentCategory[] categoriesWithoutUnsubmitted() {
        return categoriesWithoutUnsubmitted;
    }

    @NonNull
    public static SegmentCategory[] categoriesWithoutHighlights() {
        return categoriesWithoutHighlights;
    }

    @Nullable
    public static SegmentCategory byCategoryKey(@NonNull String key) {
        return mValuesMap.get(key);
    }

    /**
     * Must be called if behavior of any category is changed
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

    public static void loadAllCategoriesFromSettings() {
        for (SegmentCategory category : values()) {
            category.loadFromSettings();
        }
        updateEnabledCategories();
    }

    @NonNull
    public final String keyValue;
    @NonNull
    public final StringSetting behaviorSetting; // TODO: Replace with EnumSetting.
    @NonNull
    private final StringSetting colorSetting;

    @NonNull
    public final StringRef title;
    @NonNull
    public final StringRef description;

    /**
     * Skip button text, if the skip occurs in the first quarter of the video
     */
    @NonNull
    public final StringRef skipButtonTextBeginning;
    /**
     * Skip button text, if the skip occurs in the middle half of the video
     */
    @NonNull
    public final StringRef skipButtonTextMiddle;
    /**
     * Skip button text, if the skip occurs in the last quarter of the video
     */
    @NonNull
    public final StringRef skipButtonTextEnd;
    /**
     * Skipped segment toast, if the skip occurred in the first quarter of the video
     */
    @NonNull
    public final StringRef skippedToastBeginning;
    /**
     * Skipped segment toast, if the skip occurred in the middle half of the video
     */
    @NonNull
    public final StringRef skippedToastMiddle;
    /**
     * Skipped segment toast, if the skip occurred in the last quarter of the video
     */
    @NonNull
    public final StringRef skippedToastEnd;

    @NonNull
    public final Paint paint;

    /**
     * Value must be changed using {@link #setColor(String)}.
     */
    public int color;

    /**
     * Value must be changed using {@link #setBehaviour(CategoryBehaviour)}.
     * Caller must also {@link #updateEnabledCategories()}.
     */
    @NonNull
    public CategoryBehaviour behaviour = CategoryBehaviour.IGNORE;

    SegmentCategory(String keyValue, StringRef title, StringRef description,
                    StringRef skipButtonText,
                    StringRef skippedToastText,
                    StringSetting behavior, StringSetting color) {
        this(keyValue, title, description,
                skipButtonText, skipButtonText, skipButtonText,
                skippedToastText, skippedToastText, skippedToastText,
                behavior, color);
    }

    SegmentCategory(String keyValue, StringRef title, StringRef description,
                    StringRef skipButtonTextBeginning, StringRef skipButtonTextMiddle, StringRef skipButtonTextEnd,
                    StringRef skippedToastBeginning, StringRef skippedToastMiddle, StringRef skippedToastEnd,
                    StringSetting behavior, StringSetting color) {
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
            setColor(colorString);
        } catch (Exception ex) {
            Logger.printException(() -> "Invalid color: " + colorString, ex);
            colorSetting.resetToDefault();
            loadFromSettings();
        }
    }

    public void setBehaviour(@NonNull CategoryBehaviour behaviour) {
        this.behaviour = Objects.requireNonNull(behaviour);
        this.behaviorSetting.save(behaviour.reVancedKeyValue);
    }

    public void resetColor() {
        setColor(colorSetting.defaultValue);
    }

    /**
     * @return Hex color string of RRGGBB format, or
     *         if alpha channel is not opaque then returns AARRGGBB format.
     */
    @NonNull
    public String colorString() {
        if (Color.alpha(color) == 255) {
            return colorStringWithoutAlpha();
        }

        return String.format(Locale.US, "#%08X", color);
    }

    /**
     * @return RRGGBB hex color string without an alpha channel.
     */
    @NonNull
    public String colorStringWithoutAlpha() {
        return String.format(Locale.US, "#%06X", color & 0x00FFFFFF);
    }

    public void setColor(@NonNull String colorString) throws IllegalArgumentException {
        final int color = Color.parseColor(colorString);
        paint.setColor(color);

        if (colorString.length() <= 7) {
            // 6 digit hex color with opaque alpha channel.
            paint.setAlpha(255);
        } else if (Color.alpha(color) == 255) {
            // User enter #AARRGGBB with fully opaque color (alpha channel is FF).
            // Save without alpha channel to keep text simpler.
            colorString = colorStringWithoutAlpha();
        }

        // Save after parsing.
        this.color = color;
        colorSetting.save(colorString);
    }

    @NonNull
    private static SpannableString getCategoryColorDotSpan(String text, int color) {
        SpannableString dotSpan = new SpannableString('⬤' + text);
        dotSpan.setSpan(new ForegroundColorSpan(color), 0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return dotSpan;
    }

    @NonNull
    public static SpannableString getCategoryColorDot(int color) {
        return getCategoryColorDotSpan("", color);
    }

    @NonNull
    public SpannableString getCategoryColorDot() {
        return getCategoryColorDot(color);
    }

    @NonNull
    public SpannableString getTitleWithColorDot() {
        return getCategoryColorDotSpan(" " + title, color);
    }

    /**
     * @param segmentStartTime video time the segment category started
     * @param videoLength      length of the video
     * @return the skip button text
     */
    @NonNull
    StringRef getSkipButtonText(long segmentStartTime, long videoLength) {
        if (Settings.SB_COMPACT_SKIP_BUTTON.get()) {
            return (this == SegmentCategory.HIGHLIGHT)
                    ? skipSponsorTextCompactHighlight
                    : skipSponsorTextCompact;
        }

        if (videoLength == 0) {
            return skipButtonTextBeginning; // video is still loading.  Assume it's the beginning
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
     * @param segmentStartTime video time the segment category started
     * @param videoLength      length of the video
     * @return 'skipped segment' toast message
     */
    @NonNull
    StringRef getSkippedToastText(long segmentStartTime, long videoLength) {
        if (videoLength == 0) {
            return skippedToastBeginning; // video is still loading.  Assume it's the beginning
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
