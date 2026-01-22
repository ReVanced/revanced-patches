package app.revanced.extension.nothingx.patches;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patches to expose the K1 token for Nothing X app to enable pairing with GadgetBridge.
 */
@SuppressWarnings("unused")
public class ShowK1TokensPatch {

    private static final String TAG = "ReVanced";
    private static final String PACKAGE_NAME = "com.nothing.smartcenter";
    private static final String EMPTY_MD5 = "d41d8cd98f00b204e9800998ecf8427e";
    private static final String PREFS_NAME = "revanced_nothingx_prefs";
    private static final String KEY_DONT_SHOW_DIALOG = "dont_show_k1_dialog";

    // Colors
    private static final int COLOR_BG = 0xFF1E1E1E;
    private static final int COLOR_CARD = 0xFF2D2D2D;
    private static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int COLOR_TEXT_SECONDARY = 0xFFB0B0B0;
    private static final int COLOR_ACCENT = 0xFFFF9500;
    private static final int COLOR_TOKEN_BG = 0xFF3A3A3A;
    private static final int COLOR_BUTTON_POSITIVE = 0xFFFF9500;
    private static final int COLOR_BUTTON_NEGATIVE = 0xFFFF6B6B;

    // Match standalone K1: k1:, K1:, k1>, etc.
    private static final Pattern K1_STANDALONE_PATTERN = Pattern.compile("(?i)(?:k1\\s*[:>]\\s*)([0-9a-f]{32})");
    // Match combined r3+k1: format (64 chars = r3(32) + k1(32))
    private static final Pattern K1_COMBINED_PATTERN = Pattern.compile("(?i)r3\\+k1\\s*:\\s*([0-9a-f]{64})");

    private static volatile boolean k1Logged = false;
    private static volatile boolean lifecycleCallbacksRegistered = false;
    private static Context appContext;

    /**
     * Get K1 tokens from database and log files.
     * Call this after the app initializes.
     *
     * @param context Application context
     */
    public static void showK1Tokens(Context context) {
        if (k1Logged) {
            return;
        }

        appContext = context.getApplicationContext();

        Set<String> allTokens = new LinkedHashSet<>();

        // First try to get from database.
        String dbToken = getK1TokensFromDatabase();
        if (dbToken != null) {
            allTokens.add(dbToken);
        }

        // Then get from log files.
        Set<String> logTokens = getK1TokensFromLogFiles();
        allTokens.addAll(logTokens);

        if (allTokens.isEmpty()) {
            return;
        }

        // Log all found tokens.
        int index = 1;
        for (String token : allTokens) {
            Log.i(TAG, "#" + index++ + ": " + token.toUpperCase());
        }

        // Register lifecycle callbacks to show dialog when an Activity is ready.
        registerLifecycleCallbacks(allTokens);

        k1Logged = true;
    }

    /**
     * Register ActivityLifecycleCallbacks to show dialog when first Activity resumes.
     *
     * @param tokens Set of K1 tokens to display
     */
    private static void registerLifecycleCallbacks(Set<String> tokens) {
        if (lifecycleCallbacksRegistered || !(appContext instanceof Application)) {
            return;
        }

        Application application = (Application) appContext;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // Check if user chose not to show dialog.
                SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                if (prefs.getBoolean(KEY_DONT_SHOW_DIALOG, false)) {
                    application.unregisterActivityLifecycleCallbacks(this);
                    lifecycleCallbacksRegistered = false;
                    return;
                }

                // Show dialog on first Activity resume.
                if (tokens != null && !tokens.isEmpty()) {
                    activity.runOnUiThread(() -> showK1TokensDialog(activity, tokens));
                    // Unregister after showing
                    application.unregisterActivityLifecycleCallbacks(this);
                    lifecycleCallbacksRegistered = false;
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });

