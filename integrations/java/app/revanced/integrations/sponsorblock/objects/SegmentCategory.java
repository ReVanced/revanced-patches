package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.objects.CategoryBehaviour.IGNORE;
import static app.revanced.integrations.sponsorblock.objects.CategoryBehaviour.MANUAL_SKIP;
import static app.revanced.integrations.sponsorblock.objects.CategoryBehaviour.SKIP_AUTOMATICALLY;
import static app.revanced.integrations.sponsorblock.objects.CategoryBehaviour.SKIP_AUTOMATICALLY_ONCE;
import static app.revanced.integrations.utils.StringRef.sf;

import android.content.SharedPreferences;
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

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.settings.SharedPrefCategory;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.StringRef;

public enum SegmentCategory {
    SPONSOR("sponsor", sf("sb_segments_sponsor"), sf("sb_segments_sponsor_sum"), sf("sb_skip_button_sponsor"), sf("sb_skipped_sponsor"),
            SKIP_AUTOMATICALLY_ONCE, 0x00D400),
    SELF_PROMO("selfpromo", sf("sb_segments_selfpromo"), sf("sb_segments_selfpromo_sum"), sf("sb_skip_button_selfpromo"), sf("sb_skipped_selfpromo"),
            MANUAL_SKIP, 0xFFFF00),
    INTERACTION("interaction", sf("sb_segments_interaction"), sf("sb_segments_interaction_sum"), sf("sb_skip_button_interaction"), sf("sb_skipped_interaction"),
            MANUAL_SKIP, 0xCC00FF),
    /**
     * Unique category that is treated differently than the rest.
     */
    HIGHLIGHT("poi_highlight", sf("sb_segments_highlight"), sf("sb_segments_highlight_sum"), sf("sb_skip_button_highlight"), sf("sb_skipped_highlight"),
            MANUAL_SKIP, 0xFF1684),
    INTRO("intro", sf("sb_segments_intro"), sf("sb_segments_intro_sum"),
            sf("sb_skip_button_intro_beginning"), sf("sb_skip_button_intro_middle"), sf("sb_skip_button_intro_end"),
            sf("sb_skipped_intro_beginning"), sf("sb_skipped_intro_middle"), sf("sb_skipped_intro_end"),
            MANUAL_SKIP, 0x00FFFF),
    OUTRO("outro", sf("sb_segments_outro"), sf("sb_segments_outro_sum"), sf("sb_skip_button_outro"), sf("sb_skipped_outro"),
            MANUAL_SKIP, 0x0202ED),
    PREVIEW("preview", sf("sb_segments_preview"), sf("sb_segments_preview_sum"),
            sf("sb_skip_button_preview_beginning"), sf("sb_skip_button_preview_middle"), sf("sb_skip_button_preview_end"),
            sf("sb_skipped_preview_beginning"), sf("sb_skipped_preview_middle"), sf("sb_skipped_preview_end"),
            IGNORE, 0x008FD6),
    FILLER("filler", sf("sb_segments_filler"), sf("sb_segments_filler_sum"), sf("sb_skip_button_filler"), sf("sb_skipped_filler"),
            IGNORE, 0x7300FF),
    MUSIC_OFFTOPIC("music_offtopic", sf("sb_segments_nomusic"), sf("sb_segments_nomusic_sum"), sf("sb_skip_button_nomusic"), sf("sb_skipped_nomusic"),
            MANUAL_SKIP, 0xFF9900),
    UNSUBMITTED("unsubmitted", StringRef.empty, StringRef.empty, sf("sb_skip_button_unsubmitted"), sf("sb_skipped_unsubmitted"),
            SKIP_AUTOMATICALLY, 0xFFFFFF);

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

    private static final String COLOR_PREFERENCE_KEY_SUFFIX = "_color";

    /**
     * Categories currently enabled, formatted for an API call
     */
    public static String sponsorBlockAPIFetchCategories = "[]";

