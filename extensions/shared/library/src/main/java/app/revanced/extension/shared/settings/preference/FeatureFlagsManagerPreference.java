package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.patches.EnableDebuggingPatch;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.ui.CustomDialog;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom preference that opens a dialog for managing feature flags.
 * Allows moving boolean flags between active and blocked states with advanced selection.
 */
@SuppressWarnings({"deprecation", "unused"})
public class FeatureFlagsManagerPreference extends Preference {

    // Whitelist of flags to hide from the UI.
    private static final Set<Long> WHITELIST_FLAGS = new HashSet<>();
    static {
        WHITELIST_FLAGS.add(12345678L); // Example hidden flag.
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
            Utils.showToastShort("No feature flags logged yet. Enable debugging and restart the app");
            return;
        }

        // Filter out hidden flags.
        allKnownFlags.removeAll(WHITELIST_FLAGS);

        // Include all disabled flags, even if not logged in this session.
        Set<Long> allFlags = new HashSet<>(allKnownFlags);
        allFlags.addAll(disabledFlags);

        List<Long> availableFlags = new ArrayList<>();
        List<Long> blockedFlags = new ArrayList<>();

        for (Long flag : allFlags) {
            if (disabledFlags.contains(flag)) {
                blockedFlags.add(flag);
            } else {
                availableFlags.add(flag);
            }
        }

        Collections.sort(availableFlags);
        Collections.sort(blockedFlags);

        // Use CustomDialog for styled appearance.
        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                getTitle() != null ? getTitle().toString() : "",
                null,
                null,
                str("revanced_settings_save"),
                () -> saveFlags(blockedFlags),
                () -> {},
                str("revanced_settings_reset"),
                this::resetFlags,
                true
        );

        LinearLayout mainLayout = dialogPair.second;
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );

        // Create content first.
        View contentView = createContentView(context, availableFlags, blockedFlags);
        mainLayout.addView(contentView, mainLayout.getChildCount() - 1, listViewParams);

        Dialog dialog = dialogPair.first;
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            Utils.setDialogWindowParameters(window, Gravity.CENTER, 0, 100, false);
        }
    }

    @SuppressLint("SetTextI18n")
    private View createContentView(Context context, List<Long> availableFlags, List<Long> blockedFlags) {
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(20, 20, 20, 20);

        // Headers.
        LinearLayout headersLayout = createHeaders(context, availableFlags, blockedFlags);

        // Content with columns and buttons.
        LinearLayout columnsLayout = new LinearLayout(context);
        columnsLayout.setOrientation(LinearLayout.HORIZONTAL);
        columnsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

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

        columnsLayout.addView(availableListView);
        columnsLayout.addView(buttonsLayout);
        columnsLayout.addView(blockedListView);

        contentLayout.addView(headersLayout);
        contentLayout.addView(columnsLayout);

        return contentLayout;
    }

    @SuppressLint("SetTextI18n")
    private LinearLayout createHeaders(Context context, List<Long> availableFlags, List<Long> blockedFlags) {
        LinearLayout headersLayout = new LinearLayout(context);
        headersLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView availableCountText = new TextView(context);
        availableCountText.setText("Active Flags (" + availableFlags.size() + ")");
        availableCountText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        availableCountText.setTextSize(14);
        availableCountText.setPadding(10, 0, 10, 10);

        TextView spacer = new TextView(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView blockedCountText = new TextView(context);
        blockedCountText.setText("Blocked Flags (" + blockedFlags.size() + ")");
        blockedCountText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        blockedCountText.setTextSize(14);
        blockedCountText.setPadding(10, 0, 0, 10);

        headersLayout.addView(availableCountText);
        headersLayout.addView(spacer);
        headersLayout.addView(blockedCountText);

        return headersLayout;
    }

    @SuppressLint("ClickableViewAccessibility")
    private ListView createFlagsListView(Context context, List<Long> flags, boolean isAvailable) {
        ListView listView = new ListView(context);
        listView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setPadding(0, 0, 0, 0);
        listView.setDividerHeight(0);

        // Custom adapter to control text size and no wrap.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_multiple_choice, convertFlagsToStrings(flags)) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextSize(14);
                    textView.setSingleLine(true);
                    textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    textView.setPadding(10, 0, 0, 0);
                }
                return view;
            }
        };

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
                moveSelectedFlags(availableListView, blockedListView, availableFlags, blockedFlags,
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
        button.setSingleLine(true);
        button.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Utils.dipToPixels(40), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        button.setLayoutParams(params);
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

    private void moveAllFlags(ListView fromListView, ListView toListView,
                              List<Long> fromFlags, List<Long> toFlags,
                              TextView fromCountText, TextView toCountText, boolean toBlocked) {
        moveSelectedFlags(fromListView, toListView, fromFlags, toFlags, fromCountText, toCountText, toBlocked, true);
    }

    @SuppressLint("SetTextI18n")
    private void updateListView(@Nullable ListView listView, List<Long> flags, TextView countText, boolean moveAll) {
        if (listView == null) return;

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
        adapter.clear();
        adapter.addAll(convertFlagsToStrings(flags));
        adapter.notifyDataSetChanged();

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
                flagsString.append("\n");
            }
            flagsString.append(flag);
        }

        BaseSettings.DISABLED_FEATURE_FLAGS.save(flagsString.toString());

        Utils.showToastShort("Flags saved. Restart the app to apply changes");

        Logger.printDebug(() -> "Feature flags saved. Blocked: " + blockedFlags.size());
    }

    private void resetFlags() {
        BaseSettings.DISABLED_FEATURE_FLAGS.save("");
        Utils.showToastShort("Flags reset. Restart the app to apply changes");
    }
}
