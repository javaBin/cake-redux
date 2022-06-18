package no.javazone.cake.redux

import no.javazone.cake.redux.comments.FeedbackType
import org.assertj.core.api.Assertions
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.junit.Test

class TestrPkomCompute {
    private fun buildInput(feedbacksGiven:List<String>):JsonObject {
        val ratings:List<JsonObject> = feedbacksGiven.map { JsonObject().put("feedbacktype",FeedbackType.TALK_RATING.name).put("info",it) }
        return JsonObject().put("data",
            JsonObject().put("pkomfeedbacks",
                JsonObject().put("value",JsonArray.fromNodeList(ratings))))
    }

    @Test
    fun emptyIsNothing() {
        Assertions.assertThat(PkomFeedbackCompute.compute(JsonObject())).isEqualTo("")
        Assertions.assertThat(PkomFeedbackCompute.compute(JsonObject().put("data",JsonObject()))).isEqualTo("")
    }

    @Test
    fun testWithOne() {
        Assertions.assertThat(PkomFeedbackCompute.compute(buildInput(listOf("+")))).isEqualTo("4000 (1)")
    }

    @Test
    fun testWithTwo() {
        Assertions.assertThat(PkomFeedbackCompute.compute(buildInput(listOf("-","+")))).isEqualTo("3000 (2)")
    }
}