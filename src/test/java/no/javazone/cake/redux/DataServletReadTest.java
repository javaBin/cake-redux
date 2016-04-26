package no.javazone.cake.redux;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataServletReadTest {
    private DataServlet servlet = new DataServlet();
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse resp = mock(HttpServletResponse.class);
    private final StringWriter jsonResult = new StringWriter();
    private final EmsCommunicator emsCommunicator = mock(EmsCommunicator.class);
    private final UserFeedbackCommunicator userFeedbackCommunicator = mock(UserFeedbackCommunicator.class);

    @Before
    public void setUp() throws Exception {
        when(req.getMethod()).thenReturn("GET");
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonResult));
        servlet.setEmsCommunicator(emsCommunicator);
        servlet.setUserFeedbackCommunicator(userFeedbackCommunicator);
    }

    @Test
    public void shouldGiveListOfAllEvents() throws Exception {
        when(req.getPathInfo()).thenReturn("/events");
        when(emsCommunicator.allEvents()).thenReturn("This is a json");

        servlet.service(req, resp);

        verify(resp).setContentType("application/json;charset=UTF-8");
        verify(emsCommunicator).allEvents();

        assertThat(jsonResult.toString()).isEqualTo("This is a json");
    }

    @Test
    public void shouldReadSlotList() throws Exception {
        when(req.getPathInfo()).thenReturn("/talks");
        when(req.getParameter("eventId")).thenReturn("xxx");
        when(emsCommunicator.talkShortVersion(anyString())).thenReturn("This is slot list json");

        servlet.service(req,resp);

        verify(emsCommunicator).talkShortVersion("xxx");
        assertThat(jsonResult.toString()).isEqualTo("This is slot list json");
    }

    @Test
    public void shouldReadSingleTalk() throws Exception {
        Configuration.setProps(Collections.emptyMap());
        when(req.getPathInfo()).thenReturn("/atalk");
        when(req.getParameter("talkId")).thenReturn("zzz");
        JsonObject val = JsonFactory.jsonObject();
        when(emsCommunicator.oneTalkAsJson(anyString())).thenReturn(val);

        servlet.service(req,resp);

        verify(emsCommunicator).oneTalkAsJson("zzz");
        JsonNode parsed = JsonParser.parse(jsonResult.toString());
        assertThat(parsed).isNotNull();
    }

}
