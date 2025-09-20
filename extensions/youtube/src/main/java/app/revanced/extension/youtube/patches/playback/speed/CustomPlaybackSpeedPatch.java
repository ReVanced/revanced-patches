package app.revanced.extension.youtube.patches.playback.speed;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.youtube.videoplayer.PlayerControlButton.fadeInDuration;
import static app.revanced.extension.youtube.videoplayer.PlayerControlButton.getDialogBackgroundColor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.icu.text.NumberFormat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.function.Function;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ui.SheetBottomDialog;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.components.PlaybackSpeedMenuFilter;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class CustomPlaybackSpeedPatch {

    /**
     * Maximum playback speed, inclusive.  Custom speeds must be this or less.
     * <p>
     * Going over 8x does not increase the actual playback speed any higher,
     * and the UI selector starts flickering and acting weird.
     * Over 10x and the speeds show up out of order in the UI selector.
     */
    public static final float PLAYBACK_SPEED_MAXIMUM = 8;

    /**
     * How much +/- speed adjustment buttons change the current speed.
     */
    private static final double SPEED_ADJUSTMENT_CHANGE = 0.05;

    /**
     * Scale used to convert user speed to {@link android.widget.ProgressBar#setProgress(int)}.
     */
    private static final float PROGRESS_BAR_VALUE_SCALE = 100;

    /**
     * Tap and hold speed.
     */
    private static final float TAP_AND_HOLD_SPEED;

    /**
     * Custom playback speeds.
     */
    public static final float[] customPlaybackSpeeds;

    /**
     * Minimum and maximum custom playback speeds of {@link #customPlaybackSpeeds}.
     */
    private static final float customPlaybackSpeedsMin, customPlaybackSpeedsMax;

    /**
     * The last time the old playback menu was forcefully called.
     */
    private static volatile long lastTimeOldPlaybackMenuInvoked;

    /**
     * Formats speeds to UI strings.
     */
    private static final NumberFormat speedFormatter = NumberFormat.getNumberInstance();

    /**
     * Weak reference to the currently open dialog.
     */
    private static WeakReference<SheetBottomDialog.SlideDialog> currentDialog;

    static {
        // Use same 2 digit format as built in speed picker,
        speedFormatter.setMinimumFractionDigits(2);
        speedFormatter.setMaximumFractionDigits(2);

        final float holdSpeed = Settings.SPEED_TAP_AND_HOLD.get();
        if (holdSpeed > 0 && holdSpeed <= PLAYBACK_SPEED_MAXIMUM) {
            TAP_AND_HOLD_SPEED = holdSpeed;
        } else {
            showInvalidCustomSpeedToast();
            TAP_AND_HOLD_SPEED = Settings.SPEED_TAP_AND_HOLD.resetToDefault();
        }

        customPlaybackSpeeds = loadCustomSpeeds();
        customPlaybackSpeedsMin = customPlaybackSpeeds[0];
        customPlaybackSpeedsMax = customPlaybackSpeeds[customPlaybackSpeeds.length - 1];
    }

    /**
     * Injection point.
     */
    public static float tapAndHoldSpeed() {
        return TAP_AND_HOLD_SPEED;
    }

    private static void showInvalidCustomSpeedToast() {
        Utils.showToastLong(str("revanced_custom_playback_speeds_invalid", PLAYBACK_SPEED_MAXIMUM));
    }

    private static float[] loadCustomSpeeds() {
        try {
            // Automatically replace commas with periods,
            // if the user added speeds in a localized format.
            String[] speedStrings = Settings.CUSTOM_PLAYBACK_SPEEDS.get()
                    .replace(',', '.').split("\\s+");
            Arrays.sort(speedStrings);
            if (speedStrings.length == 0) {
                throw new IllegalArgumentException();
            }

            float[] speeds = new float[speedStrings.length];

            int i = 0;
            for (String speedString : speedStrings) {
                final float speedFloat = Float.parseFloat(speedString);
                if (speedFloat <= 0 || arrayContains(speeds, speedFloat)) {
                    throw new IllegalArgumentException();
                }

                if (speedFloat > PLAYBACK_SPEED_MAXIMUM) {
                    showInvalidCustomSpeedToast();
                    Settings.CUSTOM_PLAYBACK_SPEEDS.resetToDefault();
                    return loadCustomSpeeds();
                }

                speeds[i++] = speedFloat;
            }

            return speeds;
        } catch (Exception ex) {
            Logger.printInfo(() -> "Parse error", ex);
            Utils.showToastShort(str("revanced_custom_playback_speeds_parse_exception"));
            Settings.CUSTOM_PLAYBACK_SPEEDS.resetToDefault();
            return loadCustomSpeeds();
        }
    }

    private static boolean arrayContains(float[] array, float value) {
        for (float arrayValue : array) {
            if (arrayValue == value) return true;
        }
        return false;
    }

    /**
     * Injection point.
     */
    public static void onFlyoutMenuCreate(RecyclerView recyclerView) {
        recyclerView.getViewTreeObserver().addOnDrawListener(() -> {
            try {
                if (PlaybackSpeedMenuFilter.isPlaybackRateSelectorMenuVisible) {
                    if (hideLithoMenuAndShowSpeedMenu(recyclerView, 5)) {
                        PlaybackSpeedMenuFilter.isPlaybackRateSelectorMenuVisible = false;
                    }
                }
            } catch (Exception ex) {
                Logger.printException(() -> "isPlaybackRateSelectorMenuVisible failure", ex);
            }

            try {
                if (PlaybackSpeedMenuFilter.isOldPlaybackSpeedMenuVisible) {
                    if (hideLithoMenuAndShowSpeedMenu(recyclerView, 8)) {
                        PlaybackSpeedMenuFilter.isOldPlaybackSpeedMenuVisible = false;
                    }
                }
            } catch (Exception ex) {
                Logger.printException(() -> "isOldPlaybackSpeedMenuVisible failure", ex);
            }
        });
    }

    private static boolean hideLithoMenuAndShowSpeedMenu(RecyclerView recyclerView, int expectedChildCount) {
        if (recyclerView.getChildCount() == 0) {
            return false;
        }

        if (!(recyclerView.getChildAt(0) instanceof ViewGroup playbackSpeedParentView)) {
            return false;
        }

        if (playbackSpeedParentView.getChildCount() != expectedChildCount) {
            return false;
        }

        if (!(Utils.getParentView(recyclerView, 3) instanceof ViewGroup parentView3rd)) {
            return false;
        }

        if (!(parentView3rd.getParent() instanceof ViewGroup parentView4th)) {
            return false;
        }

        // Dismiss View [R.id.touch_outside] is the 1st ChildView of the 4th ParentView.
        // This only shows in phone layout.
        var touchInsidedView = parentView4th.getChildAt(0);
        touchInsidedView.setSoundEffectsEnabled(false);
        touchInsidedView.performClick();

        // In tablet layout there is no Dismiss View, instead we just hide all two parent views.
        parentView3rd.setVisibility(View.GONE);
        parentView4th.setVisibility(View.GONE);

        // Close the litho speed menu and show the custom speeds.
        if (Settings.RESTORE_OLD_SPEED_MENU.get()) {
            showOldPlaybackSpeedMenu();
            Logger.printDebug(() -> "Old playback speed dialog shown");
        } else {
            showModernCustomPlaybackSpeedDialog(recyclerView.getContext());
            Logger.printDebug(() -> "Modern playback speed dialog shown");
        }

        return true;
    }

    public static void showOldPlaybackSpeedMenu() {
        // This method is sometimes used multiple times.
        // To prevent this, ignore method reuse within 1 second.
        final long now = System.currentTimeMillis();
        if (now - lastTimeOldPlaybackMenuInvoked < 1000) {
            Logger.printDebug(() -> "Ignoring call to showOldPlaybackSpeedMenu");
            return;
        }
        lastTimeOldPlaybackMenuInvoked = now;

        // Rest of the implementation added by patch.
    }

    /**
     * Displays a modern custom dialog for adjusting video playback speed.
     * <p>
     * This method creates a dialog with a slider, plus/minus buttons, and preset speed buttons
     * to allow the user to modify the video playback speed. The dialog is styled with rounded
     * corners and themed colors, positioned at the bottom of the screen. The playback speed
     * can be adjusted in 0.05 increments using the slider or buttons, or set directly to preset
     * values. The dialog updates the displayed speed in real-time and applies changes to the
     * video playback. The dialog is dismissed if the player enters Picture-in-Picture (PiP) mode.
     */
    @SuppressLint("SetTextI18n")
    public static void showModernCustomPlaybackSpeedDialog(Context context) {
        try {
            // Create main layout.
            SheetBottomDialog.DraggableLinearLayout mainLayout =
                    SheetBottomDialog.createMainLayout(context, getDialogBackgroundColor());

            // Preset size constants.
            final int dip4 = dipToPixels(4);
            final int dip8 = dipToPixels(8);
            final int dip12 = dipToPixels(12);
            final int dip20 = dipToPixels(20);
            final int dip32 = dipToPixels(32);
            final int dip60 = dipToPixels(60);

            // Display current playback speed.
            TextView currentSpeedText = new TextView(context);
            float currentSpeed = VideoInformation.getPlaybackSpeed();
            // Initially show with only 0 minimum digits, so 1.0 shows as 1x.
            currentSpeedText.setText(formatSpeedStringX(currentSpeed));
            currentSpeedText.setTextColor(Utils.getAppForegroundColor());
            currentSpeedText.setTextSize(16);
            currentSpeedText.setTypeface(Typeface.DEFAULT_BOLD);
            currentSpeedText.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0, dip20, 0, 0);
            currentSpeedText.setLayoutParams(textParams);
            // Add current speed text view to main layout.
            mainLayout.addView(currentSpeedText);

            // Create horizontal layout for slider and +/- buttons.
            LinearLayout sliderLayout = new LinearLayout(context);
            sliderLayout.setOrientation(LinearLayout.HORIZONTAL);
            sliderLayout.setGravity(Gravity.CENTER_VERTICAL);

            // Create +/- buttons.
            Button minusButton = createStyledButton(context, false, dip8, dip8);
            Button plusButton = createStyledButton(context, true, dip8, dip8);

            // Create slider for speed adjustment.
            SeekBar speedSlider = new SeekBar(context);
            speedSlider.setFocusable(true);
            speedSlider.setFocusableInTouchMode(true);
            speedSlider.setMax(speedToProgressValue(customPlaybackSpeedsMax));
            speedSlider.setProgress(speedToProgressValue(currentSpeed));
            speedSlider.getProgressDrawable().setColorFilter(
                    Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN); // Theme progress bar.
            speedSlider.getThumb().setColorFilter(
                    Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN); // Theme slider thumb.
            LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            speedSlider.setLayoutParams(sliderParams);

            // Add -/+ and slider views to slider layout.
            sliderLayout.addView(minusButton);
            sliderLayout.addView(speedSlider);
            sliderLayout.addView(plusButton);

            // Add slider layout to main layout.
            mainLayout.addView(sliderLayout);

            Function<Float, Void> userSelectedSpeed = newSpeed -> {
                final float roundedSpeed = roundSpeedToNearestIncrement(newSpeed);
                if (VideoInformation.getPlaybackSpeed() == roundedSpeed) {
                    // Nothing has changed. New speed rounds to the current speed.
                    return null;
                }

                currentSpeedText.setText(formatSpeedStringX(roundedSpeed)); // Update display.
                speedSlider.setProgress(speedToProgressValue(roundedSpeed)); // Update slider.

                RememberPlaybackSpeedPatch.userSelectedPlaybackSpeed(roundedSpeed);
                VideoInformation.overridePlaybackSpeed(roundedSpeed);
                return null;
            };

            // Set listener for slider to update playback speed.
            speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Convert from progress value to video playback speed.
                        userSelectedSpeed.apply(customPlaybackSpeedsMin + (progress / PROGRESS_BAR_VALUE_SCALE));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            minusButton.setOnClickListener(v -> userSelectedSpeed.apply(
                    (float) (VideoInformation.getPlaybackSpeed() - SPEED_ADJUSTMENT_CHANGE)));
            plusButton.setOnClickListener(v -> userSelectedSpeed.apply(
                    (float) (VideoInformation.getPlaybackSpeed() + SPEED_ADJUSTMENT_CHANGE)));

            // Create GridLayout for preset speed buttons.
            GridLayout gridLayout = new GridLayout(context);
            gridLayout.setColumnCount(5); // 5 columns for speed buttons.
            gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
            gridLayout.setRowCount((int) Math.ceil(customPlaybackSpeeds.length / 5.0));
            LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            gridParams.setMargins(dip4, dip12, dip4, dip12); // Speed buttons container.
            gridLayout.setLayoutParams(gridParams);

            // For button use 1 digit minimum.
            speedFormatter.setMinimumFractionDigits(1);

            // Add buttons for each preset playback speed.
            for (float speed : customPlaybackSpeeds) {
                // Container for button and optional label.
                FrameLayout buttonContainer = new FrameLayout(context);

                // Set layout parameters for each grid cell.
                GridLayout.LayoutParams containerParams = new GridLayout.LayoutParams();
                containerParams.width = 0; // Equal width for columns.
                containerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
                containerParams.setMargins(dip4, 0, dip4, 0); // Button margins.
                containerParams.height = dip60; // Fixed height for button and label.
                buttonContainer.setLayoutParams(containerParams);

                // Create speed button.
                Button speedButton = new Button(context, null, 0);
                speedButton.setText(speedFormatter.format(speed));
                speedButton.setTextColor(Utils.getAppForegroundColor());
                speedButton.setTextSize(12);
                speedButton.setAllCaps(false);
                speedButton.setGravity(Gravity.CENTER);

                ShapeDrawable buttonBackground = new ShapeDrawable(new RoundRectShape(
                        Utils.createCornerRadii(20), null, null));
                buttonBackground.getPaint().setColor(getAdjustedBackgroundColor(false));
                speedButton.setBackground(buttonBackground);
                speedButton.setPadding(dip4, dip4, dip4, dip4);

                // Center button vertically and stretch horizontally in container.
                FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, dip32, Gravity.CENTER);
                speedButton.setLayoutParams(buttonParams);

                // Add speed buttons view to buttons container layout.
                buttonContainer.addView(speedButton);

                // Add "Normal" label for 1.0x speed.
                if (speed == 1.0f) {
                    TextView normalLabel = new TextView(context);
                    // Use same 'Normal' string as stock YouTube.
                    normalLabel.setText(str("normal_playback_rate_label"));
                    normalLabel.setTextColor(Utils.getAppForegroundColor());
                    normalLabel.setTextSize(10);
                    normalLabel.setGravity(Gravity.CENTER);

                    FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                    labelParams.bottomMargin = 0; // Position label below button.
                    normalLabel.setLayoutParams(labelParams);

                    buttonContainer.addView(normalLabel);
                }

                speedButton.setOnClickListener(v -> userSelectedSpeed.apply(speed));

                gridLayout.addView(buttonContainer);
            }

            // Restore 2 digit minimum.
            speedFormatter.setMinimumFractionDigits(2);

            // Add in-rows speed buttons layout to main layout.
            mainLayout.addView(gridLayout);

            // Create dialog.
            SheetBottomDialog.SlideDialog dialog = SheetBottomDialog.createSlideDialog(context, mainLayout, fadeInDuration);
            currentDialog = new WeakReference<>(dialog);

            // Create observer for PlayerType changes.
            Function1<PlayerType, Unit> playerTypeObserver = new Function1<>() {
                @Override
                public Unit invoke(PlayerType type) {
                    SheetBottomDialog.SlideDialog current = currentDialog.get();
                    if (current == null || !current.isShowing()) {
                        // Should never happen.
                        PlayerType.getOnChange().removeObserver(this);
                        Logger.printException(() -> "Removing player type listener as dialog is null or closed");
                    } else if (type == PlayerType.WATCH_WHILE_PICTURE_IN_PICTURE) {
                        current.dismiss();
                        Logger.printDebug(() -> "Playback speed dialog dismissed due to PiP mode");
                    }
                    return Unit.INSTANCE;
                }
            };

            // Add observer to dismiss dialog when entering PiP mode.
            PlayerType.getOnChange().addObserver(playerTypeObserver);

            // Remove observer when dialog is dismissed.
            dialog.setOnDismissListener(d -> {
                PlayerType.getOnChange().removeObserver(playerTypeObserver);
                Logger.printDebug(() -> "PlayerType observer removed on dialog dismiss");
            });

            dialog.show(); // Show the dialog.

        } catch (Exception ex) {
            Logger.printException(() -> "showModernCustomPlaybackSpeedDialog failure", ex);
        }
    }

    /**
     * Creates a styled button with a plus or minus symbol.
     *
     * @param context The Android context used to create the button.
     * @param isPlus  True to display a plus symbol, false to display a minus symbol.
     * @param marginStart The start margin in pixels (left for LTR, right for RTL).
     * @param marginEnd The end margin in pixels (right for LTR, left for RTL).
     * @return A configured {@link Button} with the specified styling and layout parameters.
     */
    private static Button createStyledButton(Context context, boolean isPlus, int marginStart, int marginEnd) {
        Button button = new Button(context, null, 0); // Disable default theme style.
        button.setText(""); // No text on button.
        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(20), null, null));
        background.getPaint().setColor(getAdjustedBackgroundColor(false));
        button.setBackground(background);
        button.setForeground(new OutlineSymbolDrawable(isPlus)); // Plus or minus symbol.
        final int dip36 = dipToPixels(36);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dip36, dip36);
        params.setMargins(marginStart, 0, marginEnd, 0); // Set margins.
        button.setLayoutParams(params);
        return button;
    }

    /**
     * @param speed The playback speed value to format.
     * @return A string representation of the speed with 'x' (e.g. "1.25x" or "1.00x").
     */
    private static String formatSpeedStringX(float speed) {
        return speedFormatter.format(speed) + 'x';
    }

    /**
     * @return user speed converted to a value for {@link SeekBar#setProgress(int)}.
     */
    private static int speedToProgressValue(float speed) {
        return (int) ((speed - customPlaybackSpeedsMin) * PROGRESS_BAR_VALUE_SCALE);
    }

    /**
     * Rounds the given playback speed to the nearest 0.05 increment,
     * unless the speed exactly matches a preset custom speed.
     *
     * @param speed The playback speed to round.
     * @return The rounded speed, constrained to the specified bounds.
     */
    private static float roundSpeedToNearestIncrement(float speed) {
        // Allow speed as-is if it exactly matches a speed preset such as 1.03x.
        if (arrayContains(customPlaybackSpeeds, speed)) {
            return speed;
        }

        // Round to nearest 0.05 speed.  Must use double precision otherwise rounding error can occur.
        final double roundedSpeed = Math.round(speed / SPEED_ADJUSTMENT_CHANGE) * SPEED_ADJUSTMENT_CHANGE;
        return Utils.clamp((float) roundedSpeed, (float) SPEED_ADJUSTMENT_CHANGE, PLAYBACK_SPEED_MAXIMUM);
    }

    /**
     * Adjusts the background color based on the current theme.
     *
     * @param isHandleBar If true, applies a stronger darkening factor (0.9) for the handle bar in light theme;
     *                    if false, applies a standard darkening factor (0.95) for other elements in light theme.
     * @return A modified background color, lightened by 20% for dark themes or darkened by 5% (or 10% for handle bar)
     *         for light themes to ensure visual contrast.
     */
    public static int getAdjustedBackgroundColor(boolean isHandleBar) {
        final float darkThemeFactor = isHandleBar ? 1.25f : 1.115f; // 1.25f for handleBar, 1.115f for others in dark theme.
        final float lightThemeFactor = isHandleBar ? 0.9f : 0.95f; // 0.9f for handleBar, 0.95f for others in light theme.
        return Utils.adjustColorBrightness(getDialogBackgroundColor(), lightThemeFactor, darkThemeFactor);
    }
}

/**
 * Custom Drawable for rendering outlined plus and minus symbols on buttons.
 */
class OutlineSymbolDrawable extends Drawable {
    private final boolean isPlus; // Determines if the symbol is a plus or minus.
    private final Paint paint;

    OutlineSymbolDrawable(boolean isPlus) {
        this.isPlus = isPlus;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Enable anti-aliasing for smooth rendering.
        paint.setColor(Utils.getAppForegroundColor());
        paint.setStyle(Paint.Style.STROKE); // Use stroke style for outline.
        paint.setStrokeWidth(dipToPixels(1)); // 1dp stroke width.
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        final int width = bounds.width();
        final int height = bounds.height();
        final float centerX = width / 2f; // Center X coordinate.
        final float centerY = height / 2f; // Center Y coordinate.
        final float size = Math.min(width, height) * 0.25f; // Symbol size is 25% of button dimensions.

        // Draw horizontal line for both plus and minus symbols.
        canvas.drawLine(centerX - size, centerY, centerX + size, centerY, paint);
        if (isPlus) {
            // Draw vertical line for plus symbol.
            canvas.drawLine(centerX, centerY - size, centerX, centerY + size, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
