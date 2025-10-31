package app.revanced.extension.shared.settings.preference;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.patches.EnableDebuggingPatch;
import app.revanced.extension.shared.settings.BaseSettings;

/**
 * A custom preference that opens a dialog for managing feature flags.
 * Allows moving boolean flags between active and blocked states.
 */
@SuppressWarnings({"deprecation", "unused"})
public class FeatureFlagsManagerPreference extends Preference {

    {
        setOnPreferenceClickListener(pref -> {
            showFlagsManagerDialog();
            return true;
        });
    }

    public FeatureFlagsManagerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FeatureFlagsManagerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FeatureFlagsManagerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FeatureFlagsManagerPreference(Context context) {
        super(context);
    }

    private void showFlagsManagerDialog() {
        Context context = getContext();

        // Initialize data
        List<Long> availableFlags = new ArrayList<>();
        List<Long> blockedFlags = new ArrayList<>();

        // Load blocked flags from settings
        Set<Long> disabledFlags = EnableDebuggingPatch.parseFlags(
                BaseSettings.DISABLED_FEATURE_FLAGS.get()
        );

        // Load all known flags from logs
        Set<Long> allKnownFlags = EnableDebuggingPatch.getAllLoggedFlags();

        if (allKnownFlags.isEmpty()) {
            Toast.makeText(context,
                    "No feature flags logged yet. Enable debugging and restart the app.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        for (Long flag : allKnownFlags) {
            if (disabledFlags.contains(flag)) {
                blockedFlags.add(flag);
            } else {
                availableFlags.add(flag);
            }
        }

        // Sort for convenience
        Collections.sort(availableFlags);
        Collections.sort(blockedFlags);

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Feature Flags Manager");

        View dialogView = createDialogView(context, availableFlags, blockedFlags);
        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> saveFlags(blockedFlags));

        builder.setNegativeButton("Cancel", null);

        builder.setNeutralButton("Reset", (dialog, which) -> resetFlags());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private View createDialogView(Context context, List<Long> availableFlags, List<Long> blockedFlags) {
        // Create layout programmatically
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);

        // Column headers
        LinearLayout headersLayout = new LinearLayout(context);
        headersLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView availableCountText = new TextView(context);
        availableCountText.setText("Active Flags (" + availableFlags.size() + ")");
        availableCountText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        availableCountText.setTextSize(16);
        availableCountText.setPadding(0, 0, 10, 10);

        TextView spacer1 = new TextView(context);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(
                100, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView blockedCountText = new TextView(context);
        blockedCountText.setText("Blocked Flags (" + blockedFlags.size() + ")");
        blockedCountText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        blockedCountText.setTextSize(16);
        blockedCountText.setPadding(10, 0, 0, 10);

        headersLayout.addView(availableCountText);
        headersLayout.addView(spacer1);
        headersLayout.addView(blockedCountText);

        // Content layout with columns and buttons
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));

