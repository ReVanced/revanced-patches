package app.revanced.extension.youtube.patches.playback.speed;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import app.revanced.extension.youtube.patches.components.PlaybackSpeedMenuFilterPatch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.ThemeHelper;

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

        // Close the litho speed menu and show the old style flyout menu or modern speed dialog.
        if (Settings.CUSTOM_PLAYBACK_SPEED_MENU_TYPE.get()) {
            // Open modern speed dialog
            showModernCustomPlaybackSpeedDialog(recyclerView.getContext());
            Logger.printDebug(() -> "Modern playback speed dialog shown");
        } else {
            // Open old style flyout menu
            showOldPlaybackSpeedMenu();
            Logger.printDebug(() -> "Old playback speed menu shown");
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
        Logger.printDebug(() -> "Old video quality menu shown");
    }

    @SuppressLint("DefaultLocale")
    public static void showModernCustomPlaybackSpeedDialog(Context context) {
        // Custom Drawable for outlined plus and minus symbols
        class OutlineSymbolDrawable extends Drawable {
            private final boolean isPlus;
            private final Paint paint;

            OutlineSymbolDrawable(boolean isPlus) {
                this.isPlus = isPlus;
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(ThemeHelper.getForegroundColor());
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dipToPixels(1)); // 1dp stroke width.
            }

            @Override
            public void draw(Canvas canvas) {
                int width = getBounds().width();
                int height = getBounds().height();
                float centerX = width / 2f;
                float centerY = height / 2f;
                float size = Math.min(width, height) * 0.2f; // Symbol size 20% of button.

                // Draw horizontal line for both plus and minus.
                canvas.drawLine(centerX - size, centerY, centerX + size, centerY, paint);
                if (isPlus) {
                    // Draw vertical line for plus.
                    canvas.drawLine(centerX, centerY - size, centerX, centerY + size, paint);
                }
            }

            @Override
            public void setAlpha(int alpha) {
                paint.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(android.graphics.ColorFilter colorFilter) {
                paint.setColorFilter(colorFilter);
            }

            @Override
            public int getOpacity() {
                return android.graphics.PixelFormat.TRANSLUCENT;
            }
        }

        // Create a dialog without a theme.
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create main layout (vertical LinearLayout).
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dipToPixels(7), dipToPixels(20), dipToPixels(7), dipToPixels(7)); // 7dp sides, 20dp top

        // Set rounded rectangle background for the main layout.
        float[] outerRadii = new float[]{dipToPixels(16), dipToPixels(16), dipToPixels(16), dipToPixels(16),
                dipToPixels(16), dipToPixels(16), dipToPixels(16), dipToPixels(16)}; // 16dp corner radius
        RoundRectShape roundRectShape = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable background = new ShapeDrawable(roundRectShape);
        background.getPaint().setColor(ThemeHelper.getBackgroundColor());
        mainLayout.setBackground(background);

        // Current speed display.
        TextView currentSpeedText = new TextView(context);
        float currentSpeed = VideoInformation.getPlaybackSpeed();
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

        // Create layout for slider and +/- buttons.
        LinearLayout sliderLayout = new LinearLayout(context);
        sliderLayout.setOrientation(LinearLayout.HORIZONTAL);
        sliderLayout.setGravity(Gravity.CENTER_VERTICAL);
        sliderLayout.setPadding(dipToPixels(5), dipToPixels(5), dipToPixels(5), dipToPixels(5)); // 0dp side padding

        // Get the maximum speed from customPlaybackSpeeds.
        float maxCustomSpeed = customPlaybackSpeeds[customPlaybackSpeeds.length - 1];

        // Minus button.
        Button minusButton = new Button(context, null, 0); // Disable default theme style
        minusButton.setText(""); // No text
        float[] radii = buttonRadii();
        ShapeDrawable minusBackground = new ShapeDrawable(new RoundRectShape(radii, null, null));
        minusBackground.getPaint().setColor(getAdjustedBackgroundColor());
        minusButton.setBackground(minusBackground); // Use setBackground instead of setForeground for background.
        OutlineSymbolDrawable minusDrawable = new OutlineSymbolDrawable(false);
        minusButton.setForeground(minusDrawable);
        LinearLayout.LayoutParams minusParams = new LinearLayout.LayoutParams(dipToPixels(36), dipToPixels(36));
        minusParams.setMargins(dipToPixels(0), 0, dipToPixels(14), 0); // 0dp from edge, 14dp to slider.
        minusButton.setLayoutParams(minusParams);

        // Slider.
        SeekBar speedSlider = new SeekBar(context);
        speedSlider.setMax((int) ((maxCustomSpeed - PLAYBACK_SPEED_MINIMUM) * 20)); // Use max custom speed
        speedSlider.setProgress((int) ((currentSpeed - PLAYBACK_SPEED_MINIMUM) * 20));
        speedSlider.getProgressDrawable().setColorFilter(
                ThemeHelper.getForegroundColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        speedSlider.getThumb().setColorFilter(
                ThemeHelper.getForegroundColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        speedSlider.setLayoutParams(sliderParams);

        // Plus button.
        Button plusButton = new Button(context, null, 0); // Disable default theme style
        plusButton.setText(""); // No text
        ShapeDrawable plusBackground = new ShapeDrawable(new RoundRectShape(radii, null, null));
        plusBackground.getPaint().setColor(getAdjustedBackgroundColor());
        plusButton.setBackground(plusBackground);
        OutlineSymbolDrawable plusDrawable = new OutlineSymbolDrawable(true);
        plusButton.setForeground(plusDrawable);
        LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(dipToPixels(36), dipToPixels(36)); // Increased size
        plusParams.setMargins(dipToPixels(14), 0, dipToPixels(0), 0); // 14dp to slider, 0dp from edge
        plusButton.setLayoutParams(plusParams);

        // Add views to slider layout.
        sliderLayout.addView(minusButton);
        sliderLayout.addView(speedSlider);
        sliderLayout.addView(plusButton);
        LinearLayout.LayoutParams sliderLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sliderLayoutParams.setMargins(0, 0, 0, dipToPixels(14)); // 14dp margin below.
        sliderLayout.setLayoutParams(sliderLayoutParams);
        mainLayout.addView(sliderLayout);

        // Slider listener.
        speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = PLAYBACK_SPEED_MINIMUM + (progress / 20f); // Step size 0.05.
                    speed = Math.round(speed * 20) / 20f; // Ensure exact 0.05 increments.
                    currentSpeedText.setText(formatSpeed(speed) + "x");
                    applyPlaybackSpeed(speed, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Minus button listener.
        minusButton.setOnClickListener(v -> {
            float current = VideoInformation.getPlaybackSpeed();
            float newSpeed = Math.max(PLAYBACK_SPEED_MINIMUM, current - 0.05f);
            if (newSpeed <= maxCustomSpeed) {
                applyPlaybackSpeed(newSpeed, false);
                currentSpeedText.setText(formatSpeed(newSpeed) + "x");
                speedSlider.setProgress((int) ((newSpeed - PLAYBACK_SPEED_MINIMUM) * 20));
            }
        });

        // Plus button listener.
        plusButton.setOnClickListener(v -> {
            float current = VideoInformation.getPlaybackSpeed();
            float newSpeed = Math.min(maxCustomSpeed, current + 0.05f);
            applyPlaybackSpeed(newSpeed, false);
            currentSpeedText.setText(formatSpeed(newSpeed) + "x");
            speedSlider.setProgress((int) ((newSpeed - PLAYBACK_SPEED_MINIMUM) * 20));
        });

        // Create GridLayout for speed buttons.
        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setColumnCount(5);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setRowCount((int) Math.ceil(customPlaybackSpeeds.length / 5.0));
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        gridParams.setMargins(0, 0, 0, 0); // No extra margins around GridLayout.
        gridLayout.setLayoutParams(gridParams);

        // Add buttons for each custom playback speed.
        for (float speed : customPlaybackSpeeds) {
            FrameLayout buttonContainer = new FrameLayout(context);

            // Set layout parameters for each cell in GridLayout.
            GridLayout.LayoutParams containerParams = new GridLayout.LayoutParams();
            containerParams.width = 0;
            containerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            containerParams.setMargins(dipToPixels(5), dipToPixels(5), dipToPixels(5), dipToPixels(5));
            containerParams.height = dipToPixels(65); // Increased height to accommodate button and label.
            buttonContainer.setLayoutParams(containerParams);

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

            // Fixed size for button, centered vertically in the FrameLayout.
            FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, dipToPixels(32), Gravity.CENTER);
            speedButton.setLayoutParams(buttonParams);

            buttonContainer.addView(speedButton);

            if (speed == 1.0f) {
                TextView normalLabel = new TextView(context);
                normalLabel.setText("Normal");
                normalLabel.setTextColor(ThemeHelper.getForegroundColor());
                normalLabel.setTextSize(10);
                normalLabel.setGravity(Gravity.CENTER);

                FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                labelParams.bottomMargin = dipToPixels(0); // Adjusted margin to position label below button.
                normalLabel.setLayoutParams(labelParams);

                buttonContainer.addView(normalLabel);
            }

            speedButton.setOnClickListener(v -> {
                applyPlaybackSpeed(speed, true);
                currentSpeedText.setText(formatSpeed(speed) + "x");
                speedSlider.setProgress((int) ((speed - PLAYBACK_SPEED_MINIMUM) * 20));
                // dialog.dismiss(); // Close dialog after pressing speedButton.
            });

            gridLayout.addView(buttonContainer);
        }

        mainLayout.addView(gridLayout);

        // Wrap mainLayout in another LinearLayout to apply side margins.
        LinearLayout wrapperLayout = new LinearLayout(context);
        wrapperLayout.setOrientation(LinearLayout.VERTICAL);
        wrapperLayout.setPadding(dipToPixels(7), 0, dipToPixels(7), 0); // 7dp side margins.
        wrapperLayout.addView(mainLayout);
        dialog.setContentView(wrapperLayout);

        // Configure dialog window attributes for bottom positioning.
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.y = dipToPixels(7); // 7dp margin from bottom.
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(null); // No default dialog background.
        }

        dialog.show();
    }

    @SuppressLint("DefaultLocale")
    private static void applyPlaybackSpeed(float speed, boolean showToast) {
        VideoInformation.overridePlaybackSpeed(speed);
        Logger.printDebug(() -> "Applying playback speed: " + speed);
        if (showToast) {
            Utils.showToastShort(str("revanced_custom_playback_speeds_changed_toast", formatSpeed(speed)));
        }
    }

    private static float[] buttonRadii() {
        float radius = dipToPixels(20); // 20dp radius for all corners
        return new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
    }

    // Helper method to format speed with comma and no trailing zeros.
    @SuppressLint("DefaultLocale")
    private static String formatSpeed(float speed) {
        BigDecimal bd = new BigDecimal(speed).setScale(2, BigDecimal.ROUND_HALF_UP);
        bd = bd.stripTrailingZeros();
        if (bd.scale() <= 0) {
            return bd.toPlainString() + ".0";
        }
        return bd.toPlainString();
    }

    public static int getAdjustedBackgroundColor() {
        final int baseColor = ThemeHelper.getBackgroundColor();
        return ThemeHelper.isDarkTheme()
                ? ThemeHelper.adjustColorBrightness(baseColor, 1.20f)  // Lighten for dark theme.
                : ThemeHelper.adjustColorBrightness(baseColor, 0.95f); // Darken for light theme.
    }
}
