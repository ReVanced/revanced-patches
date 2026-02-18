package app.revanced.extension.youtube.transcripts;

import static app.revanced.extension.shared.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import app.revanced.extension.shared.Logger;

/**
 * Displays transcript summaries to the user.
 */
public class SummaryViewer {
    private static final String TAG = "SummaryViewer";

    /**
     * Show summary in a dialog popup.
     * This is a temporary solution - in the future, summaries will be injected into the description.
     */
    public static void showSummaryDialog(@NonNull Context context, @NonNull String summary) {
        try {
            // Create a scrollable TextView for the summary
            TextView textView = new TextView(context);
            textView.setText(formatSummary(summary));
            textView.setTextSize(14);
            textView.setPadding(40, 40, 40, 40);
            textView.setMovementMethod(new ScrollingMovementMethod());
            
            // Wrap in a LinearLayout for better formatting
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(textView);

            // Create and show dialog
            new AlertDialog.Builder(context)
                .setTitle(str("revanced_summarize_dialog_title"))
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .show();

            Logger.printDebug(() -> TAG + " Summary dialog shown");
        } catch (Exception e) {
            Logger.printException(() -> TAG + " Failed to show summary dialog", e);
        }
    }

    /**
     * Format the summary text for better readability.
     * Converts markdown-style bullets to proper formatting.
     */
    @NonNull
    private static CharSequence formatSummary(@NonNull String summary) {
        // Replace markdown bullets with proper bullets
        String formatted = summary
            .replaceAll("^\\* ", "• ")
            .replaceAll("\\n\\* ", "\n• ")
            .replaceAll("^- ", "• ")
            .replaceAll("\\n- ", "\n• ")
            .replaceAll("\\n\\d+\\. ", "\n• ");

        // Add some spacing between lines
        formatted = formatted.replaceAll("\\n", "\n\n");

        return formatted;
    }

    /**
     * Future method: Inject summary into video description section.
     * This will be implemented when the description injection patch is created.
     */
    public static void injectSummaryIntoDescription(@NonNull Context context, 
                                                    @NonNull String summary) {
        // TODO: Implement description injection
        // This will create an expandable section in the video description
        Logger.printDebug(() -> TAG + " Description injection not yet implemented");
    }
}
