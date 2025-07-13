package app.revanced.extension.primevideo.videoplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.RectF;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;

import app.revanced.extension.shared.Logger;

import com.amazon.video.sdk.player.Player;

public class PlaybackSpeedPatch {
    private static Player player;


    public static void setPlayer(Player playerInstance) {
        player = playerInstance;
    }

    public static void initializeTextOverlay(View userControlsView) {
        try {
            LinearLayout buttonContainer = findTopButtonContainer(userControlsView);
            if (buttonContainer == null) {
                return;
            }

            for (int i = 0; i < buttonContainer.getChildCount(); i++) {
                View child = buttonContainer.getChildAt(i);
                if (child instanceof TextView && child.getTag() != null && "speed_overlay".equals(child.getTag())) {
                    return;
                }
            }

            Context context = userControlsView.getContext();
            TextView speedButton = new TextView(context);
            speedButton.setTag("speed_overlay");
            speedButton.setText("");
            speedButton.setGravity(Gravity.CENTER_VERTICAL);

            speedButton.setTextColor(Color.WHITE);
            speedButton.setClickable(true);
            speedButton.setFocusable(true);

            SpeedIconDrawable speedIcon = new SpeedIconDrawable();
            int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
                    context.getResources().getDisplayMetrics());
            speedIcon.setBounds(0, 0, iconSize, iconSize);
            speedButton.setCompoundDrawables(speedIcon, null, null, null);

            int buttonSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    context.getResources().getDisplayMetrics());
            speedButton.setMinimumWidth(buttonSize);
            speedButton.setMinimumHeight(buttonSize);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 4, 0);
            speedButton.setLayoutParams(params);
            speedButton.setOnClickListener(v -> changePlayBackSpeed(speedButton));
            int castButtonIndex = findCastButtonIndex(buttonContainer, context);
            if (castButtonIndex != -1) {
                buttonContainer.addView(speedButton, castButtonIndex);
            } else {
                buttonContainer.addView(speedButton);
            }

        } catch (Exception e) {
            Logger.printException(() -> "Error initializing speed overlay", e);
        }
    }

    private static int findCastButtonIndex(LinearLayout buttonContainer, Context context) {
        for (int i = 0; i < buttonContainer.getChildCount(); i++) {
            View child = buttonContainer.getChildAt(i);
            if (child.getId() != View.NO_ID) {
                try {
                    String resourceName = context.getResources().getResourceEntryName(child.getId());
                    if (resourceName != null && resourceName.equals("player_cast_btn")) {
                        return i;
                    }
                } catch (Exception e) {
                    // Continue searching
                }
            }
        }
        return -1;
    }

    private static void changePlayBackSpeed(TextView speedText) {
        try {
            if (player != null) {
                player.pause();

                AlertDialog dialog = speedPlaybackDialog(speedText);
                dialog.setOnDismissListener(dialogInterface -> {
                    try {
                        if (player != null) {
                            player.play();
                        }
                    } catch (Exception e) {
                        Logger.printException(() -> "Error resuming playback", e);
                    }
                });

                dialog.show();
            } else {
                Logger.printDebug(() -> "Player not available");
            }
        } catch (Exception e) {
            Logger.printException(() -> "Error in changePlayBackSpeed", e);
        }
    }

    private static AlertDialog speedPlaybackDialog(TextView speedText) {
        Context context = speedText.getContext();
        String[] speedOptions = {"1.0x", "1.5x", "2.0x"};
        float[] speedValues = {1.0f, 1.5f, 2.0f};

        int currentSelection = 0;
        if (player != null) {
            try {
                float currentRate = player.getPlaybackRate();
                for (int i = 0; i < speedValues.length; i++) {
                    if (Math.abs(currentRate - speedValues[i]) < 0.1f) {
                        currentSelection = i;
                        break;
                    }
                }
            } catch (Exception e) {
                Logger.printException(() -> "Error getting current playback rate", e);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Playback Speed");
        builder.setSingleChoiceItems(speedOptions, currentSelection, (dialog, which) -> {
            try {
                if (player != null) {
                    float speed = speedValues[which];
                    player.setPlaybackRate(speed);
                    player.play();
                }
            } catch (Exception e) {
                Logger.printException(() -> "Error setting playback speed", e);
            }
            dialog.dismiss();
        });

        return builder.create();
    }

    private static LinearLayout findTopButtonContainer(View userControlsView) {
        try {
            if (userControlsView instanceof ViewGroup viewGroup) {
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);

                    if (child instanceof LinearLayout && child.getId() != View.NO_ID) {
                        try {
                            String resourceName = userControlsView.getContext().getResources().getResourceEntryName(child.getId());
                            if (resourceName != null && resourceName.equals("ButtonContainerPlayerTop")) {
                                return (LinearLayout) child;
                            }
                        } catch (Exception e) {
                            Logger.printException(() -> "Error finding button container", e);
                        }
                    }

                    LinearLayout result = findTopButtonContainer(child);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            Logger.printException(() -> "Error finding button container", e);
        }
        return null;
    }
}

class SpeedIconDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    public void draw(Canvas canvas) {
        int w = getBounds().width();
        int h = getBounds().height();
        float centerX = w / 2f;
        float centerY = h * 0.7f; // Position gauge in lower portion
        float radius = Math.min(w, h) / 2f * 0.8f;

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius * 0.1f);

        // Draw semicircle
        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(oval, 180, 180, false, paint);

        // Draw three tick marks
        paint.setStrokeWidth(radius * 0.06f);
        for (int i = 0; i < 3; i++) {
            float angle = 180 + (i * 45); // 180°, 225°, 270°
            float angleRad = (float) Math.toRadians(angle);

            float startX = centerX + (radius * 0.8f) * (float) Math.cos(angleRad);
            float startY = centerY + (radius * 0.8f) * (float) Math.sin(angleRad);
            float endX = centerX + radius * (float) Math.cos(angleRad);
            float endY = centerY + radius * (float) Math.sin(angleRad);

            canvas.drawLine(startX, startY, endX, endY, paint);
        }

        // Draw needle
        paint.setStrokeWidth(radius * 0.08f);
        float needleAngle = 200; // Slightly right of center
        float needleAngleRad = (float) Math.toRadians(needleAngle);

        float needleEndX = centerX + (radius * 0.6f) * (float) Math.cos(needleAngleRad);
        float needleEndY = centerY + (radius * 0.6f) * (float) Math.sin(needleAngleRad);

        canvas.drawLine(centerX, centerY, needleEndX, needleEndY, paint);

        // Center dot
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
}