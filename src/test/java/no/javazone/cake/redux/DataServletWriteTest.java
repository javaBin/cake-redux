package no.javazone.cake.redux;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final EmsCommunicator emsCommunicator = mock(EmsCommunicator.class);

    @Before
    public void setUp() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonResult));
        servlet.setEmsCommunicator(emsCommunicator);
    }

    @Test
    public void shouldSaveTags() throws Exception {
        when(req.getPathInfo()).thenReturn("/editTalk");
        String inputjson = "{\"ref\":\"abra\",\"lastModified\":\"Tue, 04 Feb 2014 23:55:06 GMT\",\"tags\":[\"test\"],\"state\":\"pending\"}";
        mockInputStream(inputjson);

        servlet.service(req,resp);

        verify(emsCommunicator).update("abra", Arrays.asList("test"),"pending","Tue, 04 Feb 2014 23:55:06 GMT",UserAccessType.READ_ONLY);

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

    @Test
    public void shouldPublishTalk() throws Exception {
        when(req.getPathInfo()).thenReturn("/publishTalk");
        String inputjson = "{\"ref\":\"abra\",\"lastModified\":\"Tue, 04 Feb 2014 23:55:06 GMT\"}";
        mockInputStream(inputjson);

        servlet.service(req,resp);

        verify(emsCommunicator).publishTalk("abra","Tue, 04 Feb 2014 23:55:06 GMT",UserAccessType.READ_ONLY);


    }
}
