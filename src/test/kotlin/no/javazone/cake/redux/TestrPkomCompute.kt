package no.javazone.cake.redux

import no.javazone.cake.redux.comments.FeedbackType
import org.assertj.core.api.Assertions
import org.jsonbuddy.JsonArray
import org.jsonbuddy.JsonObject
import org.jsonbuddy.parse.JsonParser
import org.junit.Test
import java.util.UUID

class TestrPkomCompute {
    private fun buildInput(feedbacksGiven:List<String>):JsonObject {
        val ratings:List<JsonObject> = feedbacksGiven.map { JsonObject().put("feedbacktype",FeedbackType.TALK_RATING.name).put("info",it).put("author",UUID.randomUUID().toString()).put("created", "20220531124949") }
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

    @Test
    fun filterDuplicates() {
        val jsonVal:JsonObject = JsonParser.parseToObject("""
            {
                "data": {
                    "pkomfeedbacks": {
                        "privateData": true,
                        "value": [
                            {
                                "author": "Ole",
                                "created": "20220531124949",
                                "id": "58c66028-0ebb-4760-be17-5f0eedc7a766",
                                "talkid": "687d741e-7d95-48a3-8cf8-6afd1b102fba",
                                "feedbacktype": "TALK_RATING",
                                "info": "-"
                            },
                            {
                                "author": "Dole",
                                "created": "20220614175811",
                                "feedbacktype": "TALK_RATING",
                                "info": "-"
                            },
                            {
                                "author": "Doffen",
                                "created": "20220618095650",
                                "feedbacktype": "TALK_RATING",
                                "info": "0"
                            },
                            {
                                "author": "Doffen",
                                "created": "20220618095731",
                                "feedbacktype": "TALK_RATING",
                                "info": "-"
                            }
                        ]
                    }
                }
            }
        """.trimIndent())
        Assertions.assertThat(PkomFeedbackCompute.compute(jsonVal)).isEqualTo("2000 (3)")
    }
}