    static {
        for (SegmentCategory value : categoriesWithoutUnsubmitted)
            mValuesMap.put(value.key, value);
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

    public static void loadFromPreferences() {
        SharedPreferences preferences = SharedPrefCategory.SPONSOR_BLOCK.preferences;
        LogHelper.printDebug(() -> "loadFromPreferences");
        for (SegmentCategory category : categoriesWithoutUnsubmitted()) {
            category.load(preferences);
        }
        updateEnabledCategories();
    }

    /**
     * Must be called if behavior of any category is changed
     */
    public static void updateEnabledCategories() {
        SegmentCategory[] categories = categoriesWithoutUnsubmitted();
        List<String> enabledCategories = new ArrayList<>(categories.length);
        for (SegmentCategory category : categories) {
            if (category.behaviour != CategoryBehaviour.IGNORE) {
                enabledCategories.add(category.key);
            }
        }

        //"[%22sponsor%22,%22outro%22,%22music_offtopic%22,%22intro%22,%22selfpromo%22,%22interaction%22,%22preview%22]";
        if (enabledCategories.isEmpty())
            sponsorBlockAPIFetchCategories = "[]";
        else
            sponsorBlockAPIFetchCategories = "[%22" + TextUtils.join("%22,%22", enabledCategories) + "%22]";
    }

    @NonNull
    public final String key;
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
    public final int defaultColor;
    /**
     * If value is changed, then also call {@link #save(SharedPreferences.Editor)}
     */
    public int color;

    /**
     * If value is changed, then also call {@link #updateEnabledCategories()}
     */
    @NonNull
    public CategoryBehaviour behaviour;

    SegmentCategory(String key, StringRef title, StringRef description,
                    StringRef skipButtonText,
                    StringRef skippedToastText,
                    CategoryBehaviour defaultBehavior, int defaultColor) {
        this(key, title, description,
                skipButtonText, skipButtonText, skipButtonText,
                skippedToastText, skippedToastText, skippedToastText,
                defaultBehavior, defaultColor);
    }

    SegmentCategory(String key, StringRef title, StringRef description,
                    StringRef skipButtonTextBeginning, StringRef skipButtonTextMiddle, StringRef skipButtonTextEnd,
                    StringRef skippedToastBeginning, StringRef skippedToastMiddle, StringRef skippedToastEnd,
                    CategoryBehaviour defaultBehavior, int defaultColor) {
        this.key = Objects.requireNonNull(key);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.skipButtonTextBeginning = Objects.requireNonNull(skipButtonTextBeginning);
        this.skipButtonTextMiddle = Objects.requireNonNull(skipButtonTextMiddle);
        this.skipButtonTextEnd = Objects.requireNonNull(skipButtonTextEnd);
        this.skippedToastBeginning = Objects.requireNonNull(skippedToastBeginning);
        this.skippedToastMiddle = Objects.requireNonNull(skippedToastMiddle);
        this.skippedToastEnd = Objects.requireNonNull(skippedToastEnd);
        this.behaviour = Objects.requireNonNull(defaultBehavior);
        this.color = this.defaultColor = defaultColor;
        this.paint = new Paint();
        setColor(defaultColor);
    }

    /**
     * Caller must also call {@link #updateEnabledCategories()}
     */
    private void load(SharedPreferences preferences) {
        String categoryColor = preferences.getString(key + COLOR_PREFERENCE_KEY_SUFFIX, null);
        if (categoryColor == null) {
            setColor(defaultColor);
        } else {
            setColor(categoryColor);
        }

        String behaviorString = preferences.getString(key, null);
        if (behaviorString != null) {
            CategoryBehaviour preferenceBehavior = CategoryBehaviour.byStringKey(behaviorString);
            if (preferenceBehavior == null) {
                LogHelper.printException(() -> "Unknown behavior: " + behaviorString); // should never happen
            } else {
                behaviour = preferenceBehavior;
            }
        }
    }

    /**
     * Saves the current color and behavior.
     * Calling code is responsible for calling {@link SharedPreferences.Editor#apply()}
     */
    public void save(SharedPreferences.Editor editor) {
        String colorString = (color == defaultColor)
                ? null // remove any saved preference, so default is used on the next load
                : colorString();
        editor.putString(key + COLOR_PREFERENCE_KEY_SUFFIX, colorString);
        editor.putString(key, behaviour.key);
    }

    /**
     * @return HTML color format string
     */
    @NonNull
    public String colorString() {
        return String.format("#%06X", color);
    }

    public void setColor(@NonNull String colorString) throws IllegalArgumentException {
        setColor(Color.parseColor(colorString));
    }

    public void setColor(int color) {
        color &= 0xFFFFFF;
        this.color = color;
        paint.setColor(color);
        paint.setAlpha(255);
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
        if (SettingsEnum.SB_USE_COMPACT_SKIPBUTTON.getBoolean()) {
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
