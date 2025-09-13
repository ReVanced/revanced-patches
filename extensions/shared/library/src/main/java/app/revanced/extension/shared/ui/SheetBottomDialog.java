package app.revanced.extension.shared.ui;

import static app.revanced.extension.shared.Utils.dipToPixels;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.widget.ScrollView;
import android.widget.ListView;
import android.widget.Scroller;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Utils;

/**
 * A utility class for creating a bottom sheet dialog that slides up from the bottom of the screen.
 * The dialog supports drag-to-dismiss functionality, animations, and nested scrolling for scrollable content.
 */
public class SheetBottomDialog {

    /**
     * Creates a {@link SlideDialog} that slides up from the bottom of the screen with a specified content view.
     * The dialog supports drag-to-dismiss functionality, allowing the user to drag it downward to close it,
     * with proper handling of nested scrolling for scrollable content (e.g., {@link ListView}).
     * It includes side margins, a top spacer for drag interaction, and can be dismissed by touching outside.
     *
     * @param context           The context used to create the dialog.
     * @param contentView       The {@link View} to be displayed inside the dialog, such as a {@link LinearLayout}
     *                          containing a {@link ListView}, buttons, or other UI elements.
     * @param animationDuration The duration of the slide-in and slide-out animations in milliseconds.
     * @return A configured {@link SlideDialog} instance ready to be shown.
     * @throws IllegalArgumentException If contentView is null.
     */
    public static SlideDialog createSlideDialog(@NonNull Context context, @NonNull View contentView, int animationDuration) {
        SlideDialog dialog = new SlideDialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        // Create wrapper layout for side margins.
        LinearLayout wrapperLayout = new LinearLayout(context);
        wrapperLayout.setOrientation(LinearLayout.VERTICAL);

        // Create drag container.
        DraggableLinearLayout dragContainer = new DraggableLinearLayout(context, animationDuration);
        dragContainer.setOrientation(LinearLayout.VERTICAL);
        dragContainer.setDialog(dialog);

        // Add top spacer.
        View spacer = new View(context);
        final int dip40 = dipToPixels(40);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dip40);
        spacer.setLayoutParams(spacerParams);
        spacer.setClickable(true);
        dragContainer.addView(spacer);

        // Add content view.
        ViewGroup parent = (ViewGroup) contentView.getParent();
        if (parent != null) parent.removeView(contentView);
        dragContainer.addView(contentView);

        // Add drag container to wrapper layout.
        wrapperLayout.addView(dragContainer);

        dialog.setContentView(wrapperLayout);

        // Configure dialog window.
        Window window = dialog.getWindow();
        if (window != null) {
            Utils.setDialogWindowParameters(window, Gravity.BOTTOM, 0, 100, false);
        }

        // Set up animation on drag container.
        dialog.setAnimView(dragContainer);
        dialog.setAnimationDuration(animationDuration);

