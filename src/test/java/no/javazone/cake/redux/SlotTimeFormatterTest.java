package no.javazone.cake.redux;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SlotTimeFormatterTest {
    @Test
    public void shouldReturnFormattedSlots() throws Exception {
        SlotTimeFormatter slotTimeFormatter = new SlotTimeFormatter("2014-09-09T07:00:00Z+2014-09-09T11:00:00Z");
        assertThat(slotTimeFormatter.getStart()).isEqualTo("140909 09:00");
        assertThat(slotTimeFormatter.getEnd()).isEqualTo("140909 13:00");
        assertThat(slotTimeFormatter.getLength()).isEqualTo(240);
    }


}