        // Left column (active flags)
        ListView availableFlagsListView = new ListView(context);
        availableFlagsListView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        availableFlagsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        List<String> availableFlagsStrings = convertFlagsToStrings(availableFlags);
        ArrayAdapter<String> availableFlagsAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_multiple_choice, availableFlagsStrings);
        availableFlagsListView.setAdapter(availableFlagsAdapter);

        // Move buttons
        LinearLayout buttonsLayout = new LinearLayout(context);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        buttonsLayout.setGravity(Gravity.CENTER);
        buttonsLayout.setPadding(10, 0, 10, 0);

        Button moveToBlockedButton = new Button(context);
        moveToBlockedButton.setText(">");
        moveToBlockedButton.setLayoutParams(new LinearLayout.LayoutParams(
                80, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button moveToAvailableButton = new Button(context);
        moveToAvailableButton.setText("<");
        moveToAvailableButton.setLayoutParams(new LinearLayout.LayoutParams(
                80, LinearLayout.LayoutParams.WRAP_CONTENT));

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 20));

        buttonsLayout.addView(moveToBlockedButton);
        buttonsLayout.addView(space);
        buttonsLayout.addView(moveToAvailableButton);

        // Right column (blocked flags)
        ListView blockedFlagsListView = new ListView(context);
        blockedFlagsListView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        blockedFlagsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        List<String> blockedFlagsStrings = convertFlagsToStrings(blockedFlags);
        ArrayAdapter<String> blockedFlagsAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_multiple_choice, blockedFlagsStrings);
        blockedFlagsListView.setAdapter(blockedFlagsAdapter);

        // Button click handlers
        moveToBlockedButton.setOnClickListener(v ->
            moveSelectedFlags(availableFlagsListView, blockedFlagsListView,
                    availableFlags, blockedFlags, availableFlagsAdapter,
                    blockedFlagsAdapter, availableCountText, blockedCountText, true)
        );

        moveToAvailableButton.setOnClickListener(v ->
            moveSelectedFlags(availableFlagsListView, blockedFlagsListView,
                    availableFlags, blockedFlags, availableFlagsAdapter,
                    blockedFlagsAdapter, availableCountText, blockedCountText, false)
        );

        contentLayout.addView(availableFlagsListView);
        contentLayout.addView(buttonsLayout);
        contentLayout.addView(blockedFlagsListView);

        mainLayout.addView(headersLayout);
        mainLayout.addView(contentLayout);

        return mainLayout;
    }

    @SuppressLint("SetTextI18n")
    private void moveSelectedFlags(ListView availableListView, ListView blockedListView,
                                   List<Long> availableFlags, List<Long> blockedFlags,
                                   ArrayAdapter<String> availableAdapter,
                                   ArrayAdapter<String> blockedAdapter,
                                   TextView availableCountText, TextView blockedCountText,
                                   boolean toBlocked) {
        if (toBlocked) {
            // Move from availableFlags to blockedFlags
            List<Long> toMove = new ArrayList<>();
            SparseBooleanArray checked = availableListView.getCheckedItemPositions();

            for (int i = 0; i < availableFlags.size(); i++) {
                if (checked.get(i)) {
                    toMove.add(availableFlags.get(i));
                }
            }

            availableFlags.removeAll(toMove);
            blockedFlags.addAll(toMove);
            Collections.sort(blockedFlags);

        } else {
            // Move from blockedFlags to availableFlags
            List<Long> toMove = new ArrayList<>();
            SparseBooleanArray checked = blockedListView.getCheckedItemPositions();

            for (int i = 0; i < blockedFlags.size(); i++) {
                if (checked.get(i)) {
                    toMove.add(blockedFlags.get(i));
                }
            }

            blockedFlags.removeAll(toMove);
            availableFlags.addAll(toMove);
            Collections.sort(availableFlags);
        }

        // Update adapters
        availableAdapter.clear();
        availableAdapter.addAll(convertFlagsToStrings(availableFlags));
        availableAdapter.notifyDataSetChanged();

        blockedAdapter.clear();
        blockedAdapter.addAll(convertFlagsToStrings(blockedFlags));
        blockedAdapter.notifyDataSetChanged();

        availableCountText.setText("Active Flags (" + availableFlags.size() + ")");
        blockedCountText.setText("Blocked Flags (" + blockedFlags.size() + ")");

        // Clear selections
        availableListView.clearChoices();
        blockedListView.clearChoices();
    }

    private List<String> convertFlagsToStrings(List<Long> flags) {
        List<String> result = new ArrayList<>();
        for (Long flag : flags) {
            result.add(String.valueOf(flag));
        }
        return result;
    }

    private void saveFlags(List<Long> blockedFlags) {
        // Save blocked flags to settings
        StringBuilder flagsString = new StringBuilder();
        for (Long flag : blockedFlags) {
            if (flagsString.length() > 0) {
                flagsString.append("\n");
            }
            flagsString.append(flag);
        }

        BaseSettings.DISABLED_FEATURE_FLAGS.save(flagsString.toString());

        Toast.makeText(getContext(),
                "Flags saved. Restart the app to apply changes.",
                Toast.LENGTH_LONG).show();

        Logger.printDebug(() -> "Feature flags saved. Blocked: " + blockedFlags.size());
    }

    private void resetFlags() {
        BaseSettings.DISABLED_FEATURE_FLAGS.save("");
        Toast.makeText(getContext(),
                "Flags reset. Restart the app to apply changes.",
                Toast.LENGTH_LONG).show();
    }
}
