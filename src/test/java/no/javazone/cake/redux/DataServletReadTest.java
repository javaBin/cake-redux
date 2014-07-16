package no.javazone.cake.redux;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataServletReadTest {
    private DataServlet servlet = new DataServlet();
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse resp = mock(HttpServletResponse.class);
    private final StringWriter jsonResult = new StringWriter();
    private final EmsCommunicator emsCommunicator = mock(EmsCommunicator.class);

    @Before
    public void setUp() throws Exception {
        when(req.getMethod()).thenReturn("GET");
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonResult));
        servlet.setEmsCommunicator(emsCommunicator);
    }

    @Test
    public void shouldGiveListOfAllEvents() throws Exception {
        when(req.getPathInfo()).thenReturn("/events");
        when(emsCommunicator.allEvents()).thenReturn("This is a json");

        servlet.service(req, resp);

        verify(resp).setContentType("application/json");
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
        when(req.getPathInfo()).thenReturn("/atalk");
        when(req.getParameter("talkId")).thenReturn("zzz");
        when(emsCommunicator.fetchOneTalk(anyString())).thenReturn("This is single talk json");

        servlet.service(req,resp);

        verify(emsCommunicator).fetchOneTalk("zzz");
        assertThat(jsonResult.toString()).isEqualTo("This is single talk json");

    }

}
