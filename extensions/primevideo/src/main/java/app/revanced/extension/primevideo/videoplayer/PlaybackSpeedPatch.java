package app.revanced.extension.primevideo.videoplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.RectF;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import java.util.Arrays;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ui.Dim;

import com.amazon.video.sdk.player.Player;

public class PlaybackSpeedPatch {
    private static Player player;
    private static final float[] SPEED_VALUES = {0.5f, 0.7f, 0.8f, 0.9f, 0.95f, 1.0f, 1.05f, 1.1f, 1.2f, 1.3f, 1.5f, 2.0f};
    private static final String SPEED_BUTTON_TAG = "speed_overlay";

    public static void setPlayer(Player playerInstance) {
        player = playerInstance;
        if (player != null) {
            // Reset playback rate when switching between episodes to ensure correct display.
            player.setPlaybackRate(1.0f);
        }
    }

    public static void initializeSpeedOverlay(View userControlsView) {
        try {
            LinearLayout buttonContainer = Utils.getChildViewByResourceName(userControlsView, "ButtonContainerPlayerTop");

            // If the speed overlay exists we should return early.
            if (Utils.getChildView(buttonContainer, false, child ->
                    child instanceof ImageView && SPEED_BUTTON_TAG.equals(child.getTag())) != null) {
                return;
            }

            ImageView speedButton = createSpeedButton(userControlsView.getContext());
            speedButton.setOnClickListener(v -> changePlaybackSpeed(speedButton));
            buttonContainer.addView(speedButton, 0);

        } catch (IllegalArgumentException e) {
            Logger.printException(() -> "initializeSpeedOverlay, no button container found", e);
        } catch (Exception e) {
            Logger.printException(() -> "initializeSpeedOverlay failure", e);
        }
    }

    private static ImageView createSpeedButton(Context context) {
        ImageView speedButton = new ImageView(context);
        speedButton.setContentDescription("Playback Speed");
        speedButton.setTag(SPEED_BUTTON_TAG);
        speedButton.setClickable(true);
        speedButton.setFocusable(true);
        speedButton.setScaleType(ImageView.ScaleType.CENTER);

        SpeedIconDrawable speedIcon = new SpeedIconDrawable();
        speedButton.setImageDrawable(speedIcon);

        speedButton.setMinimumWidth(Dim.dp48);
        speedButton.setMinimumHeight(Dim.dp48);

        return speedButton;
    }

    private static String[] getSpeedOptions() {
        String[] options = new String[SPEED_VALUES.length];
        for (int i = 0; i < SPEED_VALUES.length; i++) {
            options[i] = SPEED_VALUES[i] + "x";
        }
        return options;
    }

    private static void changePlaybackSpeed(ImageView imageView) {
        if (player == null) {
            Logger.printException(() -> "Player not available");
            return;
        }

        try {
            player.pause();
            AlertDialog dialog = createSpeedPlaybackDialog(imageView);
            dialog.setOnDismissListener(dialogInterface -> player.play());
            dialog.show();

        } catch (Exception e) {
            Logger.printException(() -> "changePlaybackSpeed", e);
        }
    }

    private static AlertDialog createSpeedPlaybackDialog(ImageView imageView) {
        Context context = imageView.getContext();
        int currentSelection = getCurrentSpeedSelection();

        return new AlertDialog.Builder(context)
                .setTitle("Select Playback Speed")
                .setSingleChoiceItems(getSpeedOptions(), currentSelection,
                        PlaybackSpeedPatch::handleSpeedSelection)
                .create();
    }

    private static int getCurrentSpeedSelection() {
        try {
            float currentRate = player.getPlaybackRate();
            int index = Arrays.binarySearch(SPEED_VALUES, currentRate);
            return Math.max(index, 0); // Use slowest speed if not found.
        } catch (Exception e) {
            Logger.printException(() -> "getCurrentSpeedSelection error getting current playback speed", e);
            return 0;
        }
    }

    private static void handleSpeedSelection(android.content.DialogInterface dialog, int selectedIndex) {
        try {
            float selectedSpeed = SPEED_VALUES[selectedIndex];
            player.setPlaybackRate(selectedSpeed);
            player.play();
        } catch (Exception e) {
            Logger.printException(() -> "handleSpeedSelection error setting playback speed", e);
        } finally {
            dialog.dismiss();
        }
    }
}

class SpeedIconDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    public void draw(Canvas canvas) {
        int w = getBounds().width();
        int h = getBounds().height();
        float centerX = w / 2f;
        // Position gauge in lower portion.
        float centerY = h * 0.7f;
        float radius = Math.min(w, h) / 2f * 0.8f;

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius * 0.1f);

        // Draw semicircle.
        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(oval, 180, 180, false, paint);

        // Draw three tick marks.
        paint.setStrokeWidth(radius * 0.06f);
        for (int i = 0; i < 3; i++) {
            float angle = 180 + (i * 45); // 180°, 225°, 270°.
            float angleRad = (float) Math.toRadians(angle);

            float startX = centerX + (radius * 0.8f) * (float) Math.cos(angleRad);
            float startY = centerY + (radius * 0.8f) * (float) Math.sin(angleRad);
            float endX = centerX + radius * (float) Math.cos(angleRad);
            float endY = centerY + radius * (float) Math.sin(angleRad);

            canvas.drawLine(startX, startY, endX, endY, paint);
        }

        // Draw needle.
        paint.setStrokeWidth(radius * 0.08f);
        float needleAngle = 200; // Slightly right of center.
        float needleAngleRad = (float) Math.toRadians(needleAngle);

        float needleEndX = centerX + (radius * 0.6f) * (float) Math.cos(needleAngleRad);
        float needleEndY = centerY + (radius * 0.6f) * (float) Math.sin(needleAngleRad);

        canvas.drawLine(centerX, centerY, needleEndX, needleEndY, paint);

        // Center dot.
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius * 0.06f, paint);
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

    @Override
    public int getIntrinsicWidth() {
        return Dim.dp32;
    }

    @Override
    public int getIntrinsicHeight() {
        return Dim.dp32;
    }
}
