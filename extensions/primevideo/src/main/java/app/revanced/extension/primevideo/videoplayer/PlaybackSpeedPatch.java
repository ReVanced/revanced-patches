package app.revanced.extension.primevideo.videoplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;

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
            TextView speedText = new TextView(context);
            speedText.setText("⚡");
            speedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            speedText.setTextColor(Color.WHITE);
            speedText.setTypeface(null, Typeface.BOLD);
            speedText.setGravity(Gravity.CENTER);
            speedText.setTag("speed_overlay");

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics()));
            background.setColor(Color.parseColor("#80000000"));

            speedText.setBackground(background);

            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
            speedText.setPadding(padding, padding, padding, padding);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
            params.setMargins(margin, margin, margin, 0);

            speedText.setLayoutParams(params);
            speedText.setOnClickListener(v -> changePlayBackSpeed(speedText));

            int castButtonIndex = -1;
            for (int i = 0; i < buttonContainer.getChildCount(); i++) {
                View child = buttonContainer.getChildAt(i);
                if (child.getId() != View.NO_ID) {
                    try {
                        String resourceName = context.getResources().getResourceEntryName(child.getId());
                        if (resourceName != null && resourceName.equals("player_cast_btn")) {
                            castButtonIndex = i;
                            break;
                        }
                    } catch (Exception e) {
                        Logger.printException(() -> "Error finding cast button", e);
                    }
                }
            }

            if (castButtonIndex != -1) {
                buttonContainer.addView(speedText, castButtonIndex);
            } else {
                buttonContainer.addView(speedText);
            }
        } catch (Exception e) {
            Logger.printException(() -> "Error initializing speed overlay", e);
        }
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