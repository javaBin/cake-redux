package no.javazone.cake.redux;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AcceptorSetterTest {
    private AcceptorSetter acceptorSetter = new AcceptorSetter(null);

    @Test
    public void shouldGenerateMessage() throws Exception {
        assertThat(acceptorSetter.generateMessage("Hello", null, null, null, null, null)).isEqualTo("Hello");
    }

    @Test
    public void shouldInsertTitle() throws Exception {
        assertThat(acceptorSetter.generateMessage("Hello #title# something", "My title", null, null, null, null)).isEqualTo("Hello My title something");
        assertThat(acceptorSetter.generateMessage("Hello #title# something from #speakername#", "My title", null, "Darth Vader", null, null)).isEqualTo("Hello My title something from Darth Vader");
    }

    @Test
    public void shouldHandleSpecialCharacters() throws Exception {
        assertThat(acceptorSetter.generateMessage("Hello #title# something from #speakername#", "My $title", null, "Darth Vader", null, null)).isEqualTo("Hello My $title something from Darth Vader");
    }
}
