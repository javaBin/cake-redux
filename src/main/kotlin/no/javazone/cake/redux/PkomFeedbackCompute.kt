package no.javazone.cake.redux

import no.javazone.cake.redux.comments.FeedbackType
import no.javazone.cake.redux.comments.Rating
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject

object PkomFeedbackCompute {
    private fun JsonObject.objectOpt(key:String):JsonObject? = this.objectValue(key).orElse(null)
    private fun JsonObject.stringOpt(key:String):String? = this.stringValue(key).orElse(null)
    private fun JsonObject.arrayOpt(key:String):JsonArray? = this.arrayValue(key).orElse(null)

    fun compute(input:JsonObject):String {
        val allRatingsJson:List<JsonObject> = input.objectOpt("data")?.objectOpt("pkomfeedbacks")?.arrayOpt("value")?.objects { it }?.filter { it.stringOpt("feedbacktype") == FeedbackType.TALK_RATING.name }?:return ""

        if (allRatingsJson.isEmpty()) {
            return ""
        }
        val allRatings:List<Rating> = allRatingsJson.map { Rating.fromText(it.requiredString("info")) }

        val sum = allRatings.sumBy { it.ratingValue }
        val count = allRatings.size

        val mean:Int = sum / count

        return "$mean ($count)"
    }
}