package no.javazone.cake.redux

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.time.LocalDateTime

class FeedbackReaderTest {
    @Test
    fun shouldParseRoom() {
        val (room:String,slot:LocalDateTime) = FeedbackReader.readRoomSlot("Room 2 Wed 09:00")
        assertThat(room).isEqualTo("Room 2")

    }
}