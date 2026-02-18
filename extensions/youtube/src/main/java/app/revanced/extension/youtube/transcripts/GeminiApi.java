package app.revanced.extension.youtube.transcripts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.requests.Requester;

/**
 * Handles communication with Google Gemini API for text summarization.
 */
public class GeminiApi {
    private static final String TAG = "GeminiApi";
    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String MODEL_NAME = "gemini-1.5-flash"; // Fast and cost-effective
    private static final int CONNECTION_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 60000; // Longer timeout for AI processing
    private static final int MAX_TRANSCRIPT_CHARS = 30000; // Limit to avoid token limits

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * Result of a summarization request.
     */
    public static class SummaryResult {
        public final String summary;
        public final String errorMessage;
        public final boolean success;

        public SummaryResult(@NonNull String summary) {
            this.summary = summary;
            this.errorMessage = null;
            this.success = true;
        }

        public SummaryResult(@Nullable String errorMessage, boolean success) {
            this.summary = null;
            this.errorMessage = errorMessage != null ? errorMessage : "Unknown error";
            this.success = success;
        }

        public static SummaryResult error(@NonNull String message) {
            return new SummaryResult(message, false);
        }

        public static SummaryResult success(@NonNull String summary) {
            return new SummaryResult(summary);
        }
    }

    /**
     * Callback for async summarization requests.
     */
    public interface SummaryCallback {
        void onSuccess(@NonNull String summary);
        void onError(@NonNull String errorMessage);
    }

    /**
     * Asynchronously summarize transcript text using Gemini API.
     * 
     * @param apiKey The Gemini API key
     * @param transcriptText The transcript to summarize
     * @param callback Callback for results
     * @return Future that can be used to cancel the operation
     */
    @NonNull
    public static Future<?> summarizeAsync(@NonNull String apiKey, 
                                           @NonNull String transcriptText,
                                           @NonNull SummaryCallback callback) {
        return executor.submit(() -> {
            try {
                SummaryResult result = summarize(apiKey, transcriptText);
                if (result.success && result.summary != null) {
                    callback.onSuccess(result.summary);
                } else {
                    callback.onError(result.errorMessage);
                }
            } catch (Exception e) {
                Logger.printException(() -> TAG + " Unexpected error in async summarization", e);
                callback.onError("Unexpected error: " + e.getMessage());
            }
        });
    }

