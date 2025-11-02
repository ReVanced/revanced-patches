package app.revanced.extension.youtube.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.patches.EnableDebuggingPatch;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.shared.ui.CustomDialog;

/**
 * A custom preference that opens a dialog for managing feature flags.
 * Allows moving boolean flags between active and blocked states with advanced selection.
 */
@SuppressWarnings({"deprecation", "unused"})
public class FeatureFlagsManagerPreference extends Preference {

    public static final int DRAWABLE_REVANCED_SETTINGS_SELECT_ALL =
            getResourceIdentifierOrThrow("revanced_settings_select_all", "drawable");
    public static final int DRAWABLE_REVANCED_SETTINGS_DESELECT_ALL =
            getResourceIdentifierOrThrow("revanced_settings_deselect_all", "drawable");
    public static final int DRAWABLE_REVANCED_SETTINGS_COPY_ALL =
            getResourceIdentifierOrThrow("revanced_settings_copy_all", "drawable");
    public static final int DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_ONE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_right_one", "drawable");
    public static final int DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_DOUBLE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_right_double", "drawable");
    public static final int DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_ONE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_left_one", "drawable");
    public static final int DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_DOUBLE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_left_double", "drawable");

    static final int dip4 = Utils.dipToPixels(4);
    static final int dip24 = Utils.dipToPixels(24);
    static final int dip36 = Utils.dipToPixels(36);
    static final int dip44 = Utils.dipToPixels(44);

    /**
     * Flags to hide from the UI.
     */
    private static final Set<Long> HIDDEN_FLAGS = Set.of(
            45386834L // Blocks settings button.
    );

    /**
     * Tracks state for range selection in ListView.
     */
    private static class ListViewSelectionState {
        int lastClickedPosition = -1; // Position of the last clicked item.
        boolean isRangeSelecting = false; // True while a range is being selected.
    }

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

    /**
     * Shows the main dialog for managing feature flags.
     */
    private void showFlagsManagerDialog() {
        if (!BaseSettings.DEBUG.get()) {
            Utils.showToastShort(str("revanced_debug_logs_disabled"));
            return;
        }

        Context context = getContext();

        // Load all known and disabled flags.
        TreeSet<Long> allKnownFlags = new TreeSet<>(EnableDebuggingPatch.getAllLoggedFlags());
        allKnownFlags.removeAll(HIDDEN_FLAGS);

        TreeSet<Long> disabledFlags = new TreeSet<>(EnableDebuggingPatch.parseFlags(BaseSettings.DISABLED_FEATURE_FLAGS.get()));
        disabledFlags.removeAll(HIDDEN_FLAGS);

        if (allKnownFlags.isEmpty() && disabledFlags.isEmpty()) {
            Utils.showToastShort("No feature flags logged yet");
            return;
        }

        TreeSet<Long> availableFlags = new TreeSet<>(allKnownFlags);
        availableFlags.removeAll(disabledFlags);
        TreeSet<Long> blockedFlags = new TreeSet<>(disabledFlags);

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
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);

        // Insert content before the dialog button row.
        View contentView = createContentView(context, availableFlags, blockedFlags);
        mainLayout.addView(contentView, mainLayout.getChildCount() - 1, contentParams);