        lifecycleCallbacksRegistered = true;
    }

    /**
     * Show dialog with K1 tokens.
     *
     * @param activity Activity context
     * @param tokens   Set of K1 tokens
     */
    private static void showK1TokensDialog(Activity activity, Set<String> tokens) {
        try {
            // Create main container.
            LinearLayout mainLayout = new LinearLayout(activity);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setBackgroundColor(COLOR_BG);
            mainLayout.setPadding(dpToPx(activity, 24), dpToPx(activity, 16),
                                   dpToPx(activity, 24), dpToPx(activity, 16));

            // Title.
            TextView titleView = new TextView(activity);
            titleView.setText("K1 Token(s) Found");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextColor(COLOR_TEXT_PRIMARY);
            titleView.setGravity(Gravity.CENTER);
            mainLayout.addView(titleView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Subtitle.
            TextView subtitleView = new TextView(activity);
            subtitleView.setText(tokens.size() == 1 ? "1 token found • Tap to copy" : tokens.size() + " tokens found • Tap to copy");
            subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            subtitleView.setTextColor(COLOR_TEXT_SECONDARY);
            subtitleView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            subtitleParams.topMargin = dpToPx(activity, 4);
            subtitleParams.bottomMargin = dpToPx(activity, 16);
            mainLayout.addView(subtitleView, subtitleParams);

            // Scrollable content.
            ScrollView scrollView = new ScrollView(activity);
            scrollView.setVerticalScrollBarEnabled(false);
            LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            );
            scrollParams.topMargin = dpToPx(activity, 8);
            scrollParams.bottomMargin = dpToPx(activity, 16);
            mainLayout.addView(scrollView, scrollParams);

            LinearLayout tokensContainer = new LinearLayout(activity);
            tokensContainer.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(tokensContainer);

            // Add each token as a card.
            boolean singleToken = tokens.size() == 1;
            int index = 1;
            for (String token : tokens) {
                LinearLayout tokenCard = createTokenCard(activity, token, index++, singleToken);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.bottomMargin = dpToPx(activity, 12);
                tokensContainer.addView(tokenCard, cardParams);
            }

            // Info text.
            TextView infoView = new TextView(activity);
            infoView.setText(tokens.size() == 1 ? "Tap the token to copy it" : "Tap any token to copy it");
            infoView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            infoView.setTextColor(COLOR_TEXT_SECONDARY);
            infoView.setGravity(Gravity.CENTER);
            infoView.setAlpha(0.7f);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            infoParams.topMargin = dpToPx(activity, 8);
            mainLayout.addView(infoView, infoParams);

            // Button row.
            LinearLayout buttonRow = new LinearLayout(activity);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
            buttonRow.setGravity(Gravity.END);
            LinearLayout.LayoutParams buttonRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonRowParams.topMargin = dpToPx(activity, 16);
            mainLayout.addView(buttonRow, buttonRowParams);

            // "Don't show again" button.
            Button dontShowButton = new Button(activity);
            dontShowButton.setText("Don't show again");
            dontShowButton.setTextColor(Color.WHITE);
            dontShowButton.setBackgroundColor(Color.TRANSPARENT);
            dontShowButton.setAllCaps(false);
            dontShowButton.setTypeface(Typeface.DEFAULT);
            dontShowButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            dontShowButton.setPadding(dpToPx(activity, 16), dpToPx(activity, 8),
                                       dpToPx(activity, 16), dpToPx(activity, 8));
            LinearLayout.LayoutParams dontShowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dontShowParams.rightMargin = dpToPx(activity, 8);
            buttonRow.addView(dontShowButton, dontShowParams);

            // "OK" button.
            Button okButton = new Button(activity);
            okButton.setText("OK");
            okButton.setTextColor(Color.BLACK);
            okButton.setBackgroundColor(COLOR_BUTTON_POSITIVE);
            okButton.setAllCaps(false);
            okButton.setTypeface(Typeface.DEFAULT_BOLD);
            okButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            okButton.setPadding(dpToPx(activity, 24), dpToPx(activity, 12),
                               dpToPx(activity, 24), dpToPx(activity, 12));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                okButton.setElevation(dpToPx(activity, 4));
            }
            buttonRow.addView(okButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Build dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(mainLayout);

            final AlertDialog dialog = builder.create();

            // Style the dialog with dark background.
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.show();

            // Set button click listeners after dialog is created.
            dontShowButton.setOnClickListener(v -> {
                SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_DONT_SHOW_DIALOG, true).apply();
                Toast.makeText(activity, "Dialog disabled. Clear app data to re-enable.",
                             Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            okButton.setOnClickListener(v -> {
                dialog.dismiss();
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to show K1 dialog", e);
        }
    }

    /**
     * Create a card view for a single token.
     */
    private static LinearLayout createTokenCard(Activity activity, String token, int index, boolean singleToken) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(COLOR_TOKEN_BG);
        card.setPadding(dpToPx(activity, 16), dpToPx(activity, 12),
                       dpToPx(activity, 16), dpToPx(activity, 12));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dpToPx(activity, 2));
        }
        card.setClickable(true);
        card.setFocusable(true);

        // Token label (only show if multiple tokens).
        if (!singleToken) {
            TextView labelView = new TextView(activity);
            labelView.setText("Token #" + index);
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            labelView.setTextColor(COLOR_ACCENT);
            labelView.setTypeface(Typeface.DEFAULT_BOLD);
            card.addView(labelView);
        }

        // Token value.
        TextView tokenView = new TextView(activity);
        tokenView.setText(token.toUpperCase());
        tokenView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tokenView.setTextColor(COLOR_TEXT_PRIMARY);
        tokenView.setTypeface(Typeface.MONOSPACE);
        tokenView.setLetterSpacing(0.05f);
        LinearLayout.LayoutParams tokenParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (!singleToken) {
            tokenParams.topMargin = dpToPx(activity, 8);
        }
        card.addView(tokenView, tokenParams);

        // Click to copy.
        card.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setText(token.toUpperCase());
                Toast.makeText(activity, "Token copied!", Toast.LENGTH_SHORT).show();
            }
        });

        return card;
    }

    /**
     * Convert dp to pixels.
     */
    private static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Get K1 tokens from log files.
     * Prioritizes pairing K1 tokens over reconnect tokens.
     */
    private static Set<String> getK1TokensFromLogFiles() {
        Set<String> pairingTokens = new LinkedHashSet<>();
        Set<String> reconnectTokens = new LinkedHashSet<>();
        try {
            File logDir = new File("/data/data/" + PACKAGE_NAME + "/files/log");
            if (!logDir.exists() || !logDir.isDirectory()) {
                return pairingTokens;
            }

            File[] logFiles = logDir.listFiles((dir, name) ->
                name.endsWith(".log") || name.endsWith(".log.") || name.matches(".*\\.log\\.\\d+"));

            if (logFiles == null || logFiles.length == 0) {
                return pairingTokens;
            }

            for (File logFile : logFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Determine if this is a pairing or reconnect context.
                        boolean isPairingContext = line.toLowerCase().contains("watchbind");
                        boolean isReconnectContext = line.toLowerCase().contains("watchreconnect");

                        String k1Token = null;

                        // First check for combined r3+k1 format (priority).
                        Matcher combinedMatcher = K1_COMBINED_PATTERN.matcher(line);
                        if (combinedMatcher.find()) {
                            String combined = combinedMatcher.group(1);
                            if (combined.length() == 64) {
                                // Second half is the actual K1
                                k1Token = combined.substring(32).toLowerCase();
                            }
                        }

                        // Then check for standalone K1 format (only if not found in combined).
                        if (k1Token == null) {
                            Matcher standaloneMatcher = K1_STANDALONE_PATTERN.matcher(line);
                            if (standaloneMatcher.find()) {
                                String token = standaloneMatcher.group(1);
                                if (token != null && token.length() == 32) {
                                    k1Token = token.toLowerCase();
                                }
                            }
                        }

                        // Add to appropriate set.
                        if (k1Token != null) {
                            if (isPairingContext && !isReconnectContext) {
                                pairingTokens.add(k1Token);
                            } else {
                                reconnectTokens.add(k1Token);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip unreadable files.
                }
            }
        } catch (Exception ex) {
            // Fail silently.
        }

        // Return pairing tokens first, add reconnect tokens if no pairing tokens found.
        if (!pairingTokens.isEmpty()) {
            Log.i(TAG, "Found " + pairingTokens.size() + " pairing K1 token(s)");
            return pairingTokens;
        }

        if (!reconnectTokens.isEmpty()) {
            Log.i(TAG, "Found " + reconnectTokens.size() + " reconnect K1 token(s) (may not work for initial pairing)");
        }
        return reconnectTokens;
    }

    /**
     * Try to get K1 tokens from the database.
     */
    private static String getK1TokensFromDatabase() {
        try {
            File dbDir = new File("/data/data/" + PACKAGE_NAME + "/databases");
            if (!dbDir.exists() || !dbDir.isDirectory()) {
                return null;
            }

            File[] dbFiles = dbDir.listFiles((dir, name) ->
                name.endsWith(".db") && !name.startsWith("google_app_measurement") && !name.contains("firebase"));

            if (dbFiles == null || dbFiles.length == 0) {
                return null;
            }

            for (File dbFile : dbFiles) {
                String token = getK1TokensFromDatabase(dbFile);
                if (token != null) {
                    return token;
                }
            }

            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Extract K1 tokens from a database file.
     */
    private static String getK1TokensFromDatabase(File dbFile) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            // Get all tables.
            Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                null
            );

            List<String> tables = new ArrayList<>();
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }
            cursor.close();

            // Scan all columns for 32-char hex strings.
            for (String table : tables) {
                Cursor schemaCursor = null;
                try {
                    schemaCursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
                    List<String> columns = new ArrayList<>();
                    while (schemaCursor.moveToNext()) {
                        columns.add(schemaCursor.getString(1));
                    }
                    schemaCursor.close();

                    for (String column : columns) {
                        Cursor dataCursor = null;
                        try {
                            dataCursor = db.query(table, new String[]{column}, null, null, null, null, null);
                            while (dataCursor.moveToNext()) {
                                String value = dataCursor.getString(0);
                                if (value != null && value.length() == 32 && value.matches("[0-9a-fA-F]{32}")) {
                                    // Skip obviously fake tokens (MD5 of empty string).
                                    if (!value.equalsIgnoreCase(EMPTY_MD5)) {
                                        dataCursor.close();
                                        db.close();
                                        return value.toLowerCase();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Skip non-string columns.
                        } finally {
                            if (dataCursor != null) {
                                dataCursor.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to next table.
                } finally {
                    if (schemaCursor != null && !schemaCursor.isClosed()) {
                        schemaCursor.close();
                    }
                }
            }

            return null;
        } catch (Exception ex) {
            return null;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Reset the logged flag (useful for testing or re-pairing).
     */
    public static void resetK1Logged() {
        k1Logged = false;
        lifecycleCallbacksRegistered = false;
    }

    /**
     * Reset the "don't show again" preference.
     */
    public static void resetDontShowPreference() {
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_DONT_SHOW_DIALOG, false).apply();
        }
    }
}
