package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.settings.preference.BulletPointPreference.formatIntoBulletPoints;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

/**
 * Formats the summary text bullet points into Spanned text for better presentation.
 */
@SuppressWarnings({"unused", "deprecation"})
public class BulletPointSwitchPreference extends SwitchPreference {

    public BulletPointSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public BulletPointSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BulletPointSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BulletPointSwitchPreference(Context context) {
        super(context);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(formatIntoBulletPoints(summary));
    }

    @Override
    public void setSummaryOn(CharSequence summaryOn) {
        super.setSummaryOn(formatIntoBulletPoints(summaryOn));
    }

    @Override
    public void setSummaryOff(CharSequence summaryOff) {
        super.setSummaryOff(formatIntoBulletPoints(summaryOff));
    }
}
