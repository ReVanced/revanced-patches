package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
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
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.shared.ui.Dim;

/**
 * A custom preference that opens a dialog for managing feature flags.
 * Allows moving boolean flags between active and blocked states with advanced selection.
 */
@SuppressWarnings({"deprecation", "unused"})
public class FeatureFlagsManagerPreference extends Preference {

    private static final int DRAWABLE_REVANCED_SETTINGS_SELECT_ALL =
            getResourceIdentifierOrThrow("revanced_settings_select_all", "drawable");
    private static final int DRAWABLE_REVANCED_SETTINGS_DESELECT_ALL =
            getResourceIdentifierOrThrow("revanced_settings_deselect_all", "drawable");
    private static final int DRAWABLE_REVANCED_SETTINGS_COPY_ALL =
            getResourceIdentifierOrThrow("revanced_settings_copy_all", "drawable");
    private static final int DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_ONE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_right_one", "drawable");
    private static final int DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_DOUBLE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_right_double", "drawable");
    private static final int DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_ONE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_left_one", "drawable");
    private static final int DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_DOUBLE =
            getResourceIdentifierOrThrow("revanced_settings_arrow_left_double", "drawable");

    /**
     * Flags to hide from the UI.
     */
    private static final Set<Long> FLAGS_TO_IGNORE = Set.of(
            45386834L // 'You' tab settings icon.
    );

    /**
     * Tracks state for range selection in ListView.
     */
    private static class ListViewSelectionState {
        int lastClickedPosition = -1; // Position of the last clicked item.
        boolean isRangeSelecting = false; // True while a range is being selected.
    }

    /**
     * Helper class to pass ListView and Adapter together.
     */
    private record ColumnViews(ListView listView, FlagAdapter adapter) {}

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
        allKnownFlags.removeAll(FLAGS_TO_IGNORE);

        TreeSet<Long> disabledFlags = new TreeSet<>(EnableDebuggingPatch.parseFlags(
                BaseSettings.DISABLED_FEATURE_FLAGS.get()));
        disabledFlags.removeAll(FLAGS_TO_IGNORE);

