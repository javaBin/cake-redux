package no.javazone.cake.redux;

import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonFactory;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataServletWriteTest {
    private DataServlet servlet = new DataServlet();
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse resp = mock(HttpServletResponse.class);
    private final StringWriter jsonResult = new StringWriter();
    private final SleepingpillCommunicator sleepingpillCommunicator = mock(SleepingpillCommunicator.class);


    @Before
    public void setUp() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonResult));

        HttpSession mockSession = mock(HttpSession.class);
        when(req.getSession()).thenReturn(mockSession);
        servlet.setSleepingpillCommunicator(sleepingpillCommunicator);
    }

    @Test
    public void shouldSaveTags() throws Exception {
        when(req.getPathInfo()).thenReturn("/editTalk");
        String inputjson = JsonFactory.jsonObject()
                .put("ref","abra")
                .put("lastModified","Tue, 04 Feb 2014 23:55:06 GMT")
                .put("state","pending")
                .put("tags",JsonFactory.jsonArray().add("test"))
                .put("keywords",JsonFactory.jsonArray().add("keyone"))
                .toJson();
        //String inputjson = "{\"ref\":\"abra\",\"lastModified\":\"Tue, 04 Feb 2014 23:55:06 GMT\",\"tags\":[\"test\"],\"state\":\"pending\"}";
        mockInputStream(inputjson);

        servlet.service(req,resp);

        verify(sleepingpillCommunicator).update("abra", Arrays.asList("test"),Arrays.asList("keyone"),"pending","Tue, 04 Feb 2014 23:55:06 GMT",UserAccessType.WRITE);

    }

    private void mockInputStream(String inputjson) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(inputjson.getBytes("UTF-8"));
        when(req.getInputStream()).thenReturn(new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
            }
        });
    }

}