        Dialog dialog = dialogPair.first;
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            Utils.setDialogWindowParameters(window, Gravity.CENTER, 0, 100, false);
        }
    }

    /**
     * Creates the main content view with two columns.
     */
    private View createContentView(Context context, TreeSet<Long> availableFlags, TreeSet<Long> blockedFlags) {
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        // Header count TextViews.
        TextView availableCountText = new TextView(context);
        availableCountText.setTag("revanced_debug_feature_flags_manager_active_header");
        availableCountText.setTextSize(14);
        availableCountText.setGravity(Gravity.CENTER);

        TextView blockedCountText = new TextView(context);
        blockedCountText.setTag("revanced_debug_feature_flags_manager_blocked_header");
        blockedCountText.setTextSize(14);
        blockedCountText.setGravity(Gravity.CENTER);

        // Create ListViews and Adapters.
        Pair<ListView, FlagAdapter> availablePair = createListView(context, availableFlags, availableCountText);
        ListView availableListView = availablePair.first;
        FlagAdapter availableAdapter = availablePair.second;

        Pair<ListView, FlagAdapter> blockedPair = createListView(context, blockedFlags, blockedCountText);
        ListView blockedListView = blockedPair.first;
        FlagAdapter blockedAdapter = blockedPair.second;

        // Update initial counts.
        updateHeaderCount(availableCountText, availableFlags.size());
        updateHeaderCount(blockedCountText, blockedFlags.size());

        // Headers.
        LinearLayout headersLayout = new LinearLayout(context);
        headersLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        availableCountText.setLayoutParams(headerParams);
        blockedCountText.setLayoutParams(headerParams);

        TextView spacer = new TextView(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                dip44, LinearLayout.LayoutParams.WRAP_CONTENT));

        headersLayout.addView(availableCountText);
        headersLayout.addView(spacer);
        headersLayout.addView(blockedCountText);

        // Left column: Active Flags.
        LinearLayout leftWrapper = new LinearLayout(context);
        leftWrapper.setOrientation(LinearLayout.VERTICAL);
        leftWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        EditText searchAvailable = createSearchBox(context, availableAdapter, availableListView, availableCountText);
        leftWrapper.addView(searchAvailable);

        LinearLayout buttonsRowAvailable = createActionButtons(context, availableListView, availableAdapter);
        leftWrapper.addView(buttonsRowAvailable);

        availableListView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        leftWrapper.addView(availableListView);

        // Right column: Blocked Flags.
        LinearLayout rightWrapper = new LinearLayout(context);
        rightWrapper.setOrientation(LinearLayout.VERTICAL);
        rightWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        EditText searchBlocked = createSearchBox(context, blockedAdapter, blockedListView, blockedCountText);
        rightWrapper.addView(searchBlocked);

        LinearLayout buttonsRowBlocked = createActionButtons(context, blockedListView, blockedAdapter);
        rightWrapper.addView(buttonsRowBlocked);

        blockedListView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        rightWrapper.addView(blockedListView);

        // Move buttons: > >> < <<
        LinearLayout moveButtonsLayout = createMoveButtons(
                context, availableListView, blockedListView,
                availableFlags, blockedFlags,
                availableCountText, blockedCountText
        );

        // Main columns layout.
        LinearLayout columnsLayout = new LinearLayout(context);
        columnsLayout.setOrientation(LinearLayout.HORIZONTAL);
        columnsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        columnsLayout.addView(leftWrapper);
        columnsLayout.addView(moveButtonsLayout);
        columnsLayout.addView(rightWrapper);

        contentLayout.addView(headersLayout);
        contentLayout.addView(columnsLayout);

        return contentLayout;
    }

    /**
     * Updates the header text with the current count.
     */
    private void updateHeaderCount(TextView header, int count) {
        header.setText(str((String) header.getTag(), count));
    }

    /**
     * Creates a search box that filters the list.
     */
    private EditText createSearchBox(Context context, FlagAdapter adapter, ListView listView, TextView countText) {
        EditText search = new EditText(context);
        search.setInputType(InputType.TYPE_CLASS_NUMBER);
        search.setTextSize(14);
        search.setHint(str("revanced_debug_feature_flags_manager_search_hint"));
        search.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s.toString());
                listView.clearChoices();
                updateHeaderCount(countText, adapter.getCount());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return search;
    }

    /**
     * Creates action buttons.
     */
    private LinearLayout createActionButtons(Context context, ListView listView, FlagAdapter adapter) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageButton selectAll = createButton(context, DRAWABLE_REVANCED_SETTINGS_SELECT_ALL,
                () -> {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        listView.setItemChecked(i, true);
                    }
                });

        ImageButton clearAll = createButton(context, DRAWABLE_REVANCED_SETTINGS_DESELECT_ALL,
                () -> {
                    listView.clearChoices();
                    adapter.notifyDataSetChanged();
                });

        ImageButton copy = createButton(context, DRAWABLE_REVANCED_SETTINGS_COPY_ALL,
                () -> {
                    List<String> items = new ArrayList<>();
                    SparseBooleanArray checked = listView.getCheckedItemPositions();

                    if (checked.size() > 0) {
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (checked.get(i)) {
                                items.add(adapter.getItem(i));
                            }
                        }
                    } else {
                        for (Long flag : adapter.getFullFlags()) {
                            items.add(String.valueOf(flag));
                        }
                    }

                    Utils.setClipboard(TextUtils.join("\n", items));

                    Utils.showToastShort(str("revanced_debug_feature_flags_manager_toast_copied"));
                });

        row.addView(selectAll);
        row.addView(clearAll);
        row.addView(copy);

        return row;
    }

    /**
     * Creates the central move buttons.
     */
    private LinearLayout createMoveButtons(Context context,
                                           ListView availableListView, ListView blockedListView,
                                           TreeSet<Long> availableFlags, TreeSet<Long> blockedFlags,
                                           TextView availableCountText, TextView blockedCountText) {
        LinearLayout buttonsLayout = new LinearLayout(context);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        buttonsLayout.setGravity(Gravity.CENTER);

        ImageButton moveOneRight = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_ONE,
                () -> moveFlags(availableListView, blockedListView, availableFlags, blockedFlags,
                        availableCountText, blockedCountText, false));

        ImageButton moveAllRight = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_DOUBLE,
                () -> moveFlags(availableListView, blockedListView, availableFlags, blockedFlags,
                        availableCountText, blockedCountText, true));

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(0, dip24));

        ImageButton moveOneLeft = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_ONE,
                () -> moveFlags(blockedListView, availableListView, blockedFlags, availableFlags,
                        blockedCountText, availableCountText, false));

        ImageButton moveAllLeft = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_DOUBLE,
                () -> moveFlags(blockedListView, availableListView, blockedFlags, availableFlags,
                        blockedCountText, availableCountText, true));

        buttonsLayout.addView(moveOneRight);
        buttonsLayout.addView(moveAllRight);
        buttonsLayout.addView(space);
        buttonsLayout.addView(moveOneLeft);
        buttonsLayout.addView(moveAllLeft);

        return buttonsLayout;
    }

    /**
     * Creates a styled ImageButton.
     */
    @SuppressLint("ResourceType")
    private ImageButton createButton(Context context, int drawableResId, Runnable action) {
        ImageButton button = new ImageButton(context);

        button.setImageResource(drawableResId);
        button.setScaleType(ImageView.ScaleType.CENTER);
        int[] attrs = {android.R.attr.selectableItemBackgroundBorderless};
        TypedArray ripple = context.obtainStyledAttributes(attrs);
        button.setBackgroundDrawable(ripple.getDrawable(0));
        ripple.recycle();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dip36, dip36);
        params.setMargins(dip4, dip4, dip4, dip4);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> action.run());

        return button;
    }

    /**
     * Custom adapter with search filtering.
     */
    private static class FlagAdapter extends ArrayAdapter<String> {
        private final TreeSet<Long> fullFlags;
        private final List<Long> filteredFlags = new ArrayList<>();
        private String searchQuery = "";

        public FlagAdapter(Context context, TreeSet<Long> fullFlags) {
            super(context, android.R.layout.simple_list_item_multiple_choice, new ArrayList<>());
            this.fullFlags = fullFlags;
            updateFiltered();
        }

        public void setSearchQuery(String query) {
            searchQuery = query == null ? "" : query.trim();
            updateFiltered();
        }

        private void updateFiltered() {
            clear();
            filteredFlags.clear();
            for (Long flag : fullFlags) {
                String flagString = String.valueOf(flag);
                if (searchQuery.isEmpty() || flagString.contains(searchQuery)) {
                    add(flagString);
                    filteredFlags.add(flag);
                }
            }
            notifyDataSetChanged();
        }

        public void refresh() {
            updateFiltered();
        }

        public List<Long> getFullFlags() {
            return new ArrayList<>(fullFlags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = view.findViewById(android.R.id.text1);
            if (textView != null) {
                textView.setTextSize(14);
                textView.setPadding(dip4, 0, 0, 0);
            }

            return view;
        }
    }

    /**
     * Creates a ListView with filtering, multi-select, and range selection.
     */
    @SuppressLint("ClickableViewAccessibility")
    private Pair<ListView, FlagAdapter> createListView(Context context,
                                                       TreeSet<Long> flags, TextView countText) {
        ListView listView = new ListView(context);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setDividerHeight(0);

        FlagAdapter adapter = new FlagAdapter(context, flags);
        listView.setAdapter(adapter);

        final ListViewSelectionState state = new ListViewSelectionState();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (!state.isRangeSelecting) {
                state.lastClickedPosition = position;
            } else {
                state.isRangeSelecting = false;
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (state.lastClickedPosition == -1) {
                listView.setItemChecked(position, true);
                state.lastClickedPosition = position;
            } else {
                int start = Math.min(state.lastClickedPosition, position);
                int end = Math.max(state.lastClickedPosition, position);
                for (int i = start; i <= end; i++) {
                    listView.setItemChecked(i, true);
                }
                state.isRangeSelecting = true;
            }
            return true;
        });

        listView.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && state.isRangeSelecting) {
                state.isRangeSelecting = false;
            }
            return false;
        });

        return new Pair<>(listView, adapter);
    }

    /**
     * Moves selected or all flags from one list to another.
     *
     * @param fromListView  Source ListView.
     * @param toListView    Destination ListView.
     * @param fromFlags     Source flag set.
     * @param toFlags       Destination flag set.
     * @param fromCountText Header showing count of source items.
     * @param toCountText   Header showing count of destination items.
     * @param moveAll       If true, move all items; if false, move only selected.
     */
    private void moveFlags(ListView fromListView, ListView toListView,
                           TreeSet<Long> fromFlags, TreeSet<Long> toFlags,
                           TextView fromCountText, TextView toCountText,
                           boolean moveAll) {
        if (fromListView == null || toListView == null) return;

        List<Long> flagsToMove = new ArrayList<>();
        FlagAdapter fromAdapter = (FlagAdapter) fromListView.getAdapter();

        if (moveAll) {
            flagsToMove.addAll(fromFlags);
        } else {
            SparseBooleanArray checked = fromListView.getCheckedItemPositions();
            for (int i = 0; i < fromAdapter.getCount(); i++) {
                if (checked.get(i)) {
                    String item = fromAdapter.getItem(i);
                    if (item != null) {
                        flagsToMove.add(Long.parseLong(item));
                    }
                }
            }
        }

        if (flagsToMove.isEmpty()) return;

        fromFlags.removeAll(flagsToMove);
        toFlags.addAll(flagsToMove);

        // Clear selections before refreshing.
        fromListView.clearChoices();
        toListView.clearChoices();

        // Refresh both adapters.
        fromAdapter.refresh();
        ((FlagAdapter) toListView.getAdapter()).refresh();

        // Update headers.
        updateHeaderCount(fromCountText, fromFlags.size());
        updateHeaderCount(toCountText, toFlags.size());
    }

    /**
     * Saves blocked flags to settings.
     */
    private void saveFlags(TreeSet<Long> blockedFlags) {
        StringBuilder flagsString = new StringBuilder();
        for (Long flag : blockedFlags) {
            if (flagsString.length() > 0) {
                flagsString.append("\n");
            }
            flagsString.append(flag);
        }

        BaseSettings.DISABLED_FEATURE_FLAGS.save(flagsString.toString());
        Utils.showToastShort(str("revanced_debug_feature_flags_manager_toast_saved"));
        Logger.printDebug(() -> "Feature flags saved. Blocked: " + blockedFlags.size());

        AbstractPreferenceFragment.showRestartDialog(getContext());
    }

    /**
     * Resets all blocked flags.
     */
    private void resetFlags() {
        BaseSettings.DISABLED_FEATURE_FLAGS.save("");
        Utils.showToastShort(str("revanced_debug_feature_flags_manager_toast_reset"));

        AbstractPreferenceFragment.showRestartDialog(getContext());
    }
}
