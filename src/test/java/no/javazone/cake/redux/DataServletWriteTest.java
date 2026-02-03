package no.javazone.cake.redux;

import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;
import org.jsonbuddy.JsonFactory;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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


    private static class MockServletStream extends ServletInputStream {
        private final ByteArrayInputStream myStream;
        private boolean finished = false;
        public MockServletStream(String content) throws IOException {
             myStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }

        @Override
        public int read() throws IOException {
            int read = myStream.read();
            finished = (read == -1);
            return read;
        }
    }

    private void mockInputStream(String inputjson) throws IOException {
        when(req.getInputStream()).thenReturn(new MockServletStream(inputjson));
    }

}
