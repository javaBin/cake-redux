package no.javazone.cake.redux;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AcceptorSetterTest {
    private AcceptorSetter acceptorSetter = new AcceptorSetter(null);

    @Test
    public void shouldGenerateMessage() throws Exception {
        assertThat(acceptorSetter.generateMessage("Hello", null, null, null, null, null, JsonFactory.jsonObject())).isEqualTo("Hello");
    }

    @Test
    public void shouldInsertTitle() throws Exception {
        assertThat(acceptorSetter.generateMessage("Hello #title# something", "My title", null, null, null, null,JsonFactory.jsonObject())).isEqualTo("Hello My title something");
        assertThat(acceptorSetter.generateMessage("Hello #title# something from #speakername#", "My title", null, "Darth Vader", null, null,JsonFactory.jsonObject())).isEqualTo("Hello My title something from Darth Vader");
    }

    @Test
    public void shouldHandleSpecialCharacters() throws Exception {
        assertThat(acceptorSetter.generateMessage("Hello #title# something from #speakername#", "My $title", null, "Darth Vader", null, null,JsonFactory.jsonObject())).isEqualTo("Hello My $title something from Darth Vader");
    }

    @Test
    public void shouldHandleOtherValues() throws Exception {
        JsonObject jsonTalk = JsonFactory.jsonObject().put("dummy","The slot");
        String message = acceptorSetter.generateMessage("Hello #title# something #dummy# from #speakername#", "My $title", null, "Darth Vader", null, null, jsonTalk);
        assertThat(message).isEqualTo("Hello My $title something The slot from Darth Vader");
    }

    @Test
    public void shouldHandleSlot() throws Exception {
        JsonObject roomObj = JsonParser.parseToObject("{\"ref\":\"dgdg\",\"name\":\"Room 5\"}");
        JsonObject slotObj = JsonParser.parseToObject("{\"ref\":\"dgdg\",\"start\":\"160907 13:00\",\"end\":\"160907 13:20\"}");
        JsonObject jsonTalk = JsonFactory.jsonObject().put("slot",slotObj);
        String message = acceptorSetter.generateMessage("This is #slot# hoi", null, null, null, null, null, jsonTalk);
        assertThat(message).isEqualTo("This is September 7 at 13:00 hoi");

    }

    @Test
    public void shouldHandleRoom() throws Exception {
        JsonObject roomObj = JsonParser.parseToObject("{\"ref\":\"dgdg\",\"name\":\"Room 5\"}");
        JsonObject jsonTalk = JsonFactory.jsonObject().put("room",roomObj);
        String message = acceptorSetter.generateMessage("This is #room# hoi", null, null, null, null, null, jsonTalk);
        assertThat(message).isEqualTo("This is Room 5 hoi");

    }
}

