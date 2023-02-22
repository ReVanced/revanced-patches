package app.revanced.twitter.patches.hook.patch.recommendation

import app.revanced.twitter.patches.hook.json.BaseJsonHook
import app.revanced.twitter.patches.hook.twifucker.TwiFucker
import org.json.JSONObject


object RecommendedUsersHook : BaseJsonHook() {
    /**
     * Strips JSONObject from recommended users.
     *
     * @param json The JSONObject.
     */
    override fun apply(json: JSONObject) = TwiFucker.hideRecommendedUsers(json)
}