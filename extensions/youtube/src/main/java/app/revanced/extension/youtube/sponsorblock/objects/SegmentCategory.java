package app.revanced.extension.youtube.sponsorblock.objects;

import static app.revanced.extension.shared.StringRef.sf;
import static app.revanced.extension.youtube.settings.Settings.*;

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
import app.revanced.extension.shared.settings.FloatSetting;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.youtube.settings.Settings;

public enum SegmentCategory {
    SPONSOR("sponsor", sf("revanced_sb_segments_sponsor"), sf("revanced_sb_segments_sponsor_sum"), sf("revanced_sb_skip_button_sponsor"), sf("revanced_sb_skipped_sponsor"),
            SB_CATEGORY_SPONSOR, SB_CATEGORY_SPONSOR_COLOR, SB_CATEGORY_SPONSOR_OPACITY),
    SELF_PROMO("selfpromo", sf("revanced_sb_segments_selfpromo"), sf("revanced_sb_segments_selfpromo_sum"), sf("revanced_sb_skip_button_selfpromo"), sf("revanced_sb_skipped_selfpromo"),
            SB_CATEGORY_SELF_PROMO, SB_CATEGORY_SELF_PROMO_COLOR, SB_CATEGORY_SELF_PROMO_OPACITY),
    INTERACTION("interaction", sf("revanced_sb_segments_interaction"), sf("revanced_sb_segments_interaction_sum"), sf("revanced_sb_skip_button_interaction"), sf("revanced_sb_skipped_interaction"),
            SB_CATEGORY_INTERACTION, SB_CATEGORY_INTERACTION_COLOR, SB_CATEGORY_INTERACTION_OPACITY),
    /**
     * Unique category that is treated differently than the rest.
     */
    HIGHLIGHT("poi_highlight", sf("revanced_sb_segments_highlight"), sf("revanced_sb_segments_highlight_sum"), sf("revanced_sb_skip_button_highlight"), sf("revanced_sb_skipped_highlight"),
            SB_CATEGORY_HIGHLIGHT, SB_CATEGORY_HIGHLIGHT_COLOR, SB_CATEGORY_HIGHLIGHT_OPACITY),
    INTRO("intro", sf("revanced_sb_segments_intro"), sf("revanced_sb_segments_intro_sum"),
            sf("revanced_sb_skip_button_intro_beginning"), sf("revanced_sb_skip_button_intro_middle"), sf("revanced_sb_skip_button_intro_end"),
            sf("revanced_sb_skipped_intro_beginning"), sf("revanced_sb_skipped_intro_middle"), sf("revanced_sb_skipped_intro_end"),
            SB_CATEGORY_INTRO, SB_CATEGORY_INTRO_COLOR, SB_CATEGORY_INTRO_OPACITY),
    OUTRO("outro", sf("revanced_sb_segments_outro"), sf("revanced_sb_segments_outro_sum"), sf("revanced_sb_skip_button_outro"), sf("revanced_sb_skipped_outro"),
            SB_CATEGORY_OUTRO, SB_CATEGORY_OUTRO_COLOR, SB_CATEGORY_OUTRO_OPACITY),
    PREVIEW("preview", sf("revanced_sb_segments_preview"), sf("revanced_sb_segments_preview_sum"),
            sf("revanced_sb_skip_button_preview_beginning"), sf("revanced_sb_skip_button_preview_middle"), sf("revanced_sb_skip_button_preview_end"),
            sf("revanced_sb_skipped_preview_beginning"), sf("revanced_sb_skipped_preview_middle"), sf("revanced_sb_skipped_preview_end"),
            SB_CATEGORY_PREVIEW, SB_CATEGORY_PREVIEW_COLOR, SB_CATEGORY_PREVIEW_OPACITY),
    FILLER("filler", sf("revanced_sb_segments_filler"), sf("revanced_sb_segments_filler_sum"), sf("revanced_sb_skip_button_filler"), sf("revanced_sb_skipped_filler"),
            SB_CATEGORY_FILLER, SB_CATEGORY_FILLER_COLOR, SB_CATEGORY_FILLER_OPACITY),
    MUSIC_OFFTOPIC("music_offtopic", sf("revanced_sb_segments_nomusic"), sf("revanced_sb_segments_nomusic_sum"), sf("revanced_sb_skip_button_nomusic"), sf("revanced_sb_skipped_nomusic"),
            SB_CATEGORY_MUSIC_OFFTOPIC, SB_CATEGORY_MUSIC_OFFTOPIC_COLOR, SB_CATEGORY_MUSIC_OFFTOPIC_OPACITY),
    UNSUBMITTED("unsubmitted", StringRef.empty, StringRef.empty, sf("revanced_sb_skip_button_unsubmitted"), sf("revanced_sb_skipped_unsubmitted"),
            SB_CATEGORY_UNSUBMITTED, SB_CATEGORY_UNSUBMITTED_COLOR, SB_CATEGORY_UNSUBMITTED_OPACITY);

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

    public static SegmentCategory[] categoriesWithoutUnsubmitted() {
        return categoriesWithoutUnsubmitted;
    }

