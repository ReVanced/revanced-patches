package app.revanced.extension.youtube.patches.playback.speed;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.playback.speed.RememberPlaybackSpeedPatch;
import app.revanced.extension.youtube.patches.components.PlaybackSpeedMenuFilterPatch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.ThemeHelper;

import java.math.RoundingMode;
import java.util.Arrays;
import java.math.BigDecimal;

@SuppressWarnings("unused")
public class CustomPlaybackSpeedPatch {

    /**
     * Maximum playback speed, exclusive value.  Custom speeds must be less than this value.
     * <p>
     * Going over 8x does not increase the actual playback speed any higher,
     * and the UI selector starts flickering and acting weird.
     * Over 10x and the speeds show up out of order in the UI selector.
     */
    public static final float PLAYBACK_SPEED_MAXIMUM = 8;

    /**
     * Minimum playback speed for the slider.
     */
    public static final float PLAYBACK_SPEED_MINIMUM = 0.25f;

    /**
     * Tap and hold speed.
     */
    private static final float TAP_AND_HOLD_SPEED;

    /**
     * Custom playback speeds.
     */
    public static float[] customPlaybackSpeeds;

    /**
     * The last time the old playback menu was forcefully called.
     */
    private static long lastTimeOldPlaybackMenuInvoked;

