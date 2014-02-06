package no.javazone.cake.redux;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataServletTest {
    private DataServlet servlet = new DataServlet();

    @Test
    public void shouldGiveListOfAllEvents() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("GET");
        when(req.getPathInfo()).thenReturn("/events");

        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter jsonResult = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonResult));

        EmsCommunicator emsCommunicator = mock(EmsCommunicator.class);
        when(emsCommunicator.allEvents()).thenReturn("This is a json");

        servlet.setEmsCommunicator(emsCommunicator);

        servlet.service(req,resp);

        verify(resp).setContentType("text/json");
        verify(emsCommunicator).allEvents();

        assertThat(jsonResult.toString()).isEqualTo("This is a json");

    }
}
