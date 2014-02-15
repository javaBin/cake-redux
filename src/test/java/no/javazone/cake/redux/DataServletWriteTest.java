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
        String inputjson = "{\"ref\":\"abra\",\"lastModified\":\"Tue, 04 Feb 2014 23:55:06 GMT\",\"tags\":[\"test\"]}";
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

        servlet.service(req,resp);

        verify(emsCommunicator).updateTags("abra", Arrays.asList("test"));

    }
}
