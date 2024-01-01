package app.revanced.integrations.youtube.returnyoutubedislike.requests;

import static app.revanced.integrations.youtube.returnyoutubedislike.requests.ReturnYouTubeDislikeRoutes.getRYDConnectionFromRoute;
import static app.revanced.integrations.shared.StringRef.str;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.youtube.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

public class ReturnYouTubeDislikeApi {
    /**
     * {@link #fetchVotes(String)} TCP connection timeout
     */
    private static final int API_GET_VOTES_TCP_TIMEOUT_MILLISECONDS = 2 * 1000; // 2 Seconds.

    /**
     * {@link #fetchVotes(String)} HTTP read timeout.
     * To locally debug and force timeouts, change this to a very small number (ie: 100)
     */
    private static final int API_GET_VOTES_HTTP_TIMEOUT_MILLISECONDS = 4 * 1000; // 4 Seconds.

    /**
     * Default connection and response timeout for voting and registration.
     *
     * Voting and user registration runs in the background and has has no urgency
     * so this can be a larger value.
     */
    private static final int API_REGISTER_VOTE_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 Seconds.

    /**
     * Response code of a successful API call
     */
    private static final int HTTP_STATUS_CODE_SUCCESS = 200;

    /**
     * Indicates a client rate limit has been reached and the client must back off.
     */
    private static final int HTTP_STATUS_CODE_RATE_LIMIT = 429;

    /**
     * How long to wait until API calls are resumed, if the API requested a back off.
     * No clear guideline of how long to wait until resuming.
     */
    private static final int BACKOFF_RATE_LIMIT_MILLISECONDS = 5 * 60 * 1000; // 5 Minutes.

    /**
     * How long to wait until API calls are resumed, if any connection error occurs.
     */
    private static final int BACKOFF_CONNECTION_ERROR_MILLISECONDS = 2 * 60 * 1000; // 2 Minutes.

    /**
     * If non zero, then the system time of when API calls can resume.
     */
    private static volatile long timeToResumeAPICalls; // must be volatile, since different threads read/write to this

    /**
     * Number of times {@link #HTTP_STATUS_CODE_RATE_LIMIT} was requested by RYD api.
     * Does not include network calls attempted while rate limit is in effect,
     * and does not include rate limit imposed if a fetch fails.
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
     */
    private static void randomlyWaitIfLocallyDebugging() {
        final boolean DEBUG_RANDOMLY_DELAY_NETWORK_CALLS = false; // set true to debug UI
        if (DEBUG_RANDOMLY_DELAY_NETWORK_CALLS) {
            final long amountOfTimeToWaste = (long) (Math.random()
                    * (API_GET_VOTES_TCP_TIMEOUT_MILLISECONDS + API_GET_VOTES_HTTP_TIMEOUT_MILLISECONDS));
            Utils.doNothingForDuration(amountOfTimeToWaste);
        }
    }

    /**
     * @return True, if api rate limit is in effect.
     */
    private static boolean checkIfRateLimitInEffect(String apiEndPointName) {
        if (timeToResumeAPICalls == 0) {
            return false;
        }
        final long now = System.currentTimeMillis();
        if (now > timeToResumeAPICalls) {
            timeToResumeAPICalls = 0;
            return false;
        }
        Logger.printDebug(() -> "Ignoring api call " + apiEndPointName + " as rate limit is in effect");
        return true;
    }

