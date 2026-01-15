package app.revanced.extension.nothingx.patches;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
public class LogK1TokenPatch {

    private static final String TAG = "NothingXKey";
    private static final String PACKAGE_NAME = "com.nothing.smartcenter";
    private static final String EMPTY_MD5 = "d41d8cd98f00b204e9800998ecf8427e";

    // Match standalone K1: k1:, K1:, k1>, etc.
    private static final Pattern K1_STANDALONE_PATTERN = Pattern.compile("(?i)(?:k1\\s*[:>]\\s*)([0-9a-f]{32})");
    // Match combined r3+k1: format (64 chars = r3(32) + k1(32))
    private static final Pattern K1_COMBINED_PATTERN = Pattern.compile("(?i)r3\\+k1\\s*:\\s*([0-9a-f]{64})");

    private static volatile boolean k1Logged = false;

    /**
     * Hook to scan existing log files for K1 token.
     * Call this after the app initializes.
     */
    public static void scanLogFilesForK1Token() {
        if (k1Logged) {
            return;
        }

        Set<String> allTokens = new LinkedHashSet<>();

        // First try to get from database
        String dbToken = getK1FromDatabase();
        if (dbToken != null) {
            allTokens.add(dbToken);
        }

        // Then get from log files
        Set<String> logTokens = getK1FromLogFiles();
        allTokens.addAll(logTokens);

        if (allTokens.isEmpty()) {
            return;
        }

        // Log all found tokens
        int index = 1;
        for (String token : allTokens) {
            Log.i(TAG, "#" + index + ": " + token.toUpperCase());
            index++;
        }

        k1Logged = true;
    }

    /**
     * Get K1 tokens from log files.
     */
    private static Set<String> getK1FromLogFiles() {
        Set<String> tokens = new LinkedHashSet<>();
        try {
            File logDir = new File("/data/data/" + PACKAGE_NAME + "/files/log");
            if (!logDir.exists() || !logDir.isDirectory()) {
                return tokens;
            }

            File[] logFiles = logDir.listFiles((dir, name) ->
                name.endsWith(".log") || name.endsWith(".log.") || name.matches(".*\\.log\\.\\d+"));

            if (logFiles == null || logFiles.length == 0) {
                return tokens;
            }

            for (File logFile : logFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // First check for combined r3+k1 format
                        Matcher combinedMatcher = K1_COMBINED_PATTERN.matcher(line);
                        if (combinedMatcher.find()) {
                            String combined = combinedMatcher.group(1);
                            if (combined.length() == 64) {
                                // Second half is the actual K1
                                String k1Token = combined.substring(32).toLowerCase();
                                tokens.add(k1Token);
                            }
                        }

                        // Then check for standalone K1 format
                        Matcher standaloneMatcher = K1_STANDALONE_PATTERN.matcher(line);
                        while (standaloneMatcher.find()) {
                            String k1Token = standaloneMatcher.group(1);
                            if (k1Token != null && k1Token.length() == 32) {
                                tokens.add(k1Token.toLowerCase());
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip unreadable files
                }
            }
        } catch (Exception ex) {
            // Fail silently
        }

        return tokens;
    }

    /**
     * Try to get K1 token from the database.
     */
    private static String getK1FromDatabase() {
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
                String token = extractK1FromDatabase(dbFile);
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
     * Extract K1 from a database file.
     */
    private static String extractK1FromDatabase(File dbFile) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            // Get all tables
            Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                null
            );

            List<String> tables = new ArrayList<>();
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }
            cursor.close();

            // Scan all columns for 32-char hex strings
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
                                    // Skip obviously fake tokens (MD5 of empty string)
                                    if (!value.equalsIgnoreCase(EMPTY_MD5)) {
                                        dataCursor.close();
                                        db.close();
                                        return value.toLowerCase();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Skip non-string columns
                        } finally {
                            if (dataCursor != null) {
                                dataCursor.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to next table
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
    }
}