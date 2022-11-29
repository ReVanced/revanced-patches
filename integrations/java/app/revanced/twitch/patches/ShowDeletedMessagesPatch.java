package app.revanced.twitch.patches;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import java.util.Objects;

import app.revanced.twitch.settings.SettingsEnum;
import app.revanced.twitch.utils.ReVancedUtils;
import tv.twitch.android.shared.chat.util.ClickableUsernameSpan;

public class ShowDeletedMessagesPatch {
    public static boolean shouldUseSpoiler() {
        return Objects.equals(SettingsEnum.SHOW_DELETED_MESSAGES.getString(), "spoiler");
    }

    public static boolean shouldCrossOut() {
        return Objects.equals(SettingsEnum.SHOW_DELETED_MESSAGES.getString(), "cross-out");
    }

    public static Spanned reformatDeletedMessage(Spanned original) {
        if (!shouldCrossOut())
            return null;

        SpannableStringBuilder ssb = new SpannableStringBuilder(original);
        ssb.setSpan(new StrikethroughSpan(), 0, original.length(), 0);
        ssb.append(" (").append(ReVancedUtils.getString("revanced_deleted_msg")).append(")");
        ssb.setSpan(new StyleSpan(Typeface.ITALIC), original.length(), ssb.length(), 0);

        // Gray-out username
        ClickableUsernameSpan[] usernameSpans = original.getSpans(0, original.length(), ClickableUsernameSpan.class);
        if (usernameSpans.length > 0) {
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#ADADB8")), 0, original.getSpanEnd(usernameSpans[0]), 0);
        }

        return new SpannedString(ssb);
    }
}