    public static SegmentCategory[] categoriesWithoutHighlights() {
        return categoriesWithoutHighlights;
    }

    @Nullable
    public static SegmentCategory byCategoryKey(@NonNull String key) {
        return mValuesMap.get(key);
    }

    /**
     * Must be called if behavior of any category is changed.
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

    public static int applyOpacityToColor(int color, float opacity) {
        if (opacity < 0 || opacity > 1.0f) {
            throw new IllegalArgumentException("Invalid opacity: " + opacity);
        }
        final int opacityInt = (int) (255 * opacity);
        return (color & 0x00FFFFFF) | (opacityInt << 24);
    }

    public final String keyValue;
    public final StringSetting behaviorSetting; // TODO: Replace with EnumSetting.
    private final StringSetting colorSetting;
    private final FloatSetting opacitySetting;

    public final StringRef title;
    public final StringRef description;

    /**
     * Skip button text, if the skip occurs in the first quarter of the video
     */
    public final StringRef skipButtonTextBeginning;
    /**
     * Skip button text, if the skip occurs in the middle half of the video
     */
    public final StringRef skipButtonTextMiddle;
    /**
     * Skip button text, if the skip occurs in the last quarter of the video
     */
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

    private int color;

    /**
     * Value must be changed using {@link #setBehaviour(CategoryBehaviour)}.
     * Caller must also {@link #updateEnabledCategories()}.
     */
    @NonNull
    public CategoryBehaviour behaviour = CategoryBehaviour.IGNORE;

    SegmentCategory(String keyValue, StringRef title, StringRef description,
                    StringRef skipButtonText,
                    StringRef skippedToastText,
                    StringSetting behavior,
                    StringSetting color, FloatSetting opacity) {
        this(keyValue, title, description,
                skipButtonText, skipButtonText, skipButtonText,
                skippedToastText, skippedToastText, skippedToastText,
                behavior,
                color, opacity);
    }

    SegmentCategory(String keyValue, StringRef title, StringRef description,
                    StringRef skipButtonTextBeginning, StringRef skipButtonTextMiddle, StringRef skipButtonTextEnd,
                    StringRef skippedToastBeginning, StringRef skippedToastMiddle, StringRef skippedToastEnd,
                    StringSetting behavior,
                    StringSetting color, FloatSetting opacity) {
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
        this.opacitySetting = Objects.requireNonNull(opacity);
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
        final float opacity = opacitySetting.get();
        try {
            setColor(colorString);
            setOpacity(opacity);
        } catch (Exception ex) {
            Logger.printException(() -> "Invalid color: " + colorString + " opacity: " + opacity, ex);
            colorSetting.resetToDefault();
            opacitySetting.resetToDefault();
            loadFromSettings();
        }
    }

    public void setBehaviour(@NonNull CategoryBehaviour behaviour) {
        this.behaviour = Objects.requireNonNull(behaviour);
        this.behaviorSetting.save(behaviour.reVancedKeyValue);
    }

    private void updateColor() {
        color = applyOpacityToColor(color, opacitySetting.get());
        paint.setColor(color);
    }

    /**
     * @param opacity Segment color opacity between [0, 1].
     */
    public void setOpacity(float opacity) throws IllegalArgumentException {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Invalid opacity: " + opacity);
        }

        opacitySetting.save(opacity);
        updateColor();
    }

    public float getOpacity() {
        return opacitySetting.get();
    }

    public void resetColorAndOpacity() {
        setColor(colorSetting.defaultValue);
        setOpacity(opacitySetting.defaultValue);
    }

    /**
     * @param colorString Segment color with #RRGGBB format.
     */
    public void setColor(String colorString) throws IllegalArgumentException {
        color = Color.parseColor(colorString);
        colorSetting.save(colorString);

        updateColor();
    }

    /**
     * @return Integer color of #RRGGBB format.
     */
    public int getColorNoOpacity() {
        return color & 0x00FFFFFF;
    }

    /**
     * @return Hex color string of #RRGGBB format with no opacity level.
     */
    public String getColorString() {
        return String.format(Locale.US, "#%06X", getColorNoOpacity());
    }

    private static SpannableString getCategoryColorDotSpan(String text, int color) {
        SpannableString dotSpan = new SpannableString('⬤' + text);
        dotSpan.setSpan(new ForegroundColorSpan(color), 0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return dotSpan;
    }

    public static SpannableString getCategoryColorDot(int color) {
        return getCategoryColorDotSpan("", color);
    }

    public SpannableString getCategoryColorDot() {
        return getCategoryColorDot(color);
    }

    public SpannableString getTitleWithColorDot(int categoryColor) {
        return getCategoryColorDotSpan(" " + title, categoryColor);
    }

    public SpannableString getTitleWithColorDot() {
        return getTitleWithColorDot(color);
    }

    /**
     * @param segmentStartTime video time the segment category started
     * @param videoLength      length of the video
     * @return the skip button text
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
     * @param segmentStartTime video time the segment category started
     * @param videoLength      length of the video
     * @return 'skipped segment' toast message
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
