package app.revanced.extension.shared.settings.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.MotionEvent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.patches.EnableDebuggingPatch;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.ui.CustomDialog;
import android.util.Pair;

import androidx.annotation.Nullable;

/**
 * A custom preference that opens a dialog for managing feature flags.
 * Allows moving boolean flags between active and blocked states with advanced selection.
 */
@SuppressWarnings({"deprecation", "unused"})
public class FeatureFlagsManagerPreference extends Preference {

    // Hardcoded whitelist of flags to hide from the UI.
    private static final Set<Long> HIDDEN_FLAGS = new HashSet<>();
    static {
        HIDDEN_FLAGS.add(12345678L); // Example hidden flag.
    }

    // Track last long-pressed position for shift-selection.
    private int lastLongPressedPosition = -1;
    private boolean isShiftSelecting = false;

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

        // Load all known and disabled flags.
        Set<Long> allKnownFlags = EnableDebuggingPatch.getAllLoggedFlags();
        Set<Long> disabledFlags = EnableDebuggingPatch.parseFlags(BaseSettings.DISABLED_FEATURE_FLAGS.get());

        if (allKnownFlags.isEmpty()) {
            Toast.makeText(context,
                    "No feature flags logged yet. Enable debugging and restart the app.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Filter out hidden flags.
        allKnownFlags.removeAll(HIDDEN_FLAGS);

        List<Long> availableFlags = new ArrayList<>();
        List<Long> blockedFlags = new ArrayList<>();

        for (Long flag : allKnownFlags) {
            if (disabledFlags.contains(flag)) {
                blockedFlags.add(flag);
            } else {
                availableFlags.add(flag);
            }
        }

        Collections.sort(availableFlags);
        Collections.sort(blockedFlags);

        // Use CustomDialog for styled appearance.
        Pair<android.app.Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                "Feature Flags Manager",
                null,
                null,
                "Save",
                () -> saveFlags(blockedFlags),
                () -> {}, // Cancel does nothing
                "Reset",
                this::resetFlags,
                true
        );

        android.app.Dialog dialog = dialogPair.first;
        LinearLayout mainLayout = dialogPair.second;

        View dialogView = createDialogView(context, availableFlags, blockedFlags, dialog);
        mainLayout.addView(dialogView);

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private View createDialogView(Context context, List<Long> availableFlags, List<Long> blockedFlags, android.app.Dialog dialog) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);

        // Headers.
        LinearLayout headersLayout = createHeaders(context, availableFlags, blockedFlags);

