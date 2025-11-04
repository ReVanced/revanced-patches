package app.revanced.extension.youtube.videoplayer;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.settings.preference.CustomDialogListPreference.*;
import static app.revanced.extension.youtube.patches.VideoInformation.AUTOMATIC_VIDEO_QUALITY_VALUE;
import static app.revanced.extension.youtube.patches.VideoInformation.VIDEO_QUALITY_PREMIUM_NAME;
import static app.revanced.extension.youtube.videoplayer.PlayerControlButton.fadeInDuration;
import static app.revanced.extension.youtube.videoplayer.PlayerControlButton.getDialogBackgroundColor;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.ui.Dim;
import app.revanced.extension.shared.ui.SheetBottomDialog;
import app.revanced.extension.youtube.shared.PlayerType;
import com.google.android.libraries.youtube.innertube.model.media.VideoQuality;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.playback.quality.RememberVideoQualityPatch;
import app.revanced.extension.youtube.settings.Settings;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class VideoQualityDialogButton {

    @Nullable
    private static PlayerControlButton instance;

    @Nullable
    private static CharSequence currentOverlayText;

    static {
        VideoInformation.onQualityChange.addObserver((@Nullable VideoQuality quality) -> {
            updateButtonText(quality);
            return Unit.INSTANCE;
        });
    }

    /**
     * Weak reference to the currently open dialog.
     */
    private static WeakReference<SheetBottomDialog.SlideDialog> currentDialog;

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_video_quality_dialog_button_container",
                    "revanced_video_quality_dialog_button",
                    "revanced_video_quality_dialog_button_text",
                    Settings.VIDEO_QUALITY_DIALOG_BUTTON::get,
                    view -> {
                        try {
                            showVideoQualityDialog(view.getContext());
                        } catch (Exception ex) {
                            Logger.printException(() -> "Video quality button onClick failure", ex);
                        }
                    },
                    view -> {
                        try {
                            VideoQuality[] qualities = VideoInformation.getCurrentQualities();
                            if (qualities == null) {
                                Logger.printDebug(() -> "Cannot reset quality, videoQualities is null");
                                return true;
                            }

                            // Reset to default quality.
                            final int defaultResolution = RememberVideoQualityPatch.getDefaultQualityResolution();
                            for (VideoQuality quality : qualities) {
                                final int resolution = quality.patch_getResolution();
                                if (resolution != AUTOMATIC_VIDEO_QUALITY_VALUE && resolution <= defaultResolution) {
                                    Logger.printDebug(() -> "Resetting quality to: " + quality);
                                    VideoInformation.changeQuality(quality);
                                    return true;
                                }
                            }

                            // Existing hook cannot set default quality to auto.
                            // Instead show the quality dialog.
                            showVideoQualityDialog(view.getContext());
                            return true;
                        } catch (Exception ex) {
                            Logger.printException(() -> "Video quality button reset failure", ex);
                        }
                        return false;
                    }
            );

            // Set initial text.
            updateButtonText(VideoInformation.getCurrentQuality());
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * injection point.
     */
    public static void setVisibilityNegatedImmediate() {
        if (instance != null) instance.setVisibilityNegatedImmediate();
    }

    /**
     * Injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) {
            instance.setVisibilityImmediate(visible);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) {
            instance.setVisibility(visible, animated);
        }
    }

    /**
     * Updates the button text based on the current video quality.
     */
    public static void updateButtonText(@Nullable VideoQuality quality) {
        try {
            Utils.verifyOnMainThread();
            if (instance == null) return;

            final int resolution = quality == null
                    ? AUTOMATIC_VIDEO_QUALITY_VALUE // Video is still loading.
                    : quality.patch_getResolution();

            SpannableStringBuilder text = new SpannableStringBuilder();
            String qualityText = switch (resolution) {
                case AUTOMATIC_VIDEO_QUALITY_VALUE -> "";
                case 144, 240, 360 -> "LD";
                case 480  -> "SD";
                case 720  -> "HD";
                case 1080 -> "FHD";
                case 1440 -> "QHD";
                case 2160 -> "4K";
                default   -> "?"; // Should never happen.
            };
            text.append(qualityText);

            if (quality != null && quality.patch_getQualityName().contains(VIDEO_QUALITY_PREMIUM_NAME)) {
                // Underline the entire "FHD" text for 1080p Premium.
                text.setSpan(new UnderlineSpan(), 0, qualityText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            currentOverlayText = text;
            Utils.runOnMainThreadDelayed(() -> {
                if (currentOverlayText != text) {
                    Logger.printDebug(() -> "Ignoring stale button text update of: " + text);
                    return;
                }
                instance.setTextOverlay(text);
            }, 100);
        } catch (Exception ex) {
            Logger.printException(() -> "updateButtonText failure", ex);
        }
    }

    /**
     * Shows a dialog with available video qualities, excluding Auto, with a title showing the current quality.
     */
    private static void showVideoQualityDialog(Context context) {
        try {
            VideoQuality[] currentQualities = VideoInformation.getCurrentQualities();
            VideoQuality currentQuality = VideoInformation.getCurrentQuality();
            if (currentQualities == null || currentQuality == null) {
                Logger.printDebug(() -> "Cannot show qualities dialog, videoQualities is null");
                return;
            }
            if (currentQualities.length < 2) {
                // Should never happen.
                Logger.printException(() -> "Cannot show qualities dialog, no qualities available");
                return;
            }

            // -1 adjustment for automatic quality at first index.
            int listViewSelectedIndex = -1;
            for (VideoQuality quality : currentQualities) {
                if (quality.patch_getQualityName().equals(currentQuality.patch_getQualityName())) {
                    break;
                }
                listViewSelectedIndex++;
            }

            List<String> qualityLabels = new ArrayList<>(currentQualities.length - 1);
            for (VideoQuality availableQuality : currentQualities) {
                if (availableQuality.patch_getResolution() != AUTOMATIC_VIDEO_QUALITY_VALUE) {
                    qualityLabels.add(availableQuality.patch_getQualityName());
                }
            }

            // Create main layout.
            SheetBottomDialog.DraggableLinearLayout mainLayout =
                    SheetBottomDialog.createMainLayout(context, getDialogBackgroundColor());

            // Create SpannableStringBuilder for formatted text.
            SpannableStringBuilder spannableTitle = new SpannableStringBuilder();
            String titlePart = str("video_quality_quick_menu_title");
            String separatorPart = str("video_quality_title_seperator");

            // Append title part with default foreground color.
            spannableTitle.append(titlePart);
            spannableTitle.setSpan(
                    new ForegroundColorSpan(Utils.getAppForegroundColor()),
                    0,
                    titlePart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannableTitle.append("   "); // Space after title.

            // Append separator part with adjusted title color.
            int separatorStart = spannableTitle.length();
            spannableTitle.append(separatorPart);
            final int adjustedTitleForegroundColor = Utils.adjustColorBrightness(
                    Utils.getAppForegroundColor(), 1.6f, 0.6f);
            spannableTitle.setSpan(
                    new ForegroundColorSpan(adjustedTitleForegroundColor),
                    separatorStart,
                    separatorStart + separatorPart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannableTitle.append("   "); // Space after separator.

            // Append quality label with adjusted title color.
            final int qualityStart = spannableTitle.length();
            spannableTitle.append(currentQuality.patch_getQualityName());
            spannableTitle.setSpan(
                    new ForegroundColorSpan(adjustedTitleForegroundColor),
                    qualityStart,
                    qualityStart + currentQuality.patch_getQualityName().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // Add title with current quality.
            TextView titleView = new TextView(context);
            titleView.setText(spannableTitle);
            titleView.setTextSize(16);
            // Remove setTextColor since color is handled by SpannableStringBuilder.
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(Dim.dp12, Dim.dp16, 0, Dim.dp16);
            titleView.setLayoutParams(titleParams);
            mainLayout.addView(titleView);

            // Create ListView for quality selection.
            ListView listView = new ListView(context);
            CustomQualityAdapter adapter = new CustomQualityAdapter(context, qualityLabels);
            adapter.setSelectedPosition(listViewSelectedIndex);
            listView.setAdapter(adapter);
            listView.setDivider(null);

            // Create dialog.
            SheetBottomDialog.SlideDialog dialog = SheetBottomDialog.createSlideDialog(context, mainLayout, fadeInDuration);
            currentDialog = new WeakReference<>(dialog);

            listView.setOnItemClickListener((parent, view, which, id) -> {
                try {
                    final int originalIndex = which + 1; // Adjust for automatic.
                    VideoQuality selectedQuality = currentQualities[originalIndex];
                    RememberVideoQualityPatch.userChangedQuality(selectedQuality.patch_getResolution());
                    VideoInformation.changeQuality(selectedQuality);

                    dialog.dismiss();
                } catch (Exception ex) {
                    Logger.printException(() -> "Video quality selection failure", ex);
                }
            });

            mainLayout.addView(listView);

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
            Logger.printException(() -> "showVideoQualityDialog failure", ex);
        }
    }

    private static class CustomQualityAdapter extends ArrayAdapter<String> {

        private int selectedPosition = -1;

        public CustomQualityAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, 0, objects);
        }

        private void setSelectedPosition(int position) {
            this.selectedPosition = position;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        LAYOUT_REVANCED_CUSTOM_LIST_ITEM_CHECKED,
                        parent,
                        false
                );
                viewHolder = new ViewHolder();
                viewHolder.checkIcon = convertView.findViewById(ID_REVANCED_CHECK_ICON);
                viewHolder.placeholder = convertView.findViewById(ID_REVANCED_CHECK_ICON_PLACEHOLDER);
                viewHolder.textView = convertView.findViewById(ID_REVANCED_ITEM_TEXT);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textView.setText(getItem(position));
            final boolean isSelected = position == selectedPosition;
            viewHolder.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            viewHolder.placeholder.setVisibility(isSelected ? View.GONE : View.INVISIBLE);

            return convertView;
        }

        private static class ViewHolder {
            ImageView checkIcon;
            View placeholder;
            TextView textView;
        }
    }
}