    /**
     * @return True, if a client rate limit was requested
     */
    private static boolean checkIfRateLimitWasHit(int httpResponseCode) {
        final boolean DEBUG_RATE_LIMIT = false;  // set to true, to verify rate limit works
        if (DEBUG_RATE_LIMIT) {
            final double RANDOM_RATE_LIMIT_PERCENTAGE = 0.2; // 20% chance of a triggering a rate limit
            if (Math.random() < RANDOM_RATE_LIMIT_PERCENTAGE) {
                Logger.printDebug(() -> "Artificially triggering rate limit for debug purposes");
                httpResponseCode = HTTP_STATUS_CODE_RATE_LIMIT;
            }
        }
        return httpResponseCode == HTTP_STATUS_CODE_RATE_LIMIT;
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField") // Don't care, fields are estimates.
    private static void updateRateLimitAndStats(long timeNetworkCallStarted, boolean connectionError, boolean rateLimitHit) {
        if (connectionError && rateLimitHit) {
            throw new IllegalArgumentException();
        }
        final long responseTimeOfFetchCall = System.currentTimeMillis() - timeNetworkCallStarted;
        fetchCallResponseTimeTotal += responseTimeOfFetchCall;
        fetchCallResponseTimeMin = (fetchCallResponseTimeMin == 0) ? responseTimeOfFetchCall : Math.min(responseTimeOfFetchCall, fetchCallResponseTimeMin);
        fetchCallResponseTimeMax = Math.max(responseTimeOfFetchCall, fetchCallResponseTimeMax);
        fetchCallCount++;
        if (connectionError) {
            timeToResumeAPICalls = System.currentTimeMillis() + BACKOFF_CONNECTION_ERROR_MILLISECONDS;
            fetchCallResponseTimeLast = responseTimeOfFetchCall;
            fetchCallNumberOfFailures++;
        } else if (rateLimitHit) {
            Logger.printDebug(() -> "API rate limit was hit. Stopping API calls for the next "
                    + BACKOFF_RATE_LIMIT_MILLISECONDS + " seconds");
            timeToResumeAPICalls = System.currentTimeMillis() + BACKOFF_RATE_LIMIT_MILLISECONDS;
            numberOfRateLimitRequestsEncountered++;
            fetchCallResponseTimeLast = FETCH_CALL_RESPONSE_TIME_VALUE_RATE_LIMIT;
            Utils.showToastLong(str("revanced_ryd_failure_client_rate_limit_requested"));
        } else {
            fetchCallResponseTimeLast = responseTimeOfFetchCall;
        }
    }

    private static void handleConnectionError(@NonNull String toastMessage, @Nullable Exception ex) {
        if (Settings.RYD_TOAST_ON_CONNECTION_ERROR.get()) {
            Utils.showToastShort(toastMessage);
        }
        if (ex != null) {
            Logger.printInfo(() -> toastMessage, ex);
        }
    }

    /**
     * @return NULL if fetch failed, or if a rate limit is in effect.
     */
    @Nullable
    public static RYDVoteData fetchVotes(String videoId) {
        Utils.verifyOffMainThread();
        Objects.requireNonNull(videoId);

        if (checkIfRateLimitInEffect("fetchVotes")) {
            return null;
        }
        Logger.printDebug(() -> "Fetching votes for: " + videoId);
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

            randomlyWaitIfLocallyDebugging();

            final int responseCode = connection.getResponseCode();
            if (checkIfRateLimitWasHit(responseCode)) {
                connection.disconnect(); // rate limit hit, should disconnect
                updateRateLimitAndStats(timeNetworkCallStarted, false, true);
                return null;
            }

            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                // do not disconnect, the same server connection will likely be used again soon
                JSONObject json = Requester.parseJSONObject(connection);
                try {
                    RYDVoteData votingData = new RYDVoteData(json);
                    updateRateLimitAndStats(timeNetworkCallStarted, false, false);
                    Logger.printDebug(() -> "Voting data fetched: " + votingData);
                    return votingData;
                } catch (JSONException ex) {
                    Logger.printException(() -> "Failed to parse video: " + videoId + " json: " + json, ex);
                    // fall thru to update statistics
                }
            } else {
                handleConnectionError(str("revanced_ryd_failure_connection_status_code", responseCode), null);
            }
            connection.disconnect(); // something went wrong, might as well disconnect
        } catch (SocketTimeoutException ex) { // connection timed out, response timeout, or some other network error
            handleConnectionError((str("revanced_ryd_failure_connection_timeout")), ex);
        } catch (IOException ex) {
            handleConnectionError((str("revanced_ryd_failure_generic", ex.getMessage())), ex);
        } catch (Exception ex) {
            // should never happen
            Logger.printException(() -> "Failed to fetch votes", ex, str("revanced_ryd_failure_generic", ex.getMessage()));
        }

