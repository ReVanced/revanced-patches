package app.revanced.integrations.youtube.sponsorblock.objects;

import static app.revanced.integrations.youtube.settings.Settings.*;
import static app.revanced.integrations.shared.StringRef.sf;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.StringSetting;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.StringRef;

public enum SegmentCategory {
    SPONSOR("sponsor", sf("sb_segments_sponsor"), sf("sb_segments_sponsor_sum"), sf("sb_skip_button_sponsor"), sf("sb_skipped_sponsor"),
            SB_CATEGORY_SPONSOR, SB_CATEGORY_SPONSOR_COLOR),
    SELF_PROMO("selfpromo", sf("sb_segments_selfpromo"), sf("sb_segments_selfpromo_sum"), sf("sb_skip_button_selfpromo"), sf("sb_skipped_selfpromo"),
            SB_CATEGORY_SELF_PROMO, SB_CATEGORY_SELF_PROMO_COLOR),
    INTERACTION("interaction", sf("sb_segments_interaction"), sf("sb_segments_interaction_sum"), sf("sb_skip_button_interaction"), sf("sb_skipped_interaction"),
            SB_CATEGORY_INTERACTION, SB_CATEGORY_INTERACTION_COLOR),
    /**
     * Unique category that is treated differently than the rest.
     */
    HIGHLIGHT("poi_highlight", sf("sb_segments_highlight"), sf("sb_segments_highlight_sum"), sf("sb_skip_button_highlight"), sf("sb_skipped_highlight"),
            SB_CATEGORY_HIGHLIGHT, SB_CATEGORY_HIGHLIGHT_COLOR),
    INTRO("intro", sf("sb_segments_intro"), sf("sb_segments_intro_sum"),
            sf("sb_skip_button_intro_beginning"), sf("sb_skip_button_intro_middle"), sf("sb_skip_button_intro_end"),
            sf("sb_skipped_intro_beginning"), sf("sb_skipped_intro_middle"), sf("sb_skipped_intro_end"),
            SB_CATEGORY_INTRO, SB_CATEGORY_INTRO_COLOR),
    OUTRO("outro", sf("sb_segments_outro"), sf("sb_segments_outro_sum"), sf("sb_skip_button_outro"), sf("sb_skipped_outro"),
            SB_CATEGORY_OUTRO, SB_CATEGORY_OUTRO_COLOR),
    PREVIEW("preview", sf("sb_segments_preview"), sf("sb_segments_preview_sum"),
            sf("sb_skip_button_preview_beginning"), sf("sb_skip_button_preview_middle"), sf("sb_skip_button_preview_end"),
            sf("sb_skipped_preview_beginning"), sf("sb_skipped_preview_middle"), sf("sb_skipped_preview_end"),
            SB_CATEGORY_PREVIEW, SB_CATEGORY_PREVIEW_COLOR),
    FILLER("filler", sf("sb_segments_filler"), sf("sb_segments_filler_sum"), sf("sb_skip_button_filler"), sf("sb_skipped_filler"),
            SB_CATEGORY_FILLER, SB_CATEGORY_FILLER_COLOR),
    MUSIC_OFFTOPIC("music_offtopic", sf("sb_segments_nomusic"), sf("sb_segments_nomusic_sum"), sf("sb_skip_button_nomusic"), sf("sb_skipped_nomusic"),
            SB_CATEGORY_MUSIC_OFFTOPIC, SB_CATEGORY_MUSIC_OFFTOPIC_COLOR),
    UNSUBMITTED("unsubmitted", StringRef.empty, StringRef.empty, sf("sb_skip_button_unsubmitted"), sf("sb_skipped_unsubmitted"),
            SB_CATEGORY_UNSUBMITTED, SB_CATEGORY_UNSUBMITTED_COLOR),;

    private static final StringRef skipSponsorTextCompact = sf("sb_skip_button_compact");
    private static final StringRef skipSponsorTextCompactHighlight = sf("sb_skip_button_compact_highlight");

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
    private final StringSetting behaviorSetting;
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
    /**
     * @return HTML color format string
     */
    @NonNull
    public String colorString() {
        return String.format("#%06X", color);
    }

    public void setColor(@NonNull String colorString) throws IllegalArgumentException {
        final int color = Color.parseColor(colorString) & 0xFFFFFF;
        this.color = color;
        paint.setColor(color);
        paint.setAlpha(255);
        colorSetting.save(colorString); // Save after parsing.
    }

    public void resetColor() {
        setColor(colorSetting.defaultValue);
    }

    @NonNull
    private static String getCategoryColorDotHTML(int color) {
        color &= 0xFFFFFF;
        return String.format("<font color=\"#%06X\">⬤</font>", color);
    }

    @NonNull
    public static Spanned getCategoryColorDot(int color) {
        return Html.fromHtml(getCategoryColorDotHTML(color));
    }

    @NonNull
    public Spanned getCategoryColorDot() {
        return getCategoryColorDot(color);
    }

    @NonNull
    public Spanned getTitleWithColorDot() {
        return Html.fromHtml(getCategoryColorDotHTML(color) + " " + title);
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
