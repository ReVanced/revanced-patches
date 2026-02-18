package app.revanced.extension.youtube.transcripts;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

/**
 * Cache for video transcript summaries to avoid repeated API calls.
 * Uses SharedPreferences for persistent storage.
 */
public class TranscriptSummaryCache {
    private static final String TAG = "TranscriptSummaryCache";
    private static final String CACHE_PREFS_NAME = "revanced_transcript_summary_cache";
    private static final String KEY_PREFIX = "summary_";
    
    private static SharedPreferences preferences;

    /**
     * Cached summary entry with metadata.
     */
    public static class CachedSummary {
        public final String summary;
        public final long timestamp;
        public final String model;

        public CachedSummary(@NonNull String summary, long timestamp, @NonNull String model) {
            this.summary = summary;
            this.timestamp = timestamp;
            this.model = model;
        }

        @NonNull
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("summary", summary);
            json.put("timestamp", timestamp);
            json.put("model", model);
            return json;
        }

        @Nullable
        public static CachedSummary fromJson(@NonNull String jsonString) {
            try {
                JSONObject json = new JSONObject(jsonString);
                String summary = json.getString("summary");
                long timestamp = json.getLong("timestamp");
                String model = json.optString("model", "unknown");
                return new CachedSummary(summary, timestamp, model);
            } catch (JSONException e) {
                Logger.printException(() -> TAG + " Failed to parse cached summary JSON", e);
                return null;
            }
        }
    }

    /**
     * Initialize the cache. Must be called before any other methods.
     */
    public static void initialize() {
        if (preferences == null) {
            Context context = Utils.getContext();
            if (context != null) {
                preferences = context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE);
                Logger.printDebug(() -> TAG + " Cache initialized with " + preferences.getAll().size() + " entries");
            } else {
                Logger.printDebug(() -> TAG + " Cannot initialize cache - context is null");
            }
        }
    }

    /**
     * Get cached summary for a video ID.
     * 
     * @param videoId The YouTube video ID
     * @return Cached summary or null if not found or expired
     */
    @Nullable
    public static CachedSummary get(@NonNull String videoId) {
        ensureInitialized();
        if (preferences == null) return null;

        String key = KEY_PREFIX + videoId;
        String cachedJson = preferences.getString(key, null);
        
        if (cachedJson == null) {
            Logger.printDebug(() -> TAG + " No cache entry for video: " + videoId);
            return null;
        }

        CachedSummary cached = CachedSummary.fromJson(cachedJson);
        if (cached != null) {
            Logger.printDebug(() -> TAG + " Cache hit for video: " + videoId);
        }
        
        return cached;
    }

    /**
     * Store a summary in the cache.
     * 
     * @param videoId The YouTube video ID
     * @param summary The summary text
     * @param model The AI model used
     */
    public static void put(@NonNull String videoId, @NonNull String summary, @NonNull String model) {
        ensureInitialized();
        if (preferences == null) return;

        try {
            CachedSummary cached = new CachedSummary(summary, System.currentTimeMillis(), model);
            String key = KEY_PREFIX + videoId;
            
            preferences.edit()
                    .putString(key, cached.toJson().toString())
                    .apply();
            
            Logger.printDebug(() -> TAG + " Cached summary for video: " + videoId);
        } catch (JSONException e) {
            Logger.printException(() -> TAG + " Failed to cache summary", e);
        }
    }

    /**
     * Check if a summary is cached for a video ID.
     * 
     * @param videoId The YouTube video ID
     * @return true if cached summary exists
     */
    public static boolean has(@NonNull String videoId) {
        ensureInitialized();
        if (preferences == null) return false;

        String key = KEY_PREFIX + videoId;
        return preferences.contains(key);
    }

    /**
     * Remove cached summary for a specific video.
     * 
     * @param videoId The YouTube video ID
     */
    public static void remove(@NonNull String videoId) {
        ensureInitialized();
        if (preferences == null) return;

        String key = KEY_PREFIX + videoId;
        preferences.edit().remove(key).apply();
        Logger.printDebug(() -> TAG + " Removed cache entry for video: " + videoId);
    }

    /**
     * Clear all cached summaries.
     */
    public static void clearAll() {
        ensureInitialized();
        if (preferences == null) return;

        int count = preferences.getAll().size();
        preferences.edit().clear().apply();
        Logger.printDebug(() -> TAG + " Cleared all " + count + " cache entries");
    }

    /**
     * Get the number of cached summaries.
     * 
     * @return Number of entries in cache
     */
    public static int size() {
        ensureInitialized();
        if (preferences == null) return 0;
        
        return preferences.getAll().size();
    }

    /**
     * Get total size of cache in bytes (approximate).
     * Useful for displaying cache statistics.
     * 
     * @return Approximate cache size in bytes
     */
    public static long getCacheSizeBytes() {
        ensureInitialized();
        if (preferences == null) return 0;

        long totalSize = 0;
        for (String value : preferences.getAll().values()) {
            if (value instanceof String) {
                totalSize += ((String) value).length() * 2; // Approximate UTF-16 encoding
            }
        }
        return totalSize;
    }

    /**
     * Format cache size for display.
     * 
     * @return Human-readable cache size (e.g., "2.5 KB")
     */
    @NonNull
    public static String formatCacheSize() {
        long bytes = getCacheSizeBytes();
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Ensure cache is initialized before use.
     */
    private static void ensureInitialized() {
        if (preferences == null) {
            initialize();
        }
    }
}
