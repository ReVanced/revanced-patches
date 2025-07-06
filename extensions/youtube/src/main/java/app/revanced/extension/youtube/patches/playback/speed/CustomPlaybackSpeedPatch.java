package app.revanced.extension.youtube.patches.playback.speed;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.function.Function;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.components.PlaybackSpeedMenuFilterPatch;
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
     * Formats speeds to UI strings.
     */
    private static final NumberFormat speedFormatter = NumberFormat.getNumberInstance();

    /**
     * Weak reference to the currently open dialog.
     */
    private static WeakReference<Dialog> currentDialog = new WeakReference<>(null);

    /**
     * Minimum and maximum custom playback speeds of {@link #customPlaybackSpeeds}.
     */
    private static final float customPlaybackSpeedsMin, customPlaybackSpeedsMax;

    static {
        // Cap at 2 decimals (rounds automatically).
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
                if (PlaybackSpeedMenuFilterPatch.isPlaybackRateSelectorMenuVisible) {
                    if (hideLithoMenuAndShowCustomSpeedMenu(recyclerView, 5)) {
                        PlaybackSpeedMenuFilterPatch.isPlaybackRateSelectorMenuVisible = false;
                    }
                }
            } catch (Exception ex) {
                Logger.printException(() -> "onFlyoutMenuCreate failure", ex);
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean hideLithoMenuAndShowCustomSpeedMenu(RecyclerView recyclerView, int expectedChildCount) {
        if (recyclerView.getChildCount() == 0) {
            return false;
        }

        View firstChild = recyclerView.getChildAt(0);
        if (!(firstChild instanceof ViewGroup playbackSpeedParentView)) {
            return false;
        }

        if (playbackSpeedParentView.getChildCount() != expectedChildCount) {
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
     * video playback. The dialog is dismissed if the player enters Picture-in-Picture (PiP) mode.
     */
    @SuppressLint("SetTextI18n")
    public static void showModernCustomPlaybackSpeedDialog(Context context) {
        // Create a dialog without a theme for custom appearance.
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove default title bar.

        // Store the dialog reference.
        currentDialog = new WeakReference<>(dialog);

        // Enable dismissing the dialog when tapping outside.
        dialog.setCanceledOnTouchOutside(true);

        // Create main vertical LinearLayout for dialog content.
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Preset size constants.
        final int dip4 = dipToPixels(4);   // Height for handle bar.
        final int dip5 = dipToPixels(5);
        final int dip6 = dipToPixels(6);   // Padding for mainLayout from bottom.
        final int dip8 = dipToPixels(8);   // Padding for mainLayout from left and right.
        final int dip20 = dipToPixels(20);
        final int dip32 = dipToPixels(32); // Height for in-rows speed buttons.
        final int dip36 = dipToPixels(36); // Height for minus and plus buttons.
        final int dip40 = dipToPixels(40); // Width for handle bar.
        final int dip60 = dipToPixels(60); // Height for speed button container.

        mainLayout.setPadding(dip5, dip8, dip5, dip8);

        // Set rounded rectangle background for the main layout.
        RoundRectShape roundRectShape = new RoundRectShape(
                Utils.createCornerRadii(12), null, null);
        ShapeDrawable background = new ShapeDrawable(roundRectShape);
        background.getPaint().setColor(Utils.getDialogBackgroundColor());
        mainLayout.setBackground(background);

        // Add handle bar at the top.
        View handleBar = new View(context);
        ShapeDrawable handleBackground = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(4), null, null));
        handleBackground.getPaint().setColor(getAdjustedBackgroundColor(true));
        handleBar.setBackground(handleBackground);
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                dip40, // handle bar width.
                dip4   // handle bar height.
        );
        handleParams.gravity = Gravity.CENTER_HORIZONTAL; // Center horizontally.
        handleParams.setMargins(0, 0, 0, dip20); // 20dp bottom margins.
        handleBar.setLayoutParams(handleParams);
        // Add handle bar view to main layout.
        mainLayout.addView(handleBar);

        // Display current playback speed.
        TextView currentSpeedText = new TextView(context);
        float currentSpeed = VideoInformation.getPlaybackSpeed();
        // Initially show with only 0 minimum digits, so 1.0 shows as 1x
        currentSpeedText.setText(formatSpeedStringX(currentSpeed, 0));
        currentSpeedText.setTextColor(Utils.getAppForegroundColor());
        currentSpeedText.setTextSize(16);
        currentSpeedText.setTypeface(Typeface.DEFAULT_BOLD);
        currentSpeedText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 0, 0, 0);
        currentSpeedText.setLayoutParams(textParams);
        // Add current speed text view to main layout.
        mainLayout.addView(currentSpeedText);

        // Create horizontal layout for slider and +/- buttons.
        LinearLayout sliderLayout = new LinearLayout(context);
        sliderLayout.setOrientation(LinearLayout.HORIZONTAL);
        sliderLayout.setGravity(Gravity.CENTER_VERTICAL);
        sliderLayout.setPadding(dip5, dip5, dip5, dip5); // 5dp padding.

        // Create minus button.
        Button minusButton = new Button(context, null, 0); // Disable default theme style.
        minusButton.setText(""); // No text on button.
        ShapeDrawable minusBackground = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(20), null, null));
        minusBackground.getPaint().setColor(getAdjustedBackgroundColor(false));
        minusButton.setBackground(minusBackground);
        OutlineSymbolDrawable minusDrawable = new OutlineSymbolDrawable(false); // Minus symbol.
        minusButton.setForeground(minusDrawable);
        LinearLayout.LayoutParams minusParams = new LinearLayout.LayoutParams(dip36, dip36);
        minusParams.setMargins(0, 0, dip5, 0); // 5dp to slider.
        minusButton.setLayoutParams(minusParams);

        // Create slider for speed adjustment.
        SeekBar speedSlider = new SeekBar(context);
        speedSlider.setMax(speedToProgressValue(customPlaybackSpeedsMax));
        speedSlider.setProgress(speedToProgressValue(currentSpeed));
        speedSlider.getProgressDrawable().setColorFilter(
                Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN); // Theme progress bar.
        speedSlider.getThumb().setColorFilter(
                Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN); // Theme slider thumb.
        LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        sliderParams.setMargins(dip5, 0, dip5, 0); // 5dp to -/+ buttons.
        speedSlider.setLayoutParams(sliderParams);

        // Create plus button.
        Button plusButton = new Button(context, null, 0); // Disable default theme style.
        plusButton.setText(""); // No text on button.
        ShapeDrawable plusBackground = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(20), null, null));
        plusBackground.getPaint().setColor(getAdjustedBackgroundColor(false));
        plusButton.setBackground(plusBackground);
        OutlineSymbolDrawable plusDrawable = new OutlineSymbolDrawable(true); // Plus symbol.
        plusButton.setForeground(plusDrawable);
        LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(dip36, dip36);
        plusParams.setMargins(dip5, 0, 0, 0); // 5dp to slider.
        plusButton.setLayoutParams(plusParams);

        // Add -/+ and slider views to slider layout.
        sliderLayout.addView(minusButton);
        sliderLayout.addView(speedSlider);
        sliderLayout.addView(plusButton);

        LinearLayout.LayoutParams sliderLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sliderLayoutParams.setMargins(0, 0, 0, dip5); // 5dp bottom margin.
        sliderLayout.setLayoutParams(sliderLayoutParams);

        // Add slider layout to main layout.
        mainLayout.addView(sliderLayout);

        Function<Float, Void> userSelectedSpeed = newSpeed -> {
            final float roundedSpeed = roundSpeedToNearestIncrement(newSpeed);
            if (VideoInformation.getPlaybackSpeed() == roundedSpeed) {
                // Nothing has changed. New speed rounds to the current speed.
                return null;
            }

            VideoInformation.overridePlaybackSpeed(roundedSpeed);
            RememberPlaybackSpeedPatch.userSelectedPlaybackSpeed(roundedSpeed);
            currentSpeedText.setText(formatSpeedStringX(roundedSpeed, 2)); // Update display.
            speedSlider.setProgress(speedToProgressValue(roundedSpeed)); // Update slider.
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
        gridParams.setMargins(0, 0, 0, 0); // No margins around GridLayout.
        gridLayout.setLayoutParams(gridParams);

        // For all buttons show at least 1 zero in decimal (2 -> "2.0").
        speedFormatter.setMinimumFractionDigits(1);

        // Add buttons for each preset playback speed.
        for (float speed : customPlaybackSpeeds) {
            // Container for button and optional label.
            FrameLayout buttonContainer = new FrameLayout(context);

            // Set layout parameters for each grid cell.
            GridLayout.LayoutParams containerParams = new GridLayout.LayoutParams();
            containerParams.width = 0; // Equal width for columns.
            containerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            containerParams.setMargins(dip5, 0, dip5, 0); // Button margins.
            containerParams.height = dip60; // Fixed height for button and label.
            buttonContainer.setLayoutParams(containerParams);

            // Create speed button.
            Button speedButton = new Button(context, null, 0);
            speedButton.setText(speedFormatter.format(speed)); // Do not use 'x' speed format.
            speedButton.setTextColor(Utils.getAppForegroundColor());
            speedButton.setTextSize(12);
            speedButton.setAllCaps(false);
            speedButton.setGravity(Gravity.CENTER);

            ShapeDrawable buttonBackground = new ShapeDrawable(new RoundRectShape(
                    Utils.createCornerRadii(20), null, null));
            buttonBackground.getPaint().setColor(getAdjustedBackgroundColor(false));
            speedButton.setBackground(buttonBackground);
            speedButton.setPadding(dip5, dip5, dip5, dip5);

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

        // Add in-rows speed buttons layout to main layout.
        mainLayout.addView(gridLayout);

        // Wrap mainLayout in another LinearLayout for side margins.
        LinearLayout wrapperLayout = new LinearLayout(context);
        wrapperLayout.setOrientation(LinearLayout.VERTICAL);
        wrapperLayout.setPadding(dip8, 0, dip8, 0); // 8dp side margins.
        wrapperLayout.addView(mainLayout);
        dialog.setContentView(wrapperLayout);

        // Configure dialog window to appear at the bottom.
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM; // Position at bottom of screen.
            params.y = dip6; // 6dp margin from bottom.
            // In landscape, use the smaller dimension (height) as portrait width.
            int portraitWidth = context.getResources().getDisplayMetrics().widthPixels;
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                portraitWidth = Math.min(
                        portraitWidth,
                        context.getResources().getDisplayMetrics().heightPixels);
            }
            params.width = portraitWidth; // Use portrait width.
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(null); // Remove default dialog background.
        }

        // Apply slide-in animation when showing the dialog.
        final int fadeDurationFast = Utils.getResourceInteger("fade_duration_fast");
        Animation slideInABottomAnimation = Utils.getResourceAnimation("slide_in_bottom");
        slideInABottomAnimation.setDuration(fadeDurationFast);
        mainLayout.startAnimation(slideInABottomAnimation);

        // Set touch listener on mainLayout to enable drag-to-dismiss.
        //noinspection ClickableViewAccessibility
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            /** Threshold for dismissing the dialog. */
            final float dismissThreshold = dipToPixels(100); // Distance to drag to dismiss.
            /** Store initial Y position of touch. */
            float touchY;
            /** Track current translation. */
            float translationY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Capture initial Y position of touch.
                        touchY = event.getRawY();
                        translationY = mainLayout.getTranslationY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // Calculate drag distance and apply translation downwards only.
                        final float deltaY = event.getRawY() - touchY;
                        // Only allow downward drag (positive deltaY).
                        if (deltaY >= 0) {
                            mainLayout.setTranslationY(translationY + deltaY);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Check if dialog should be dismissed based on drag distance.
                        if (mainLayout.getTranslationY() > dismissThreshold) {
                            // Animate dialog off-screen and dismiss.
                            //noinspection ExtractMethodRecommender
                            final float remainingDistance = context.getResources().getDisplayMetrics().heightPixels
                                    - mainLayout.getTop();
                            TranslateAnimation slideOut = new TranslateAnimation(
                                    0, 0, mainLayout.getTranslationY(), remainingDistance);
                            slideOut.setDuration(fadeDurationFast);
                            slideOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                            mainLayout.startAnimation(slideOut);
                        } else {
                            // Animate back to original position if not dragged far enough.
                            TranslateAnimation slideBack = new TranslateAnimation(
                                    0, 0, mainLayout.getTranslationY(), 0);
                            slideBack.setDuration(fadeDurationFast);
                            mainLayout.startAnimation(slideBack);
                            mainLayout.setTranslationY(0);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Create observer for PlayerType changes.
        Function1<PlayerType, Unit> playerTypeObserver = new Function1<>() {
            @Override
            public Unit invoke(PlayerType type) {
                Dialog current = currentDialog.get();
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

        dialog.show(); // Display the dialog.
    }

    /**
     * @param speed The playback speed value to format.
     * @return A string representation of the speed with 'x' (e.g. "1.25x" or "1.00x").
     */
    private static String formatSpeedStringX(float speed, int minimumFractionDigits) {
        speedFormatter.setMinimumFractionDigits(minimumFractionDigits);
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
        final int baseColor = Utils.getDialogBackgroundColor();
        float darkThemeFactor = isHandleBar ? 1.25f : 1.115f; // 1.25f for handleBar, 1.115f for others in dark theme.
        float lightThemeFactor = isHandleBar ? 0.9f : 0.95f; // 0.9f for handleBar, 0.95f for others in light theme.
        return Utils.isDarkModeEnabled()
                ? Utils.adjustColorBrightness(baseColor, darkThemeFactor)  // Lighten for dark theme.
                : Utils.adjustColorBrightness(baseColor, lightThemeFactor); // Darken for light theme.
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