    /**
     * Synchronously summarize transcript text using Gemini API.
     * Should be called on a background thread.
     * 
     * @param apiKey The Gemini API key
     * @param transcriptText The transcript to summarize
     * @return SummaryResult containing the summary or error
     */
    @NonNull
    public static SummaryResult summarize(@NonNull String apiKey, @NonNull String transcriptText) {
        if (apiKey.isEmpty()) {
            return SummaryResult.error("API key is empty");
        }

        if (transcriptText.isEmpty()) {
            return SummaryResult.error("Transcript is empty");
        }

        // Truncate transcript if too long
        String truncatedTranscript = transcriptText;
        if (transcriptText.length() > MAX_TRANSCRIPT_CHARS) {
            truncatedTranscript = transcriptText.substring(0, MAX_TRANSCRIPT_CHARS) + "\n[Transcript truncated...]";
            Logger.printDebug(() -> TAG + " Truncated transcript from " + transcriptText.length() + 
                " to " + MAX_TRANSCRIPT_CHARS + " chars");
        }

        try {
            String endpoint = GEMINI_API_BASE + MODEL_NAME + ":generateContent?key=" + apiKey;
            URL url = new URL(endpoint);
            
            Logger.printDebug(() -> TAG + " Sending summarization request to Gemini");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Build request body
            JSONObject requestBody = buildRequestBody(truncatedTranscript);
            byte[] requestBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);

            connection.setFixedLengthStreamingMode(requestBytes.length);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBytes);
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                String response = Requester.parseString(connection);
                connection.disconnect();
                return parseGeminiResponse(response);
            } else {
                String errorResponse = Requester.parseErrorString(connection);
                connection.disconnect();
                Logger.printDebug(() -> TAG + " API error " + responseCode + ": " + errorResponse);
                return handleErrorResponse(responseCode, errorResponse);
            }

        } catch (IOException e) {
            Logger.printException(() -> TAG + " Network error during summarization", e);
            return SummaryResult.error("Network error: " + e.getMessage());
        } catch (JSONException e) {
            Logger.printException(() -> TAG + " JSON error building request", e);
            return SummaryResult.error("Request format error");
        }
    }

    /**
     * Builds the JSON request body for Gemini API.
     */
    @NonNull
    private static JSONObject buildRequestBody(@NonNull String transcriptText) throws JSONException {
        String prompt = "You are a helpful assistant that summarizes YouTube video transcripts. " +
                "Please provide a clear, concise summary of the following video transcript in 3-5 key points. " +
                "Focus on the main topics and important information. " +
                "Format your response as a bulleted list.\n\n" +
                "Transcript:\n" + transcriptText;

        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);

        // Add generation config for better output
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.4); // Lower temperature for more focused summaries
        generationConfig.put("maxOutputTokens", 500); // Limit summary length
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 10);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    /**
     * Parse the successful response from Gemini API.
     */
    @NonNull
    private static SummaryResult parseGeminiResponse(@NonNull String response) {
        try {
            JSONObject root = new JSONObject(response);
            
            if (!root.has("candidates")) {
                Logger.printDebug(() -> TAG + " No candidates in response");
                return SummaryResult.error("Invalid API response format");
            }

            JSONArray candidates = root.getJSONArray("candidates");
            if (candidates.length() == 0) {
                return SummaryResult.error("No summary generated");
            }

            JSONObject candidate = candidates.getJSONObject(0);
            
            // Check for finish reason
            String finishReason = candidate.optString("finishReason", "");
            if ("SAFETY".equals(finishReason)) {
                return SummaryResult.error("Content blocked by safety filters");
            }

            if (!candidate.has("content")) {
                return SummaryResult.error("No content in response");
            }

            JSONObject content = candidate.getJSONObject("content");
            if (!content.has("parts")) {
                return SummaryResult.error("No parts in content");
            }

            JSONArray parts = content.getJSONArray("parts");
            if (parts.length() == 0) {
                return SummaryResult.error("Empty response");
            }

            JSONObject firstPart = parts.getJSONObject(0);
            String summaryText = firstPart.optString("text", "").trim();

            if (summaryText.isEmpty()) {
                return SummaryResult.error("Empty summary text");
            }

            Logger.printDebug(() -> TAG + " Successfully parsed summary: " + summaryText.length() + " chars");
            return SummaryResult.success(summaryText);

        } catch (JSONException e) {
            Logger.printException(() -> TAG + " Failed to parse Gemini response", e);
            return SummaryResult.error("Failed to parse API response");
        }
    }

    /**
     * Handle error responses from Gemini API.
     */
    @NonNull
    private static SummaryResult handleErrorResponse(int responseCode, @NonNull String errorResponse) {
        try {
            JSONObject error = new JSONObject(errorResponse);
            if (error.has("error")) {
                JSONObject errorObj = error.getJSONObject("error");
                String message = errorObj.optString("message", "Unknown error");
                String status = errorObj.optString("status", "");
                
                Logger.printDebug(() -> TAG + " API error status: " + status + ", message: " + message);
                
                if (responseCode == 400) {
                    return SummaryResult.error("Invalid request: " + message);
                } else if (responseCode == 403) {
                    return SummaryResult.error("API key invalid or quota exceeded");
                } else if (responseCode == 429) {
                    return SummaryResult.error("Rate limit exceeded. Please try again later.");
                } else if (responseCode >= 500) {
                    return SummaryResult.error("Gemini service error. Please try again later.");
                }
                
                return SummaryResult.error(message);
            }
        } catch (JSONException e) {
            Logger.printDebug(() -> TAG + " Could not parse error response");
        }

        return SummaryResult.error("API request failed with code " + responseCode);
    }

    /**
     * Validate API key format (basic check).
     * @return true if format looks valid
     */
    public static boolean isValidApiKeyFormat(@Nullable String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }
        
        // Gemini API keys typically start with "AIza" and are 39 characters
        // This is just a basic format check, not authentication
        return apiKey.length() >= 30 && apiKey.matches("[A-Za-z0-9_-]+");
    }
}
