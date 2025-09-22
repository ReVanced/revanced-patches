package app.revanced.extension.youtube.patches;

import java.util.regex.Pattern;
import android.text.SpannableString;
import app.revanced.extension.youtube.settings.Settings;

public final class MetadataStringHelperPatch {
    
    private static final Pattern VIEWS_PATTERN = 
        Pattern.compile("[\\d,.]+[KMB]?\\s*views\\s*[\\u00b7·]\\s*");

    /**
     * Filters metadata string by removing view counts when both "views" and "ago" are present
     * @param metadataString The original metadata string
     * @return Filtered metadata string without view counts
     */
    public static SpannableString filterMetadataString(SpannableString metadataString) {
        if (!Settings.HIDE_VIEW_COUNT.get()) {
            return metadataString;
        }

        if (metadataString == null) {
            return null;
        }
        
        String str = metadataString.toString();
        
        if (str.contains("views") && str.contains("ago")) {
            return SpannableString.valueOf(VIEWS_PATTERN.matcher(str).replaceAll(" ").trim());
        }
        
        return metadataString;
    }
}