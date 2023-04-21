package app.revanced.integrations.returnyoutubedislike.requests;

import static app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeRoutes.getRYDConnectionFromRoute;
import static app.revanced.integrations.utils.StringRef.str;

import android.util.Base64;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ReturnYouTubeDislikeApi {
    /**
     * {@link #fetchVotes(String)} TCP connection timeout
     */
    private static final int API_GET_VOTES_TCP_TIMEOUT_MILLISECONDS = 2000;

    /**
     * {@link #fetchVotes(String)} HTTP read timeout
     *  To locally debug and force timeouts, change this to a very small number (ie: 100)
     */
    private static final int API_GET_VOTES_HTTP_TIMEOUT_MILLISECONDS = 4000;

    /**
     * Default connection and response timeout for voting and registration.
     *
     * Voting and user registration runs in the background and has has no urgency
     * so this can be a larger value.
     */
    private static final int API_REGISTER_VOTE_TIMEOUT_MILLISECONDS = 90000;

    /**
     * Response code of a successful API call
     */
    private static final int HTTP_STATUS_CODE_SUCCESS = 200;

    /**
     * Response code indicating the video id is not for a video that can be voted for.
     * (it's not a Short or a regular video, and it's likely a YouTube Story)
     */
    private static final int HTTP_STATUS_CODE_NOT_FOUND = 404;

    /**
     * Indicates a client rate limit has been reached
     */
    private static final int RATE_LIMIT_HTTP_STATUS_CODE = 429;

    /**
     * How long to wait until API calls are resumed, if a rate limit is hit.
     * No clear guideline of how long to backoff. Using 2 minutes for now.
     */
    private static final int RATE_LIMIT_BACKOFF_SECONDS = 120;

    /**
     * Last time a {@link #RATE_LIMIT_HTTP_STATUS_CODE} was reached.
     * zero if has not been reached.
     */
    private static volatile long lastTimeRateLimitWasHit; // must be volatile, since different threads read/write to this

    /**
     * Number of times {@link #RATE_LIMIT_HTTP_STATUS_CODE} was requested by RYD api.
     * Does not include network calls attempted while rate limit is in effect
     */
    private static volatile int numberOfRateLimitRequestsEncountered;

    /**
     * Number of network calls made in {@link #fetchVotes(String)}
     */
    private static volatile int fetchCallCount;

    /**
     * Number of times {@link #fetchVotes(String)} failed due to timeout or any other error.
     * This does not include when rate limit requests are encountered.
     */
    private static volatile int fetchCallNumberOfFailures;

    /**
     * Total time spent waiting for {@link #fetchVotes(String)} network call to complete.
     * Value does does not persist on app shut down.
     */
    private static volatile long fetchCallResponseTimeTotal;

    /**
     * Round trip network time for the most recent call to {@link #fetchVotes(String)}
     */
    private static volatile long fetchCallResponseTimeLast;
    private static volatile long fetchCallResponseTimeMin;
    private static volatile long fetchCallResponseTimeMax;

    public static final int FETCH_CALL_RESPONSE_TIME_VALUE_RATE_LIMIT = -1;

    /**
     * If rate limit was hit, this returns {@link #FETCH_CALL_RESPONSE_TIME_VALUE_RATE_LIMIT}
     */
    public static long getFetchCallResponseTimeLast() {
        return fetchCallResponseTimeLast;
    }
    public static long getFetchCallResponseTimeMin() {
        return fetchCallResponseTimeMin;
    }
    public static long getFetchCallResponseTimeMax() {
        return fetchCallResponseTimeMax;
    }
    public static long getFetchCallResponseTimeAverage() {
        return fetchCallCount == 0 ? 0 : (fetchCallResponseTimeTotal / fetchCallCount);
    }
    public static int getFetchCallCount() {
        return fetchCallCount;
    }
    public static int getFetchCallNumberOfFailures() {
        return fetchCallNumberOfFailures;
    }
    public static int getNumberOfRateLimitRequestsEncountered() {
        return numberOfRateLimitRequestsEncountered;
    }

    private ReturnYouTubeDislikeApi() {
    } // utility class

    /**
     * Simulates a slow response by doing meaningless calculations.
     * Used to debug the app UI and verify UI timeout logic works
     *
     * @param maximumTimeToWait maximum time to wait
     */
    @SuppressWarnings("UnusedReturnValue")
    private static long randomlyWaitIfLocallyDebugging(long maximumTimeToWait) {
        final boolean DEBUG_RANDOMLY_DELAY_NETWORK_CALLS = false; // set true to debug UI
        if (DEBUG_RANDOMLY_DELAY_NETWORK_CALLS) {
            final long amountOfTimeToWaste = (long) (Math.random() * maximumTimeToWait);
            final long timeCalculationStarted = System.currentTimeMillis();
            LogHelper.printDebug(() -> "Artificially creating network delay of: " + amountOfTimeToWaste + " ms");

            long meaninglessValue = 0;
            while (System.currentTimeMillis() - timeCalculationStarted < amountOfTimeToWaste) {
                // could do a thread sleep, but that will trigger an exception if the thread is interrupted
                meaninglessValue += Long.numberOfLeadingZeros((long) (Math.random() * Long.MAX_VALUE));
            }
            // return the value, otherwise the compiler or VM might optimize and remove the meaningless time wasting work,
            // leaving an empty loop that hammers on the System.currentTimeMillis native call
            return meaninglessValue;
        }
        return 0;
    }

    /**
     * @return True, if api rate limit is in effect.
     */
    private static boolean checkIfRateLimitInEffect(String apiEndPointName) {
        if (lastTimeRateLimitWasHit == 0) {
            return false;
        }
        final long numberOfSecondsSinceLastRateLimit = (System.currentTimeMillis() - lastTimeRateLimitWasHit) / 1000;
        if (numberOfSecondsSinceLastRateLimit < RATE_LIMIT_BACKOFF_SECONDS) {
            LogHelper.printDebug(() -> "Ignoring api call " + apiEndPointName + " as only "
                    + numberOfSecondsSinceLastRateLimit + " seconds has passed since last rate limit.");
            return true;
        }
        return false;
    }

    /**
     * @return True, if a client rate limit was requested
     */
    private static boolean checkIfRateLimitWasHit(int httpResponseCode) {
        final boolean DEBUG_RATE_LIMIT = false;  // set to true, to verify rate limit works
        if (DEBUG_RATE_LIMIT) {
            final double RANDOM_RATE_LIMIT_PERCENTAGE = 0.2; // 20% chance of a triggering a rate limit
            if (Math.random() < RANDOM_RATE_LIMIT_PERCENTAGE) {
                LogHelper.printDebug(() -> "Artificially triggering rate limit for debug purposes");
                httpResponseCode = RATE_LIMIT_HTTP_STATUS_CODE;
            }
        }

        if (httpResponseCode == RATE_LIMIT_HTTP_STATUS_CODE) {
            lastTimeRateLimitWasHit = System.currentTimeMillis();
            //noinspection NonAtomicOperationOnVolatileField // don't care, field is used only as an estimate
            numberOfRateLimitRequestsEncountered++;
            LogHelper.printDebug(() -> "API rate limit was hit. Stopping API calls for the next "
                    + RATE_LIMIT_BACKOFF_SECONDS + " seconds");
            ReVancedUtils.showToastLong(str("revanced_ryd_failure_client_rate_limit_requested"));
            return true;
        }
        return false;
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField") // do not want to pay performance cost of full synchronization for debug fields that are only estimates anyways
    private static void updateStatistics(long timeNetworkCallStarted, long timeNetworkCallEnded, boolean connectionError, boolean rateLimitHit) {
        if (connectionError && rateLimitHit) {
            throw new IllegalArgumentException();
        }
        final long responseTimeOfFetchCall = timeNetworkCallEnded - timeNetworkCallStarted;
        fetchCallResponseTimeTotal += responseTimeOfFetchCall;
        fetchCallResponseTimeMin = (fetchCallResponseTimeMin == 0) ? responseTimeOfFetchCall : Math.min(responseTimeOfFetchCall, fetchCallResponseTimeMin);
        fetchCallResponseTimeMax = Math.max(responseTimeOfFetchCall, fetchCallResponseTimeMax);
        fetchCallCount++;
        if (connectionError) {
            fetchCallResponseTimeLast = responseTimeOfFetchCall;
            fetchCallNumberOfFailures++;
        } else if (rateLimitHit) {
            fetchCallResponseTimeLast = FETCH_CALL_RESPONSE_TIME_VALUE_RATE_LIMIT;
        } else {
            fetchCallResponseTimeLast = responseTimeOfFetchCall;
        }
    }

    /**
     * @return NULL if fetch failed, or if a rate limit is in effect.
     */
    @Nullable
    public static RYDVoteData fetchVotes(String videoId) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);

        if (checkIfRateLimitInEffect("fetchVotes")) {
            return null;
        }
        LogHelper.printDebug(() -> "Fetching votes for: " + videoId);
        final long timeNetworkCallStarted = System.currentTimeMillis();

        try {
            HttpURLConnection connection = getRYDConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_DISLIKES, videoId);
            // request headers, as per https://returnyoutubedislike.com/docs/fetching
            // the documentation says to use 'Accept:text/html', but the RYD browser plugin uses 'Accept:application/json'
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Connection", "keep-alive"); // keep-alive is on by default with http 1.1, but specify anyways
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setUseCaches(false);
            connection.setConnectTimeout(API_GET_VOTES_TCP_TIMEOUT_MILLISECONDS); // timeout for TCP connection to server
            connection.setReadTimeout(API_GET_VOTES_HTTP_TIMEOUT_MILLISECONDS); // timeout for server response

            randomlyWaitIfLocallyDebugging(2*(API_GET_VOTES_TCP_TIMEOUT_MILLISECONDS + API_GET_VOTES_HTTP_TIMEOUT_MILLISECONDS));

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect(); // rate limit hit, should disconnect
                updateStatistics(timeNetworkCallStarted, System.currentTimeMillis(),false, true);
                return null;
            }

            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                final long timeNetworkCallEnded = System.currentTimeMillis(); // record end time before parsing
                // do not disconnect, the same server connection will likely be used again soon
                JSONObject json = Requester.parseJSONObject(connection);
                try {
                    RYDVoteData votingData = new RYDVoteData(json);
                    updateStatistics(timeNetworkCallStarted, timeNetworkCallEnded, false, false);
                    LogHelper.printDebug(() -> "Voting data fetched: " + votingData);
                    return votingData;
                } catch (JSONException ex) {
                    LogHelper.printException(() -> "Failed to parse video: " + videoId + " json: " + json, ex);
                    // fall thru to update statistics
                }
            } else if (responseCode == HTTP_STATUS_CODE_NOT_FOUND) {
                // normal response when viewing YouTube Stories (cannot vote for these)
                LogHelper.printDebug(() -> "Video has no like/dislikes (video is a YouTube Story?): " + videoId);
                return null; // do not updated connection statistics
            } else {
                LogHelper.printException(() -> "Failed to fetch votes for video: " + videoId + " response code was: " + responseCode,
                        null, str("revanced_ryd_failure_connection_status_code", responseCode));
                connection.disconnect(); // something went wrong, might as well disconnect
            }
        } catch (SocketTimeoutException ex) { // connection timed out, response timeout, or some other network error
            LogHelper.printException(() -> "Failed to fetch votes", ex, str("revanced_ryd_failure_connection_timeout"));
        } catch (Exception ex) {
            // should never happen
            LogHelper.printException(() -> "Failed to fetch votes", ex, str("revanced_ryd_failure_generic", ex.getMessage()));
        }

        updateStatistics(timeNetworkCallStarted, System.currentTimeMillis(), true, false);
        return null;
    }

    /**
     * @return The newly created and registered user id.  Returns NULL if registration failed.
     */
    @Nullable
    public static String registerAsNewUser() {
        ReVancedUtils.verifyOffMainThread();
        try {
            if (checkIfRateLimitInEffect("registerAsNewUser")) {
                return null;
            }
            String userId = randomString(36);
            LogHelper.printDebug(() -> "Trying to register new user: " + userId);

            HttpURLConnection connection = getRYDConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_REGISTRATION, userId);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(API_REGISTER_VOTE_TIMEOUT_MILLISECONDS);
            connection.setReadTimeout(API_REGISTER_VOTE_TIMEOUT_MILLISECONDS);

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect(); // disconnect, as no more connections will be made for a little while
                return null;
            }
            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                JSONObject json = Requester.parseJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");

                String solution = solvePuzzle(challenge, difficulty);
                return confirmRegistration(userId, solution);
            }
            LogHelper.printException(() -> "Failed to register new user: " + userId
                    + " response code was: " + responseCode); // failed attempt, and ok to log userId
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to register user", ex);
        }
        return null;
    }

    @Nullable
    private static String confirmRegistration(String userId, String solution) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);
        try {
            if (checkIfRateLimitInEffect("confirmRegistration")) {
                return null;
            }
            LogHelper.printDebug(() -> "Trying to confirm registration with solution: " + solution);

            HttpURLConnection connection = getRYDConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_REGISTRATION, userId);
            applyCommonPostRequestSettings(connection);

            String jsonInputString = "{\"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect(); // disconnect, as no more connections will be made for a little while
                return null;
            }
            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                String result = Requester.parseJson(connection);
                if (result.equalsIgnoreCase("true")) {
                    LogHelper.printDebug(() -> "Registration confirmation successful");
                    return userId;
                }
                LogHelper.printException(() -> "Failed to confirm registration for user: " + userId
                        + " solution: " + solution + " response string was: " + result);
            } else {
                LogHelper.printException(() -> "Failed to confirm registration for user: " + userId
                        + " solution: " + solution + " response code was: " + responseCode);
            }
            connection.disconnect(); // something went wrong, might as well disconnect
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to confirm registration for user: " + userId
                    + "solution: " + solution, ex);
        }
        return null;
    }

    public static boolean sendVote(String videoId, String userId, ReturnYouTubeDislike.Vote vote) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(vote);

        try {
            if (checkIfRateLimitInEffect("sendVote")) {
                return false;
            }
            LogHelper.printDebug(() -> "Trying to vote for video: " + videoId + " with vote: " + vote);

            HttpURLConnection connection = getRYDConnectionFromRoute(ReturnYouTubeDislikeRoutes.SEND_VOTE);
            applyCommonPostRequestSettings(connection);

            String voteJsonString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"value\": \"" + vote.value + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = voteJsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect(); // disconnect, as no more connections will be made for a little while
                return false;
            }
            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                JSONObject json = Requester.parseJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");

                String solution = solvePuzzle(challenge, difficulty);
                return confirmVote(videoId, userId, solution);
            }
            LogHelper.printException(() -> "Failed to send vote for video: " + videoId
                    + " vote: " + vote + " response code was: " + responseCode);
            connection.disconnect(); // something went wrong, might as well disconnect
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to send vote for video: " + videoId + " vote: " + vote, ex);
        }
        return false;
    }

    private static boolean confirmVote(String videoId, String userId, String solution) {
        ReVancedUtils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);

        try {
            if (checkIfRateLimitInEffect("confirmVote")) {
                return false;
            }
            LogHelper.printDebug(() -> "Trying to confirm vote for video: " + videoId + " solution: " + solution);
            HttpURLConnection connection = getRYDConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_VOTE);
            applyCommonPostRequestSettings(connection);

            String jsonInputString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect(); // disconnect, as no more connections will be made for a little while
                return false;
            }

            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                String result = Requester.parseJson(connection);
                if (result.equalsIgnoreCase("true")) {
                    LogHelper.printDebug(() -> "Vote confirm successful for video: " + videoId);
                    return true;
                }
                LogHelper.printException(() -> "Failed to confirm vote for video: " + videoId
                        + " solution: " + solution + " response string was: " + result);
            } else {
                LogHelper.printException(() -> "Failed to confirm vote for video: " + videoId
                        + " solution: " + solution + " response code was: " + responseCode);
            }
            connection.disconnect(); // something went wrong, might as well disconnect
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to confirm vote for video: " + videoId
                    + " solution: " + solution, ex);
        }
        return false;
    }

    private static void applyCommonPostRequestSettings(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setConnectTimeout(API_REGISTER_VOTE_TIMEOUT_MILLISECONDS); // timeout for TCP connection to server
        connection.setReadTimeout(API_REGISTER_VOTE_TIMEOUT_MILLISECONDS); // timeout for server response
    }


    private static String solvePuzzle(String challenge, int difficulty) {
        final long timeSolveStarted = System.currentTimeMillis();
        byte[] decodedChallenge = Base64.decode(challenge, Base64.NO_WRAP);

        byte[] buffer = new byte[20];
        System.arraycopy(decodedChallenge, 0, buffer, 4, 16);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex); // should never happen
        }

        final int maxCount = (int) (Math.pow(2, difficulty + 1) * 5);
        for (int i = 0; i < maxCount; i++) {
            buffer[0] = (byte) i;
            buffer[1] = (byte) (i >> 8);
            buffer[2] = (byte) (i >> 16);
            buffer[3] = (byte) (i >> 24);
            byte[] messageDigest = md.digest(buffer);

            if (countLeadingZeroes(messageDigest) >= difficulty) {
                String solution = Base64.encodeToString(new byte[]{buffer[0], buffer[1], buffer[2], buffer[3]}, Base64.NO_WRAP);
                LogHelper.printDebug(() -> "Found puzzle solution: " + solution + " of difficulty: " + difficulty
                        + " in: " + (System.currentTimeMillis() - timeSolveStarted) + " ms");
                return solution;
            }
        }

        // should never be reached
        throw new IllegalStateException("Failed to solve puzzle challenge: " + challenge + " of difficulty: " + difficulty);
    }

    // https://stackoverflow.com/a/157202
    private static String randomString(int len) {
        String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    private static int countLeadingZeroes(byte[] uInt8View) {
        int zeroes = 0;
        int value;
        for (byte b : uInt8View) {
            value = b & 0xFF;
            if (value == 0) {
                zeroes += 8;
            } else {
                int count = 1;
                if (value >>> 4 == 0) {
                    count += 4;
                    value <<= 4;
                }
                if (value >>> 6 == 0) {
                    count += 2;
                    value <<= 2;
                }
                zeroes += count - (value >>> 7);
                break;
            }
        }
        return zeroes;
    }
}