        return dialog;
    }

    /**
     * Creates a {@link DraggableLinearLayout} with a rounded background and a centered handle bar,
     * styled for use as the main layout in a {@link SlideDialog}. The layout has vertical orientation,
     * includes padding, and supports drag-to-dismiss functionality with proper handling of nested scrolling
     * for scrollable content (e.g., {@link ListView}) or clickable elements (e.g., buttons, {@link android.widget.SeekBar}).
     *
     * @param context         The context used to create the layout.
     * @param backgroundColor The background color for the layout as an {@link Integer}, or null to use
     *                        the default dialog background color.
     * @return A configured {@link DraggableLinearLayout} with a handle bar and styled background.
     */
    public static DraggableLinearLayout createMainLayout(@NonNull Context context, @Nullable Integer backgroundColor) {
        // Preset size constants.
        final int dip4 = dipToPixels(4);   // Handle bar height.
        final int dip8 = dipToPixels(8);   // Dialog padding.
        final int dip40 = dipToPixels(40); // Handle bar width.

        DraggableLinearLayout mainLayout = new DraggableLinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(dip8, 0, dip8, dip8);
        mainLayout.setLayoutParams(layoutParams);

        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(12), null, null));
        int color = (backgroundColor != null) ? backgroundColor : Utils.getDialogBackgroundColor();
        background.getPaint().setColor(color);
        mainLayout.setBackground(background);

        // Add handle bar.
        LinearLayout handleContainer = new LinearLayout(context);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, dip8, 0, 0);
        handleContainer.setLayoutParams(containerParams);
        handleContainer.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        View handleBar = new View(context);
        ShapeDrawable handleBackground = new ShapeDrawable(new RoundRectShape(
                Utils.createCornerRadii(4), null, null));
        handleBackground.getPaint().setColor(Utils.adjustColorBrightness(color, 0.9f, 1.25f));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dip40, dip4);
        handleBar.setLayoutParams(handleParams);
        handleBar.setBackground(handleBackground);

        handleContainer.addView(handleBar);
        mainLayout.addView(handleContainer);

        return mainLayout;
    }

    /**
     * A custom {@link LinearLayout} that provides drag-to-dismiss functionality for a {@link SlideDialog}.
     * This layout intercepts touch events to allow dragging the dialog downward to dismiss it when the
     * content cannot scroll upward. It ensures compatibility with scrollable content (e.g., {@link ListView},
     * {@link ScrollView}) and clickable elements (e.g., buttons, {@link android.widget.SeekBar}) by prioritizing
     * their touch events to prevent conflicts.
     *
     * <p>Dragging is enabled only after the dialog's slide-in animation completes. The dialog is dismissed
     * if dragged beyond 50% of its height or with a downward fling velocity exceeding 800 px/s.</p>
     */
    public static class DraggableLinearLayout extends LinearLayout {
        private static final int MIN_FLING_VELOCITY = 800; // px/s
        private static final float DISMISS_HEIGHT_FRACTION = 0.5f; // 50% of height.

        private float initialTouchRawY; // Raw Y on ACTION_DOWN.
        private float dragOffset; // Current drag translation.
        private boolean isDragging;
        private boolean isDragEnabled;

        private final int animationDuration;
        private final Scroller scroller;
        private final VelocityTracker velocityTracker;
        private final Runnable settleRunnable;

        private SlideDialog dialog;
        private float dismissThreshold;

        /**
         * Constructs a new {@link DraggableLinearLayout} with the specified context.
         */
        public DraggableLinearLayout(@NonNull Context context) {
            this(context, 0);
        }

        /**
         * Constructs a new {@link DraggableLinearLayout} with the specified context and animation duration.
         *
         * @param context      The context used to initialize the layout.
         * @param animDuration The duration of the drag animation in milliseconds.
         */
        public DraggableLinearLayout(@NonNull Context context, int animDuration) {
            super(context);
            scroller = new Scroller(context, new DecelerateInterpolator());
            velocityTracker = VelocityTracker.obtain();
            animationDuration = animDuration;
            settleRunnable = this::runSettleAnimation;

            setClickable(true);

            // Enable drag only after slide-in animation finishes.
            isDragEnabled = false;
            postDelayed(() -> isDragEnabled = true, animationDuration + 50);
        }

        /**
         * Sets the {@link SlideDialog} associated with this layout for dismissal.
         */
        public void setDialog(@NonNull SlideDialog dialog) {
            this.dialog = dialog;
        }

        /**
         * Updates the dismissal threshold when the layout's size changes.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            dismissThreshold = h * DISMISS_HEIGHT_FRACTION;
        }

        /**
         * Intercepts touch events to initiate dragging when the content cannot scroll upward and the
         * touch movement exceeds the system's touch slop.
         */
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (!isDragEnabled) return false;

            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    initialTouchRawY = ev.getRawY();
                    isDragging = false;
                    scroller.forceFinished(true);
                    removeCallbacks(settleRunnable);
                    velocityTracker.clear();
                    velocityTracker.addMovement(ev);
                    dragOffset = getTranslationY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    float dy = ev.getRawY() - initialTouchRawY;
                    if (dy > ViewConfiguration.get(getContext()).getScaledTouchSlop()
                            && !canChildScrollUp()) {
                        isDragging = true;
                        return true; // Intercept touches for drag.
                    }
                    break;
            }
            return false;
        }

        /**
         * Handles touch events to perform dragging or trigger dismissal/return animations based on
         * drag distance or fling velocity.
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!isDragEnabled) return super.onTouchEvent(ev);
            velocityTracker.addMovement(ev);

            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_MOVE:
                    if (isDragging) {
                        float deltaY = ev.getRawY() - initialTouchRawY;
                        dragOffset = Math.max(0, deltaY); // Prevent upward drag.
                        setTranslationY(dragOffset); // 1:1 following finger.
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    velocityTracker.computeCurrentVelocity(1000);
                    float velocityY = velocityTracker.getYVelocity();

                    if (dragOffset > dismissThreshold || velocityY > MIN_FLING_VELOCITY) {
                        startDismissAnimation();
                    } else {
                        startReturnAnimation();
                    }
                    isDragging = false;
                    return true;
            }
            // Consume the touch event to prevent focus changes on child views.
            return true;
        }

        /**
         * Starts an animation to dismiss the dialog by sliding it downward.
         */
        private void startDismissAnimation() {
            scroller.startScroll(0, (int) dragOffset,
                    0, getHeight() - (int) dragOffset, animationDuration);
            post(settleRunnable);
        }

        /**
         * Starts an animation to return the dialog to its original position.
         */
        private void startReturnAnimation() {
            scroller.startScroll(0, (int) dragOffset,
                    0, -(int) dragOffset, animationDuration);
            post(settleRunnable);
        }

        /**
         * Runs the settle animation, updating the layout's translation until the animation completes.
         * Dismisses the dialog if the drag offset reaches the view's height.
         */
        private void runSettleAnimation() {
            if (scroller.computeScrollOffset()) {
                dragOffset = scroller.getCurrY();
                setTranslationY(dragOffset);

                if (dragOffset >= getHeight() && dialog != null) {
                    dialog.dismiss();
                    scroller.forceFinished(true);
                } else {
                    post(settleRunnable);
                }
            } else {
                dragOffset = getTranslationY();
            }
        }

        /**
         * Checks if any child view can scroll upward, preventing drag if scrolling is possible.
         *
         * @return True if a child can scroll upward, false otherwise.
         */
        private boolean canChildScrollUp() {
            View target = findScrollableChild(this);
            return target != null && target.canScrollVertically(-1);
        }

        /**
         * Recursively searches for a scrollable child view within the given view group.
         *
         * @param group The view group to search.
         * @return The scrollable child view, or null if none found.
         */
        private View findScrollableChild(ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child.canScrollVertically(-1)) return child;
                if (child instanceof ViewGroup) {
                    View scroll = findScrollableChild((ViewGroup) child);
                    if (scroll != null) return scroll;
                }
            }
            return null;
        }
    }

    /**
     * A custom dialog that slides up from the bottom of the screen with animation. It supports
     * drag-to-dismiss functionality and ensures smooth dismissal animations without overlapping
     * dismiss calls. The dialog animates a specified view during show and dismiss operations.
     */
    public static class SlideDialog extends Dialog {
        private View animView;
        private boolean isDismissing = false;
        private int duration;
        private final int screenHeight;

        /**
         * Constructs a new {@link SlideDialog} with the specified context.
         */
        public SlideDialog(@NonNull Context context) {
            super(context);
            screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        }

        /**
         * Sets the view to animate during show and dismiss operations.
         */
        public void setAnimView(@NonNull View view) {
            this.animView = view;
        }

        /**
         * Sets the duration of the slide-in and slide-out animations.
         */
        public void setAnimationDuration(int duration) {
            this.duration = duration;
        }

        /**
         * Displays the dialog with a slide-up animation for the animated view, if set.
         */
        @Override
        public void show() {
            super.show();
            if (animView == null) return;

            animView.setTranslationY(screenHeight);
            animView.animate()
                    .translationY(0)
                    .setDuration(duration)
                    .setListener(null)
                    .start();
        }

        /**
         * Cancels the dialog, triggering a dismissal animation.
         */
        @Override
        public void cancel() {
            dismiss();
        }

        /**
         * Dismisses the dialog with a slide-down animation for the animated view, if set.
         * Ensures that dismissal is not triggered multiple times concurrently.
         */
        @Override
        public void dismiss() {
            if (isDismissing) return;
            isDismissing = true;

            Window window = getWindow();
            if (window == null) {
                super.dismiss();
                isDismissing = false;
                return;
            }

            WindowManager.LayoutParams params = window.getAttributes();
            float startDim = params != null ? params.dimAmount : 0f;

            // Animate dimming effect.
            ValueAnimator dimAnimator = ValueAnimator.ofFloat(startDim, 0f);
            dimAnimator.setDuration(duration);
            dimAnimator.addUpdateListener(animation -> {
                if (params != null) {
                    params.dimAmount = (float) animation.getAnimatedValue();
                    window.setAttributes(params);
                }
            });

            if (animView == null) {
                dimAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        SlideDialog.super.dismiss();
                        isDismissing = false;
                    }
                });
                dimAnimator.start();
                return;
            }

            dimAnimator.start();
            animView.animate()
                    .translationY(screenHeight)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            SlideDialog.super.dismiss();
                            isDismissing = false;
                        }
                    })
                    .start();
        }
    }
}