        if (allKnownFlags.isEmpty() && disabledFlags.isEmpty()) {
            // String does not need to be localized because it's basically impossible
            // to reach the settings menu without encountering at least 1 flag.
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

        // Headers.
        TextView availableHeader = createHeader(context, "revanced_debug_feature_flags_manager_active_header");
        TextView blockedHeader = createHeader(context, "revanced_debug_feature_flags_manager_blocked_header");

        LinearLayout headersLayout = new LinearLayout(context);
        headersLayout.setOrientation(LinearLayout.HORIZONTAL);
        headersLayout.addView(availableHeader, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        headersLayout.addView(blockedHeader, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Columns.
        View leftColumn = createColumn(context, availableFlags, availableHeader);
        View rightColumn = createColumn(context, blockedFlags, blockedHeader);

        ColumnViews leftViews = (ColumnViews) leftColumn.getTag();
        ColumnViews rightViews = (ColumnViews) rightColumn.getTag();

        updateHeaderCount(availableHeader, leftViews.adapter);
        updateHeaderCount(blockedHeader, rightViews.adapter);

        // Main columns layout.
        LinearLayout columnsLayout = new LinearLayout(context);
        columnsLayout.setOrientation(LinearLayout.HORIZONTAL);
        columnsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        columnsLayout.addView(leftColumn, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        Space spaceBetweenColumns = new Space(context);
        spaceBetweenColumns.setLayoutParams(new LinearLayout.LayoutParams(Dim.dp8, ViewGroup.LayoutParams.MATCH_PARENT));
        columnsLayout.addView(spaceBetweenColumns);

        columnsLayout.addView(rightColumn, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        // Move buttons below columns.
        Pair<LinearLayout, LinearLayout> moveButtons = createMoveButtons(context,
                leftViews.listView, rightViews.listView,
                availableFlags, blockedFlags, availableHeader, blockedHeader);

        // Layout for buttons row.
        LinearLayout buttonsRow = new LinearLayout(context);
        buttonsRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonsRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        buttonsRow.addView(moveButtons.first, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Space spaceBetweenButtons = new Space(context);
        spaceBetweenButtons.setLayoutParams(new LinearLayout.LayoutParams(Dim.dp8, ViewGroup.LayoutParams.WRAP_CONTENT));
        buttonsRow.addView(spaceBetweenButtons);

        buttonsRow.addView(moveButtons.second, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        contentLayout.addView(headersLayout);
        contentLayout.addView(columnsLayout);
        contentLayout.addView(buttonsRow);

        return contentLayout;
    }

    /**
     * Creates a header TextView.
     */
    private TextView createHeader(Context context, String tag) {
        TextView textview = new TextView(context);
        textview.setTag(tag);
        textview.setTextSize(16);
        textview.setTextColor(Utils.getAppForegroundColor());
        textview.setGravity(Gravity.CENTER);

        return textview;
    }

    /**
     * Creates a single column (search + buttons + list).
     */
    private View createColumn(Context context, TreeSet<Long> flags, TextView countText) {
        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);

        Pair<ListView, FlagAdapter> pair = createListView(context, flags, countText);
        ListView listView = pair.first;
        FlagAdapter adapter = pair.second;

        EditText search = createSearchBox(context, adapter, listView, countText);
        LinearLayout buttons = createActionButtons(context, listView, adapter);

        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                Dim.roundedCorners(10), null, null));
        background.getPaint().setColor(Utils.getEditTextBackground());
        listView.setPadding(0, Dim.dp4, 0, Dim.dp4);
        listView.setBackground(background);
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        wrapper.addView(search);
        wrapper.addView(buttons);
        wrapper.addView(listView);

        // Save references for move buttons.
        wrapper.setTag(new ColumnViews(listView, adapter));

        return wrapper;
    }

    /**
     * Updates the header text with the current count.
     */
    private void updateHeaderCount(TextView header, FlagAdapter adapter) {
        header.setText(str((String) header.getTag(), adapter.getCount()));
    }

    /**
     * Creates a search box that filters the list.
     */
    @SuppressLint("ClickableViewAccessibility")
    private EditText createSearchBox(Context context, FlagAdapter adapter, ListView listView, TextView countText) {
        EditText search = new EditText(context);
        search.setInputType(InputType.TYPE_CLASS_NUMBER);
        search.setTextSize(16);
        search.setHint(str("revanced_debug_feature_flags_manager_search_hint"));
        search.setHapticFeedbackEnabled(false);
        search.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s.toString());
                listView.clearChoices();
                updateHeaderCount(countText, adapter);
                Drawable clearIcon = context.getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
                clearIcon.setBounds(0, 0, Dim.dp20, Dim.dp20);
                search.setCompoundDrawables(null, null, TextUtils.isEmpty(s) ? null : clearIcon, null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        search.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable[] compoundDrawables = search.getCompoundDrawables();
                if (compoundDrawables[2] != null &&
                        event.getRawX() >= (search.getRight() - compoundDrawables[2].getBounds().width())) {
                    search.setText("");
                    return true;
                }
            }
            return false;
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
                    for (int i = 0, count = adapter.getCount(); i < count; i++) {
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
                        for (int i = 0, count = adapter.getCount(); i < count; i++) {
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
     * Creates the move buttons (left and right groups).
     */
    private Pair<LinearLayout, LinearLayout> createMoveButtons(Context context,
                                                               ListView availableListView, ListView blockedListView,
                                                               TreeSet<Long> availableFlags, TreeSet<Long> blockedFlags,
                                                               TextView availableCountText, TextView blockedCountText) {
        // Left group: >> >
        LinearLayout leftButtons = new LinearLayout(context);
        leftButtons.setOrientation(LinearLayout.HORIZONTAL);
        leftButtons.setGravity(Gravity.CENTER);

        ImageButton moveAllRight = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_DOUBLE,
                () -> moveFlags(availableListView, blockedListView, availableFlags, blockedFlags,
                        availableCountText, blockedCountText, true));

        ImageButton moveOneRight = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_RIGHT_ONE,
                () -> moveFlags(availableListView, blockedListView, availableFlags, blockedFlags,
                        availableCountText, blockedCountText, false));

        leftButtons.addView(moveAllRight);
        leftButtons.addView(moveOneRight);

        // Right group: < <<
        LinearLayout rightButtons = new LinearLayout(context);
        rightButtons.setOrientation(LinearLayout.HORIZONTAL);
        rightButtons.setGravity(Gravity.CENTER);

        ImageButton moveOneLeft = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_ONE,
                () -> moveFlags(blockedListView, availableListView, blockedFlags, availableFlags,
                        blockedCountText, availableCountText, false));

        ImageButton moveAllLeft = createButton(context, DRAWABLE_REVANCED_SETTINGS_ARROW_LEFT_DOUBLE,
                () -> moveFlags(blockedListView, availableListView, blockedFlags, availableFlags,
                        blockedCountText, availableCountText, true));

        rightButtons.addView(moveOneLeft);
        rightButtons.addView(moveAllLeft);

        return new Pair<>(leftButtons, rightButtons);
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
        //noinspection Recycle
        TypedArray ripple = context.obtainStyledAttributes(attrs);
        button.setBackgroundDrawable(ripple.getDrawable(0));
        ripple.close();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Dim.dp32, Dim.dp32);
        params.setMargins(Dim.dp8, Dim.dp8, Dim.dp8, Dim.dp8);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> action.run());

        return button;
    }

    /**
     * Custom adapter with search filtering.
     */
    private static class FlagAdapter extends ArrayAdapter<String> {
        private final TreeSet<Long> fullFlags;
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
            for (Long flag : fullFlags) {
                String flagString = String.valueOf(flag);
                if (searchQuery.isEmpty() || flagString.contains(searchQuery)) {
                    add(flagString);
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
            for (int i = 0, count = fromAdapter.getCount(); i < count; i++) {
                if (checked.get(i)) {
                    String item = fromAdapter.getItem(i);
                    if (item != null) {
                        flagsToMove.add(Long.parseLong(item));
                    }
                }
            }
        }

        if (flagsToMove.isEmpty()) return;

        for (Long flag : flagsToMove) {
            fromFlags.remove(flag);
            toFlags.add(flag);
        }

        // Clear selections before refreshing.
        fromListView.clearChoices();
        toListView.clearChoices();

        // Refresh both adapters.
        fromAdapter.refresh();
        ((FlagAdapter) toListView.getAdapter()).refresh();

        // Update headers.
        updateHeaderCount(fromCountText, fromAdapter);
        updateHeaderCount(toCountText, (FlagAdapter) toListView.getAdapter());
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