    static {
        final float holdSpeed = Settings.SPEED_TAP_AND_HOLD.get();

        if (holdSpeed > 0 && holdSpeed <= PLAYBACK_SPEED_MAXIMUM) {
            TAP_AND_HOLD_SPEED = holdSpeed;
        } else {
            showInvalidCustomSpeedToast();
            TAP_AND_HOLD_SPEED = Settings.SPEED_TAP_AND_HOLD.resetToDefault();
        }

        loadCustomSpeeds();
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

    private static void loadCustomSpeeds() {
        try {
            String[] speedStrings = Settings.CUSTOM_PLAYBACK_SPEEDS.get().split("\\s+");
            Arrays.sort(speedStrings);
            if (speedStrings.length == 0) {
                throw new IllegalArgumentException();
            }

            customPlaybackSpeeds = new float[speedStrings.length];

            int i = 0;
            for (String speedString : speedStrings) {
                final float speedFloat = Float.parseFloat(speedString);
                if (speedFloat <= 0 || arrayContains(customPlaybackSpeeds, speedFloat)) {
                    throw new IllegalArgumentException();
                }

                if (speedFloat >= PLAYBACK_SPEED_MAXIMUM) {
                    showInvalidCustomSpeedToast();
                    Settings.CUSTOM_PLAYBACK_SPEEDS.resetToDefault();
                    loadCustomSpeeds();
                    return;
                }

                customPlaybackSpeeds[i++] = speedFloat;
            }
        } catch (Exception ex) {
            Logger.printInfo(() -> "parse error", ex);
            Utils.showToastShort(str("revanced_custom_playback_speeds_parse_exception"));
            Settings.CUSTOM_PLAYBACK_SPEEDS.resetToDefault();
            loadCustomSpeeds();
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
                if (PlaybackSpeedMenuFilterPatch.isPlaybackRateSelectorMenuVisible) {
                    if (hideLithoMenuAndShowOldSpeedMenu(recyclerView, 5)) {
                        PlaybackSpeedMenuFilterPatch.isPlaybackRateSelectorMenuVisible = false;
                    }
                    return;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "isPlaybackRateSelectorMenuVisible failure", ex);
            }

            try {
                if (PlaybackSpeedMenuFilterPatch.isOldPlaybackSpeedMenuVisible) {
                    if (hideLithoMenuAndShowOldSpeedMenu(recyclerView, 8)) {
                        PlaybackSpeedMenuFilterPatch.isOldPlaybackSpeedMenuVisible = false;
                    }
                }
            } catch (Exception ex) {
                Logger.printException(() -> "isOldPlaybackSpeedMenuVisible failure", ex);
            }
        });
    }

    private static boolean hideLithoMenuAndShowOldSpeedMenu(RecyclerView recyclerView, int expectedChildCount) {
        if (recyclerView.getChildCount() == 0) {
            return false;
        }

        View firstChild = recyclerView.getChildAt(0);
        if (!(firstChild instanceof ViewGroup)) {
            return false;
        }

        ViewGroup PlaybackSpeedParentView = (ViewGroup) firstChild;
        if (PlaybackSpeedParentView.getChildCount() != expectedChildCount) {
            return false;
        }

        ViewParent parentView3rd = Utils.getParentView(recyclerView, 3);
        if (!(parentView3rd instanceof ViewGroup)) {
            return true;
        }

        ViewParent parentView4th = parentView3rd.getParent();
        if (!(parentView4th instanceof ViewGroup)) {
            return true;
        }

        // Dismiss View [R.id.touch_outside] is the 1st ChildView of the 4th ParentView.
        // This only shows in phone layout.
        final var touchInsidedView = ((ViewGroup) parentView4th).getChildAt(0);
        touchInsidedView.setSoundEffectsEnabled(false);
        touchInsidedView.performClick();

        // In tablet layout there is no Dismiss View, instead we just hide all two parent views.
        ((ViewGroup) parentView3rd).setVisibility(View.GONE);
        ((ViewGroup) parentView4th).setVisibility(View.GONE);

        // Close the litho speed menu and show the modern custom speed dialog.
        showModernCustomPlaybackSpeedDialog(recyclerView.getContext());
        Logger.printDebug(() -> "Modern playback speed dialog shown");

        return true;
    }

    /**
     * Displays a modern custom dialog for adjusting video playback speed.
     * <p>
     * This method creates a dialog with a slider, plus/minus buttons, and preset speed buttons
     * to allow the user to modify the video playback speed. The dialog is styled with rounded
     * corners and themed colors, positioned at the bottom of the screen. The playback speed
     * can be adjusted in 0.05 increments using the slider or buttons, or set directly to preset
     * values. The dialog updates the displayed speed in real-time and applies changes to the
     * video playback.
     *
     * @param context The context used to create and display the dialog.
     */
    @SuppressLint("SetTextI18n")
    public static void showModernCustomPlaybackSpeedDialog(Context context) {
        // Custom Drawable for rendering outlined plus and minus symbols on buttons.
        class OutlineSymbolDrawable extends Drawable {
            private final boolean isPlus; // Determines if the symbol is a plus or minus.
            private final Paint paint;

            OutlineSymbolDrawable(boolean isPlus) {
                this.isPlus = isPlus;
                paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Enable anti-aliasing for smooth rendering.
                paint.setColor(ThemeHelper.getForegroundColor());
                paint.setStyle(Paint.Style.STROKE); // Use stroke style for outline.
                paint.setStrokeWidth(dipToPixels(1)); // 1dp stroke width.
            }

            @Override
            public void draw(Canvas canvas) {
                int width = getBounds().width();
                int height = getBounds().height();
                float centerX = width / 2f; // Center X coordinate.
                float centerY = height / 2f; // Center Y coordinate.
                float size = Math.min(width, height) * 0.25f; // Symbol size is 25% of button dimensions.

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

        // Create a dialog without a theme for custom appearance.
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove default title bar.

        // Create main vertical LinearLayout for dialog content.
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dipToPixels(7), dipToPixels(20), dipToPixels(7), dipToPixels(7)); // 7dp sides, 20dp top padding.

        // Set rounded rectangle background for the main layout.
        RoundRectShape roundRectShape = new RoundRectShape(outerRadii(), null, null);
        ShapeDrawable background = new ShapeDrawable(roundRectShape);
        background.getPaint().setColor(ThemeHelper.getBackgroundColor());
        mainLayout.setBackground(background);

        // Display current playback speed.
        TextView currentSpeedText = new TextView(context);
        float currentSpeed = VideoInformation.getPlaybackSpeed(); // Get current playback speed.
        currentSpeedText.setText(formatSpeed(currentSpeed) + "x");
        currentSpeedText.setTextColor(ThemeHelper.getForegroundColor());
        currentSpeedText.setTextSize(16);
        currentSpeedText.setTypeface(Typeface.DEFAULT_BOLD);
        currentSpeedText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 0, 0, 0);
        currentSpeedText.setLayoutParams(textParams);
        mainLayout.addView(currentSpeedText);

        // Create horizontal layout for slider and +/- buttons.
        LinearLayout sliderLayout = new LinearLayout(context);
        sliderLayout.setOrientation(LinearLayout.HORIZONTAL);
        sliderLayout.setGravity(Gravity.CENTER_VERTICAL);
        sliderLayout.setPadding(dipToPixels(5), dipToPixels(5), dipToPixels(5), dipToPixels(5)); // 5dp padding.

        // Get the maximum speed from customPlaybackSpeeds array.
        float maxCustomSpeed = customPlaybackSpeeds[customPlaybackSpeeds.length - 1];

        // Create minus button.
        Button minusButton = new Button(context, null, 0); // Disable default theme style.
        minusButton.setText(""); // No text on button.
        float[] radii = buttonRadii(); // Get corner radii for buttons.
        ShapeDrawable minusBackground = new ShapeDrawable(new RoundRectShape(radii, null, null));
        minusBackground.getPaint().setColor(getAdjustedBackgroundColor());
        minusButton.setBackground(minusBackground);
        OutlineSymbolDrawable minusDrawable = new OutlineSymbolDrawable(false); // Minus symbol.
        minusButton.setForeground(minusDrawable);
        LinearLayout.LayoutParams minusParams = new LinearLayout.LayoutParams(dipToPixels(36), dipToPixels(36));
        minusParams.setMargins(dipToPixels(0), 0, dipToPixels(14), 0); // 0dp from edge, 14dp to slider.
        minusButton.setLayoutParams(minusParams);

        // Create slider for speed adjustment.
        SeekBar speedSlider = new SeekBar(context);
        speedSlider.setMax((int) ((maxCustomSpeed - PLAYBACK_SPEED_MINIMUM) * 20)); // Set max based on custom speed range.
        speedSlider.setProgress((int) ((currentSpeed - PLAYBACK_SPEED_MINIMUM) * 20)); // Set initial progress.
        speedSlider.getProgressDrawable().setColorFilter(
                ThemeHelper.getForegroundColor(), PorterDuff.Mode.SRC_IN); // Theme progress bar.
        speedSlider.getThumb().setColorFilter(
                ThemeHelper.getForegroundColor(), PorterDuff.Mode.SRC_IN); // Theme slider thumb.
        LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        speedSlider.setLayoutParams(sliderParams);

        // Create plus button.
        Button plusButton = new Button(context, null, 0); // Disable default theme style.
        plusButton.setText(""); // No text on button.
        ShapeDrawable plusBackground = new ShapeDrawable(new RoundRectShape(radii, null, null));
        plusBackground.getPaint().setColor(getAdjustedBackgroundColor());
        plusButton.setBackground(plusBackground);
        OutlineSymbolDrawable plusDrawable = new OutlineSymbolDrawable(true); // Plus symbol.
        plusButton.setForeground(plusDrawable);
        LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(dipToPixels(36), dipToPixels(36));
        plusParams.setMargins(dipToPixels(14), 0, dipToPixels(0), 0); // 14dp to slider, 0dp from edge.
        plusButton.setLayoutParams(plusParams);

        // Add views to slider layout.
        sliderLayout.addView(minusButton);
        sliderLayout.addView(speedSlider);
        sliderLayout.addView(plusButton);
        LinearLayout.LayoutParams sliderLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sliderLayoutParams.setMargins(0, 0, 0, dipToPixels(14)); // 14dp bottom margin.
        sliderLayout.setLayoutParams(sliderLayoutParams);
        mainLayout.addView(sliderLayout);

        // Set listener for slider to update playback speed.
        speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = PLAYBACK_SPEED_MINIMUM + (progress / 20f); // Calculate speed with 0.05 step.
                    speed = Math.round(speed * 20) / 20f; // Round to nearest 0.05 increment.
                    currentSpeedText.setText(formatSpeed(speed) + "x"); // Update displayed speed.
                    applyPlaybackSpeed(speed, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set listener for minus button to decrease speed.
        minusButton.setOnClickListener(v -> {
            float current = VideoInformation.getPlaybackSpeed();
            float newSpeed = Math.max(PLAYBACK_SPEED_MINIMUM, current - 0.05f); // Decrease by 0.05, respect minimum.
            if (newSpeed <= maxCustomSpeed) {
                applyPlaybackSpeed(newSpeed, false);
                currentSpeedText.setText(formatSpeed(newSpeed) + "x"); // Update display.
                speedSlider.setProgress((int) ((newSpeed - PLAYBACK_SPEED_MINIMUM) * 20)); // Update slider.
            }
        });

        // Set listener for plus button to increase speed.
        plusButton.setOnClickListener(v -> {
            float current = VideoInformation.getPlaybackSpeed();
            float newSpeed = Math.min(maxCustomSpeed, current + 0.05f); // Increase by 0.05, respect maximum.
            applyPlaybackSpeed(newSpeed, false);
            currentSpeedText.setText(formatSpeed(newSpeed) + "x"); // Update display.
            speedSlider.setProgress((int) ((newSpeed - PLAYBACK_SPEED_MINIMUM) * 20)); // Update slider.
        });

        // Create GridLayout for preset speed buttons.
        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setColumnCount(5); // 5 columns for speed buttons.
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setRowCount((int) Math.ceil(customPlaybackSpeeds.length / 5.0));
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gridParams.setMargins(0, 0, 0, 0); // No margins around GridLayout.
        gridLayout.setLayoutParams(gridParams);

        // Add buttons for each preset playback speed.
        for (float speed : customPlaybackSpeeds) {
            // Container for button and optional label.
            FrameLayout buttonContainer = new FrameLayout(context);

            // Set layout parameters for each grid cell.
            GridLayout.LayoutParams containerParams = new GridLayout.LayoutParams();
            containerParams.width = 0; // Equal width for columns.
            containerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            containerParams.setMargins(dipToPixels(5), dipToPixels(5), dipToPixels(5), dipToPixels(5)); // 5dp margins.
            containerParams.height = dipToPixels(65); // Fixed height for button and label.
            buttonContainer.setLayoutParams(containerParams);

            // Create speed button.
            Button speedButton = new Button(context);
            speedButton.setText(formatSpeed(speed));
            speedButton.setTextColor(ThemeHelper.getForegroundColor());
            speedButton.setTextSize(12);
            speedButton.setAllCaps(false);
            speedButton.setGravity(Gravity.CENTER);

            float[] btnRadii = new float[]{dipToPixels(20), dipToPixels(20), dipToPixels(20), dipToPixels(20),
                    dipToPixels(20), dipToPixels(20), dipToPixels(20), dipToPixels(20)};
            ShapeDrawable buttonBackground = new ShapeDrawable(new RoundRectShape(btnRadii, null, null));
            buttonBackground.getPaint().setColor(getAdjustedBackgroundColor());
            speedButton.setBackground(buttonBackground);
            speedButton.setPadding(dipToPixels(10), dipToPixels(8), dipToPixels(10), dipToPixels(8));

            // Center button vertically in container.
            FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, dipToPixels(32), Gravity.CENTER);
            speedButton.setLayoutParams(buttonParams);

            buttonContainer.addView(speedButton);

            // Add "Normal" label for 1.0x speed.
            if (speed == 1.0f) {
                TextView normalLabel = new TextView(context);
                normalLabel.setText(str("revanced_custom_playback_speeds_normal_speed"));
                normalLabel.setTextColor(ThemeHelper.getForegroundColor());
                normalLabel.setTextSize(10);
                normalLabel.setGravity(Gravity.CENTER);

                FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                labelParams.bottomMargin = dipToPixels(0); // Position label below button.
                normalLabel.setLayoutParams(labelParams);

                buttonContainer.addView(normalLabel);
            }

            // Set listener to apply selected speed.
            speedButton.setOnClickListener(v -> {
                applyPlaybackSpeed(speed, true);
                currentSpeedText.setText(formatSpeed(speed) + "x"); // Update display.
                speedSlider.setProgress((int) ((speed - PLAYBACK_SPEED_MINIMUM) * 20)); // Update slider.
                // dialog.dismiss(); // Optionally close dialog after selection.
            });

            gridLayout.addView(buttonContainer);
        }

        mainLayout.addView(gridLayout);

        // Wrap mainLayout in another LinearLayout for side margins.
        LinearLayout wrapperLayout = new LinearLayout(context);
        wrapperLayout.setOrientation(LinearLayout.VERTICAL);
        wrapperLayout.setPadding(dipToPixels(7), 0, dipToPixels(7), 0); // 7dp side margins.
        wrapperLayout.addView(mainLayout);
        dialog.setContentView(wrapperLayout);

        // Configure dialog window to appear at the bottom.
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM; // Position at bottom of screen.
            params.y = dipToPixels(7); // 7dp margin from bottom.
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(null); // Remove default dialog background.
        }

        dialog.show(); // Display the dialog.
    }

    /**
     * Applies the specified playback speed to the video and optionally shows a toast.
     *
     * @param speed The playback speed to apply (e.g., 1.0f for normal speed).
     * @param showToast If true, displays a toast with the new speed.
     */
    private static void applyPlaybackSpeed(float speed, boolean showToast) {
        VideoInformation.overridePlaybackSpeed(speed);
        RememberPlaybackSpeedPatch.userSelectedPlaybackSpeed(speed);
        Logger.printDebug(() -> "Applying playback speed: " + speed);
        if (showToast) {
            Utils.showToastShort(str("revanced_custom_playback_speeds_changed_toast", formatSpeed(speed)));
        }
    }

    /**
     * Defines corner radii for the plus and minus buttons.
     */
    private static float[] buttonRadii() {
        float radius = dipToPixels(20); // 20dp radius for all corners.
        return new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
    }

    /**
     * Defines corner radii for the main dialog.
     */
    private static float[] outerRadii() {
        float radius = dipToPixels(12); // 12dp corner radius.
        return new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
    }

    /**
     * Formats a playback speed value as a string with no trailing zeros.
     * <p>
     * This helper method converts a float speed value to a string, rounding to two decimal places
     * and removing trailing zeros. For whole numbers, it ensures at least one decimal place (e.g., "1.0").
     *
     * @param speed The playback speed value to format.
     * @return A string representation of the speed (e.g., "1.25" or "1.0").
     */
    private static String formatSpeed(float speed) {
        BigDecimal bd = new BigDecimal(speed).setScale(2, RoundingMode.HALF_UP);
        bd = bd.stripTrailingZeros(); // Remove trailing zeros.
        String plainString = bd.toPlainString();
        if (bd.scale() <= 0) {
            return plainString + ".0"; // Ensure at least one decimal for whole numbers.
        }
        return plainString;
    }

    /**
     * Adjusts the background color based on the current theme.
     * <p>
     * This method returns a modified background color, lightening it by 20% for dark themes
     * or darkening it by 5% for light themes, to ensure visual contrast.
     *
     * @return The adjusted background color as an integer.
     */
    public static int getAdjustedBackgroundColor() {
        final int baseColor = ThemeHelper.getBackgroundColor();
        return ThemeHelper.isDarkTheme()
                ? ThemeHelper.adjustColorBrightness(baseColor, 1.20f)  // Lighten for dark theme.
                : ThemeHelper.adjustColorBrightness(baseColor, 0.95f); // Darken for light theme.
    }
}
