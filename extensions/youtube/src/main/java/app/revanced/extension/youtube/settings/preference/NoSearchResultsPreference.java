package app.revanced.extension.youtube.settings.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.revanced.extension.shared.StringRef;
import app.revanced.extension.shared.Utils;

/**
 * Custom Preference for the "No Search Results Found" screen.
 */
@SuppressWarnings("deprecation")
public class NoSearchResultsPreference extends Preference {
    private final String query;
    private final Runnable onTryAgainClick;

    public NoSearchResultsPreference(Context context, String query, Runnable onTryAgainClick) {
        super(context);
        this.query = query;
        this.onTryAgainClick = onTryAgainClick;
        setSelectable(false);
    }

    @SuppressLint("ResourceType")
    @Override
    protected View onCreateView(ViewGroup parent) {
        // Create the root LinearLayout.
        super.onCreateView(parent);
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Utils.getAppBackgroundColor());
        final int dip24 = Utils.dipToPixels(24);
        layout.setPadding(dip24, dip24, dip24, dip24);

        // Create the ImageView for the icon.
        ImageView icon = new ImageView(getContext());
        int iconSize = Utils.dipToPixels(96);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        icon.setLayoutParams(iconParams);
        int iconResId = Utils.getResourceIdentifier("revanced_settings_no_search_result_icon", "drawable");
        icon.setImageResource(iconResId);
        icon.setColorFilter(Utils.getAppForegroundColor());
        icon.setContentDescription(null);
        layout.addView(icon);

        // Create the TextView for the title.
        TextView title = new TextView(getContext());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Utils.dipToPixels(16);
        title.setLayoutParams(titleParams);
        title.setText(StringRef.str("revanced_settings_search_no_results_title", query));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTextColor(Utils.getAppForegroundColor());
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        title.setMaxLines(3);
        title.setTextAppearance(getContext(), android.R.attr.textAppearanceLarge);
        layout.addView(title);

        // Create the TextView for the summary.
        TextView summary = new TextView(getContext());
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        summaryParams.topMargin = Utils.dipToPixels(8);
        summaryParams.leftMargin = Utils.dipToPixels(24);
        summaryParams.rightMargin = Utils.dipToPixels(24);
        summary.setLayoutParams(summaryParams);
        summary.setText(StringRef.str("revanced_settings_search_no_results_summary"));
        summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        summary.setTextColor(Utils.getAppForegroundColor());
        summary.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        summary.setEllipsize(TextUtils.TruncateAt.END);
        summary.setTextAppearance(getContext(), android.R.attr.textAppearanceSmall);
        layout.addView(summary);

        // Add a spacer to push the button to the bottom.
        View spacer = new View(getContext());
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.dipToPixels(16)
        );
        spacer.setLayoutParams(spacerParams);
        layout.addView(spacer);

        // Create the Try Again button.
        Button tryAgainButton = new Button(getContext(), null, 0);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Utils.dipToPixels(40)); // Button height.
        buttonParams.bottomMargin = Utils.dipToPixels(16);
        tryAgainButton.setLayoutParams(buttonParams);
        tryAgainButton.setText(StringRef.str("revanced_settings_search_try_again"));
        tryAgainButton.setTextSize(16);
        tryAgainButton.setAllCaps(true);
        tryAgainButton.setGravity(Gravity.CENTER);

        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(20), null, null));
        int backgroundColor = Utils.getCancelOrNeutralButtonBackgroundColor();
        background.getPaint().setColor(backgroundColor);
        tryAgainButton.setBackground(background);
        tryAgainButton.setTextColor(Utils.getAppForegroundColor());
        tryAgainButton.setPadding(Utils.dipToPixels(16), 0, Utils.dipToPixels(16), 0);

        tryAgainButton.setOnClickListener(v -> onTryAgainClick.run());
        layout.addView(tryAgainButton);

        return layout;
    }
}
