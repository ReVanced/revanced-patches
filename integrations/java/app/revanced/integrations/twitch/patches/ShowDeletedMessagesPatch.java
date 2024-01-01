package app.revanced.integrations.twitch.patches;

import static app.revanced.integrations.shared.StringRef.str;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import androidx.annotation.Nullable;

import app.revanced.integrations.twitch.settings.Settings;
import tv.twitch.android.shared.chat.util.ClickableUsernameSpan;

@SuppressWarnings("unused")
public class ShowDeletedMessagesPatch {
    public static boolean shouldUseSpoiler() {
        return "spoiler".equals(Settings.SHOW_DELETED_MESSAGES.get());
    }

    public static boolean shouldCrossOut() {
        return "cross-out".equals(Settings.SHOW_DELETED_MESSAGES.get());
    }

    @Nullable
    public static Spanned reformatDeletedMessage(Spanned original) {
        if (!shouldCrossOut())
            return null;

        SpannableStringBuilder ssb = new SpannableStringBuilder(original);
        ssb.setSpan(new StrikethroughSpan(), 0, original.length(), 0);
        ssb.append(" (").append(str("revanced_deleted_msg")).append(")");
        ssb.setSpan(new StyleSpan(Typeface.ITALIC), original.length(), ssb.length(), 0);

        // Gray-out username
        ClickableUsernameSpan[] usernameSpans = original.getSpans(0, original.length(), ClickableUsernameSpan.class);
        if (usernameSpans.length > 0) {
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#ADADB8")), 0, original.getSpanEnd(usernameSpans[0]), 0);
        }

        return new SpannedString(ssb);
    }
}