        updateRateLimitAndStats(timeNetworkCallStarted, true, false);
        return null;
    }

    /**
     * @return The newly created and registered user id.  Returns NULL if registration failed.
     */
    @Nullable
    public static String registerAsNewUser() {
        Utils.verifyOffMainThread();
        try {
            if (checkIfRateLimitInEffect("registerAsNewUser")) {
                return null;
            }
            String userId = randomString(36);
            Logger.printDebug(() -> "Trying to register new user");

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
            handleConnectionError(str("revanced_ryd_failure_connection_status_code", responseCode), null);
            connection.disconnect();
        } catch (SocketTimeoutException ex) {
            handleConnectionError(str("revanced_ryd_failure_connection_timeout"), ex);
        } catch (IOException ex) {
            handleConnectionError(str("revanced_ryd_failure_generic", "registration failed"), ex);
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to register user", ex); // should never happen
        }
        return null;
    }

    @Nullable
    private static String confirmRegistration(String userId, String solution) {
        Utils.verifyOffMainThread();
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);
        try {
            if (checkIfRateLimitInEffect("confirmRegistration")) {
                return null;
            }
            Logger.printDebug(() -> "Trying to confirm registration with solution: " + solution);

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
            String result = null;
            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                result = Requester.parseJson(connection);
                if (result.equalsIgnoreCase("true")) {
                    Logger.printDebug(() -> "Registration confirmation successful");
                    return userId;
                }
            }
            final String resultLog = result == null ? "(no response)" : result;
            Logger.printInfo(() -> "Failed to confirm registration for user: " + userId
                    + " solution: " + solution + " responseCode: " + responseCode + " responseString: " + resultLog);
            handleConnectionError(str("revanced_ryd_failure_connection_status_code", responseCode), null);
            connection.disconnect(); // something went wrong, might as well disconnect
        } catch (SocketTimeoutException ex) {
            handleConnectionError(str("revanced_ryd_failure_connection_timeout"), ex);
        } catch (IOException ex) {
            handleConnectionError(str("revanced_ryd_failure_generic", "confirm registration failed"), ex);
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to confirm registration for user: " + userId
                    + "solution: " + solution, ex);
        }
        return null;
    }

    /**
     * Must call off main thread, as this will make a network call if user is not yet registered.
     *
     * @return ReturnYouTubeDislike user ID. If user registration has never happened
     * and the network call fails, this returns NULL.
     */
    @Nullable
    private static String getUserId() {
        Utils.verifyOffMainThread();

        String userId = Settings.RYD_USER_ID.get();
        if (!userId.isEmpty()) {
            return userId;
        }

        userId = registerAsNewUser();
        if (userId != null) {
            Settings.RYD_USER_ID.save(userId);
        }
        return userId;
    }

    public static boolean sendVote(String videoId, ReturnYouTubeDislike.Vote vote) {
        Utils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(vote);

        try {
            String userId = getUserId();
            if (userId == null) return false;

            if (checkIfRateLimitInEffect("sendVote")) {
                return false;
            }
            Logger.printDebug(() -> "Trying to vote for video: " + videoId + " with vote: " + vote);

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
            Logger.printInfo(() -> "Failed to send vote for video: " + videoId + " vote: " + vote
                    + " response code was: " + responseCode);
            handleConnectionError(str("revanced_ryd_failure_connection_status_code", responseCode), null);
            connection.disconnect(); // something went wrong, might as well disconnect
        } catch (SocketTimeoutException ex) {
            handleConnectionError(str("revanced_ryd_failure_connection_timeout"), ex);
        } catch (IOException ex) {
            handleConnectionError(str("revanced_ryd_failure_generic", "send vote failed"), ex);
        } catch (Exception ex) {
            // should never happen
            Logger.printException(() -> "Failed to send vote for video: " + videoId + " vote: " + vote, ex);
        }
        return false;
    }

    private static boolean confirmVote(String videoId, String userId, String solution) {
        Utils.verifyOffMainThread();
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(solution);

        try {
            if (checkIfRateLimitInEffect("confirmVote")) {
                return false;
            }
            Logger.printDebug(() -> "Trying to confirm vote for video: " + videoId + " solution: " + solution);
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
            String result = null;
            if (responseCode == HTTP_STATUS_CODE_SUCCESS) {
                result = Requester.parseJson(connection);
                if (result.equalsIgnoreCase("true")) {
                    Logger.printDebug(() -> "Vote confirm successful for video: " + videoId);
                    return true;
                }
            }
            final String resultLog = result == null ? "(no response)" : result;
            Logger.printInfo(() -> "Failed to confirm vote for video: " + videoId
                    + " solution: " + solution + " responseCode: " + responseCode + " responseString: " + resultLog);
            handleConnectionError(str("revanced_ryd_failure_connection_status_code", responseCode), null);
            connection.disconnect(); // something went wrong, might as well disconnect
        } catch (SocketTimeoutException ex) {
            handleConnectionError(str("revanced_ryd_failure_connection_timeout"), ex);
        } catch (IOException ex) {
            handleConnectionError(str("revanced_ryd_failure_generic", "confirm vote failed"), ex);
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to confirm vote for video: " + videoId
                    + " solution: " + solution, ex); // should never happen
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
                Logger.printDebug(() -> "Found puzzle solution: " + solution + " of difficulty: " + difficulty
                        + " in: " + (System.currentTimeMillis() - timeSolveStarted) + " ms");
                return solution;
            }
        }

        // should never be reached
        throw new IllegalStateException("Failed to solve puzzle challenge: " + challenge + " difficulty: " + difficulty);
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
        for (byte b : uInt8View) {
            int value = b & 0xFF;
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
