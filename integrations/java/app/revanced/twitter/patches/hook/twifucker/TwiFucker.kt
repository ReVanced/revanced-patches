package app.revanced.twitter.patches.hook.twifucker

import android.util.Log
import app.revanced.twitter.patches.hook.twifucker.TwiFuckerUtils.forEach
import app.revanced.twitter.patches.hook.twifucker.TwiFuckerUtils.forEachIndexed
import org.json.JSONArray
import org.json.JSONObject

// https://raw.githubusercontent.com/Dr-TSNG/TwiFucker/880cdf1c1622e54ab45561ffcb4f53d94ed97bae/app/src/main/java/icu/nullptr/twifucker/hook/JsonHook.kt
internal object TwiFucker {
    // root
    private fun JSONObject.jsonGetInstructions(): JSONArray? =
        optJSONObject("timeline")?.optJSONArray("instructions")

    private fun JSONObject.jsonGetData(): JSONObject? = optJSONObject("data")

    private fun JSONObject.jsonHasRecommendedUsers(): Boolean = has("recommended_users")

    private fun JSONObject.jsonRemoveRecommendedUsers() {
        remove("recommended_users")
    }

    private fun JSONObject.jsonCheckAndRemoveRecommendedUsers() {
        if (jsonHasRecommendedUsers()) {
            Log.d("revanced", "Handle recommended users: $this")
            jsonRemoveRecommendedUsers()
        }
    }

    private fun JSONObject.jsonHasThreads(): Boolean = has("threads")

    private fun JSONObject.jsonRemoveThreads() {
        remove("threads")
    }

    private fun JSONObject.jsonCheckAndRemoveThreads() {
        if (jsonHasThreads()) {
            Log.d("revabced", "Handle threads: $this")
            jsonRemoveThreads()
        }
    }

    // data
    private fun JSONObject.dataGetInstructions(): JSONArray? {
        val timeline = optJSONObject("user_result")?.optJSONObject("result")
            ?.optJSONObject("timeline_response")?.optJSONObject("timeline")
            ?: optJSONObject("timeline_response")?.optJSONObject("timeline")
            ?: optJSONObject("timeline_response")
        return timeline?.optJSONArray("instructions")
    }

    private fun JSONObject.dataCheckAndRemove() {
        dataGetInstructions()?.forEach { instruction ->
            instruction.instructionCheckAndRemove()
        }
    }

    private fun JSONObject.dataGetLegacy(): JSONObject? =
        optJSONObject("tweet_result")?.optJSONObject("result")?.let {
            if (it.has("tweet")) {
                it.optJSONObject("tweet")
            } else {
                it
            }
        }?.optJSONObject("legacy")


    // entry
    private fun JSONObject.entryHasPromotedMetadata(): Boolean =
        optJSONObject("content")?.optJSONObject("item")?.optJSONObject("content")
            ?.optJSONObject("tweet")
            ?.has("promotedMetadata") == true || optJSONObject("content")?.optJSONObject("content")
            ?.has("tweetPromotedMetadata") == true || optJSONObject("item")?.optJSONObject("content")
            ?.has("tweetPromotedMetadata") == true

    private fun JSONObject.entryGetContentItems(): JSONArray? =
        optJSONObject("content")?.optJSONArray("items")
            ?: optJSONObject("content")?.optJSONObject("timelineModule")?.optJSONArray("items")

    private fun JSONObject.entryIsTweetDetailRelatedTweets(): Boolean =
        optString("entryId").startsWith("tweetdetailrelatedtweets-")

    private fun JSONObject.entryGetTrends(): JSONArray? =
        optJSONObject("content")?.optJSONObject("timelineModule")?.optJSONArray("items")

    // trend
    private fun JSONObject.trendHasPromotedMetadata(): Boolean =
        optJSONObject("item")?.optJSONObject("content")?.optJSONObject("trend")
            ?.has("promotedMetadata") == true

    private fun JSONArray.trendRemoveAds() {
        val trendRemoveIndex = mutableListOf<Int>()
        forEachIndexed { trendIndex, trend ->
            if (trend.trendHasPromotedMetadata()) {
                Log.d("revanced", "Handle trends ads $trendIndex $trend")
                trendRemoveIndex.add(trendIndex)
            }
        }
        for (i in trendRemoveIndex.asReversed()) {
            remove(i)
        }
    }

    // instruction
    private fun JSONObject.instructionTimelineAddEntries(): JSONArray? = optJSONArray("entries")

    private fun JSONObject.instructionGetAddEntries(): JSONArray? =
        optJSONObject("addEntries")?.optJSONArray("entries")

    private fun JSONObject.instructionCheckAndRemove() {
        instructionTimelineAddEntries()?.entriesRemoveAnnoyance()
        instructionGetAddEntries()?.entriesRemoveAnnoyance()
    }

    // entries
    private fun JSONArray.entriesRemoveTimelineAds() {
        val removeIndex = mutableListOf<Int>()
        forEachIndexed { entryIndex, entry ->
            entry.entryGetTrends()?.trendRemoveAds()

            if (entry.entryHasPromotedMetadata()) {
                Log.d("revanced", "Handle timeline ads $entryIndex $entry")
                removeIndex.add(entryIndex)
            }

            val innerRemoveIndex = mutableListOf<Int>()
            val contentItems = entry.entryGetContentItems()
            contentItems?.forEachIndexed inner@{ itemIndex, item ->
                if (item.entryHasPromotedMetadata()) {
                    Log.d("revanced", "Handle timeline replies ads $entryIndex $entry")
                    if (contentItems.length() == 1) {
                        removeIndex.add(entryIndex)
                    } else {
                        innerRemoveIndex.add(itemIndex)
                    }
                    return@inner
                }
            }
            for (i in innerRemoveIndex.asReversed()) {
                contentItems?.remove(i)
            }
        }
        for (i in removeIndex.reversed()) {
            remove(i)
        }
    }

    private fun JSONArray.entriesRemoveTweetDetailRelatedTweets() {
        val removeIndex = mutableListOf<Int>()
        forEachIndexed { entryIndex, entry ->

            if (entry.entryIsTweetDetailRelatedTweets()) {
                Log.d("revanced", "Handle tweet detail related tweets $entryIndex $entry")
                removeIndex.add(entryIndex)
            }
        }
        for (i in removeIndex.reversed()) {
            remove(i)
        }
    }

    private fun JSONArray.entriesRemoveAnnoyance() {
        entriesRemoveTimelineAds()
        entriesRemoveTweetDetailRelatedTweets()
    }

    fun hideRecommendedUsers(json: JSONObject) {
        json.jsonCheckAndRemoveRecommendedUsers()
    }

    fun hidePromotedAds(json: JSONObject) {
        json.jsonGetInstructions()?.forEach { instruction ->
            instruction.instructionCheckAndRemove()
        }
        json.jsonGetData()?.dataCheckAndRemove()
    }
}