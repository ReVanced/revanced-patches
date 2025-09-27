package app.revanced.extension.shared.settings.preference;

import android.content.Context;
import android.preference.Preference;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.AttributeSet;

/**
 * Formats the summary text bullet points into Spanned text for better presentation.
 */
@SuppressWarnings({"unused", "deprecation"})
public class BulletPointPreference extends Preference {

    /**
     * Replaces bullet points with styled spans.
     */
    public static CharSequence formatIntoBulletPoints(CharSequence source) {
        final char bulletPoint = '•';
        if (TextUtils.indexOf(source, bulletPoint) < 0) {
            return source; // Nothing to do.
        }

        SpannableStringBuilder builder = new SpannableStringBuilder(source);

        int lineStart = 0;
        int length = builder.length();

        while (lineStart < length) {
            int lineEnd = TextUtils.indexOf(builder, '\n', lineStart);
            if (lineEnd < 0) lineEnd = length;

            // Apply BulletSpan only if the line starts with the '•' character.
            if (lineEnd > lineStart && builder.charAt(lineStart) == bulletPoint) {
                int deleteEnd = lineStart + 1; // remove the bullet itself

                // If there's a single space right after the bullet, remove that too.
                if (deleteEnd < builder.length() && builder.charAt(deleteEnd) == ' ') {
                    deleteEnd++;
                }

                builder.delete(lineStart, deleteEnd);

                // Apply the BulletSpan to the remainder of that line.
                builder.setSpan(new BulletSpan(20),
                        lineStart,
                        lineEnd - (deleteEnd - lineStart), // adjust for deleted chars.
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                // Update total length and lineEnd after deletion.
                length = builder.length();
                final int removed = deleteEnd - lineStart;
                lineEnd -= removed;
            }

            lineStart = lineEnd + 1;
        }

        return new SpannedString(builder);
    }

    public BulletPointPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public BulletPointPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BulletPointPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BulletPointPreference(Context context) {
        super(context);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(formatIntoBulletPoints(summary));
    }
}