        // Content with columns and buttons.
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));

        // Left: Active flags.
        ListView availableListView = createFlagsListView(context, availableFlags, true);
        TextView availableCountText = (TextView) headersLayout.getChildAt(0);

        // Right: Blocked flags.
        ListView blockedListView = createFlagsListView(context, blockedFlags, false);
        TextView blockedCountText = (TextView) headersLayout.getChildAt(2);

        // Button panel: > >> < <<
        LinearLayout buttonsLayout = createMoveButtons(
                context, availableListView, blockedListView,
                availableFlags, blockedFlags,
                availableCountText, blockedCountText
        );

        contentLayout.addView(availableListView);
        contentLayout.addView(buttonsLayout);
        contentLayout.addView(blockedListView);

        mainLayout.addView(headersLayout);
        mainLayout.addView(contentLayout);

        return mainLayout;
    }

    private LinearLayout createHeaders(Context context, List<Long> availableFlags, List<Long> blockedFlags) {
        LinearLayout headersLayout = new LinearLayout(context);
        headersLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView availableCountText = new TextView(context);
        availableCountText.setText("Active Flags (" + availableFlags.size() + ")");
        availableCountText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        availableCountText.setTextSize(16);
        availableCountText.setPadding(0, 0, 10, 10);

        TextView spacer = new TextView(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView blockedCountText = new TextView(context);
        blockedCountText.setText("Blocked Flags (" + blockedFlags.size() + ")");
        blockedCountText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        blockedCountText.setTextSize(16);
        blockedCountText.setPadding(10, 0, 0, 10);

        headersLayout.addView(availableCountText);
        headersLayout.addView(spacer);
        headersLayout.addView(blockedCountText);

        return headersLayout;
    }

    private ListView createFlagsListView(Context context, List<Long> flags, boolean isAvailable) {
        ListView listView = new ListView(context);
        listView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_multiple_choice,
                convertFlagsToStrings(flags));
        listView.setAdapter(adapter);

        // Long click for shift selection.
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (lastLongPressedPosition == -1) {
                lastLongPressedPosition = position;
                isShiftSelecting = true;
                toggleSelectionRange(listView, position, position);
            } else {
                int start = Math.min(lastLongPressedPosition, position);
                int end = Math.max(lastLongPressedPosition, position);
                toggleSelectionRange(listView, start, end);
                lastLongPressedPosition = position;
            }
            return true;
        });

        // Reset on touch outside long press.
        listView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && isShiftSelecting) {
                isShiftSelecting = false;
            }
            return false;
        });

        return listView;
    }

    private void toggleSelectionRange(ListView listView, int start, int end) {
        for (int i = start; i <= end; i++) {
            listView.setItemChecked(i, true);
        }
    }

    private LinearLayout createMoveButtons(Context context,
                                           ListView availableListView, ListView blockedListView,
                                           List<Long> availableFlags, List<Long> blockedFlags,
                                           TextView availableCountText, TextView blockedCountText) {
        LinearLayout buttonsLayout = new LinearLayout(context);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        buttonsLayout.setGravity(Gravity.CENTER);
        buttonsLayout.setPadding(10, 0, 10, 0);

        Button moveOneRight = createMoveButton(context, ">", () ->
                moveSelectedFlags(availableListView, blockedListView, availableFlags,blockedFlags,
                        availableCountText, blockedCountText, true, false));

        Button moveAllRight = createMoveButton(context, ">>", () ->
                moveAllFlags(availableListView, blockedListView, availableFlags, blockedFlags,
                        availableCountText, blockedCountText, true));

        Space space1 = new Space(context);
        space1.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 20));

        Button moveOneLeft = createMoveButton(context, "<", () ->
                moveSelectedFlags(blockedListView, availableListView, blockedFlags, availableFlags,
                        blockedCountText, availableCountText, false, false));

        Button moveAllLeft = createMoveButton(context, "<<", () ->
                moveAllFlags(blockedListView, availableListView, blockedFlags, availableFlags,
                        blockedCountText, availableCountText, false));

        Space space2 = new Space(context);
        space2.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 20));

        buttonsLayout.addView(moveOneRight);
        buttonsLayout.addView(moveAllRight);
        buttonsLayout.addView(space1);
        buttonsLayout.addView(moveOneLeft);
        buttonsLayout.addView(moveAllLeft);
        buttonsLayout.addView(space2);

        return buttonsLayout;
    }

    private Button createMoveButton(Context context, String text, Runnable action) {
        Button button = new Button(context);
        button.setText(text);
        button.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    @SuppressLint("SetTextI18n")
    private void moveSelectedFlags(@Nullable ListView fromListView, @Nullable ListView toListView,
                                   List<Long> fromFlags, List<Long> toFlags,
                                   TextView fromCountText, TextView toCountText,
                                   boolean toBlocked, boolean moveAll) {
        List<Long> toMove = new ArrayList<>();
        SparseBooleanArray checked;

        if (moveAll) {
            toMove.addAll(fromFlags);
        } else {
            if (fromListView == null) return;
            checked = fromListView.getCheckedItemPositions();
            for (int i = 0; i < fromFlags.size(); i++) {
                if (checked.get(i)) {
                    toMove.add(fromFlags.get(i));
                }
            }
        }

        if (toMove.isEmpty()) return;

        fromFlags.removeAll(toMove);
        toFlags.addAll(toMove);
        Collections.sort(toFlags);

        // Use stored adapters instead of casting
        updateListView(fromListView, fromFlags, fromCountText, moveAll);
        updateListView(toListView, toFlags, toCountText, moveAll);

        if (!moveAll) {
            fromListView.clearChoices();
        }
        if (!moveAll && toListView != null) {
            toListView.clearChoices();
        }

        lastLongPressedPosition = -1;
        isShiftSelecting = false;
    }

    // New: moveAllFlags now uses actual ListView references
    private void moveAllFlags(ListView fromListView, ListView toListView,
                              List<Long> fromFlags, List<Long> toFlags,
                              TextView fromCountText, TextView toCountText, boolean toBlocked) {
        moveSelectedFlags(fromListView, toListView, fromFlags, toFlags, fromCountText, toCountText, toBlocked, true);
    }

    // Replace updateListView to avoid unchecked cast
    private void updateListView(@Nullable ListView listView, List<Long> flags, TextView countText, boolean moveAll) {
        if (listView == null) return;

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
        adapter.clear();
        adapter.addAll(convertFlagsToStrings(flags));
        adapter.notifyDataSetChanged();

        String label = listView.getId() == android.R.id.list ? "Active" : "Blocked"; // fallback
        // Better: determine by count text
        String currentText = countText.getText().toString();
        String prefix = currentText.contains("Active") ? "Active" : "Blocked";
        countText.setText(prefix + " Flags (" + flags.size() + ")");
    }

    private List<String> convertFlagsToStrings(List<Long> flags) {
        List<String> result = new ArrayList<>();
        for (Long flag : flags) {
            result.add(String.valueOf(flag));
        }
        return result;
    }

    private void saveFlags(List<Long> blockedFlags) {
        StringBuilder flagsString = new StringBuilder();
        for (Long flag : blockedFlags) {
            if (flagsString.length() > 0) {
                flagsString.append('\n'); // Use actual newline.
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
