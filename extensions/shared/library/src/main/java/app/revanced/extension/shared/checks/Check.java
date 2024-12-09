package app.revanced.extension.shared.checks;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.DialogFragmentOnStartAction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.widget.Button;

import androidx.annotation.Nullable;

import java.util.Collection;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

abstract class Check {
    private static final int NUMBER_OF_TIMES_TO_IGNORE_WARNING_BEFORE_DISABLING = 2;

    private static final int SECONDS_BEFORE_SHOWING_IGNORE_BUTTON = 15;
    private static final int SECONDS_BEFORE_SHOWING_WEBSITE_BUTTON = 10;

    private static final Uri GOOD_SOURCE = Uri.parse("https://revanced.app");

    /**
     * @return If the check conclusively passed or failed. A null value indicates it neither passed nor failed.
     */
    @Nullable
    protected abstract Boolean check();

    protected abstract String failureReason();

    /**
     * Specifies a sorting order for displaying the checks that failed.
     * A lower value indicates to show first before other checks.
     */
    public abstract int uiSortingValue();

    /**
     * For debugging and development only.
     * Forces all checks to be performed and the check failed dialog to be shown.
     * Can be enabled by importing settings text with {@link BaseSettings#CHECK_ENVIRONMENT_WARNINGS_ISSUED}
     * set to -1.
     */
    static boolean debugAlwaysShowWarning() {
        final boolean alwaysShowWarning = BaseSettings.CHECK_ENVIRONMENT_WARNINGS_ISSUED.get() < 0;
        if (alwaysShowWarning) {
            Logger.printInfo(() -> "Debug forcing environment check warning to show");
        }

        return alwaysShowWarning;
    }

    static boolean shouldRun() {
        return BaseSettings.CHECK_ENVIRONMENT_WARNINGS_ISSUED.get()
                < NUMBER_OF_TIMES_TO_IGNORE_WARNING_BEFORE_DISABLING;
    }

    static void disableForever() {
        Logger.printInfo(() -> "Environment checks disabled forever");

        BaseSettings.CHECK_ENVIRONMENT_WARNINGS_ISSUED.save(Integer.MAX_VALUE);
    }

    @SuppressLint("NewApi")
    static void issueWarning(Activity activity, Collection<Check> failedChecks) {
        final var reasons = new StringBuilder();

        reasons.append("<ul>");
        for (var check : failedChecks) {
            // Add a non breaking space to fix bullet points spacing issue.
            reasons.append("<li>&nbsp;").append(check.failureReason());
        }
        reasons.append("</ul>");

        var message = Html.fromHtml(
                str("revanced_check_environment_failed_message", reasons.toString()),
                FROM_HTML_MODE_COMPACT
        );

        Utils.runOnMainThreadDelayed(() -> {
            AlertDialog alert = new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(str("revanced_check_environment_failed_title"))
                    .setMessage(message)
                    .setPositiveButton(
                            " ",
                            (dialog, which) -> {
                                final var intent = new Intent(Intent.ACTION_VIEW, GOOD_SOURCE);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);

                                // Shutdown to prevent the user from navigating back to this app,
                                // which is no longer showing a warning dialog.
                                activity.finishAffinity();
                                System.exit(0);
                            }
                    ).setNegativeButton(
                            " ",
                            (dialog, which) -> {
                                // Cleanup data if the user incorrectly imported a huge negative number.
                                final int current = Math.max(0, BaseSettings.CHECK_ENVIRONMENT_WARNINGS_ISSUED.get());
                                BaseSettings.CHECK_ENVIRONMENT_WARNINGS_ISSUED.save(current + 1);

                                dialog.dismiss();
                            }
                    ).create();

            Utils.showDialog(activity, alert, false, new DialogFragmentOnStartAction() {
                boolean hasRun;
                @Override
                public void onStart(AlertDialog dialog) {
                    // Only run this once, otherwise if the user changes to a different app
                    // then changes back, this handler will run again and disable the buttons.
                    if (hasRun) {
                        return;
                    }
                    hasRun = true;

                    var openWebsiteButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    openWebsiteButton.setEnabled(false);

                    var dismissButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    dismissButton.setEnabled(false);

                    getCountdownRunnable(dismissButton, openWebsiteButton).run();
                }
            });
        }, 1000); // Use a delay, so this dialog is shown on top of any other startup dialogs.
    }

    private static Runnable getCountdownRunnable(Button dismissButton, Button openWebsiteButton) {
        return new Runnable() {
            private int secondsRemaining = SECONDS_BEFORE_SHOWING_IGNORE_BUTTON;

            @Override
            public void run() {
                Utils.verifyOnMainThread();

                if (secondsRemaining > 0) {
                    if (secondsRemaining - SECONDS_BEFORE_SHOWING_WEBSITE_BUTTON == 0) {
                        openWebsiteButton.setText(str("revanced_check_environment_dialog_open_official_source_button"));
                        openWebsiteButton.setEnabled(true);
                    }

                    secondsRemaining--;

                    Utils.runOnMainThreadDelayed(this, 1000);
                } else {
                    dismissButton.setText(str("revanced_check_environment_dialog_ignore_button"));
                    dismissButton.setEnabled(true);
                }
            }
        };
    }
}
