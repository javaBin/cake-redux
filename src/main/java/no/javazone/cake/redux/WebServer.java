package no.javazone.cake.redux;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebServer {

    private final Integer port;
    private String warFile;

    public WebServer(Integer port, String warFile) {
        this.port = port;
        this.warFile = warFile;
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 1) {
            System.out.println("Usage WebServer <Config file name> [war-file-path]");
            return;
        }
        String warFile = null;
        if (args.length > 1) {
            warFile = args[1];
        }
        System.setProperty("cake-redux-config-file",args[0]);
        new WebServer(getPort(8081),warFile).start();
    }

    private void start() throws Exception {
        Server server = new Server(port);
        if (warFile != null) {
            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath("/");
            webAppContext.setWar(warFile);
            server.setHandler(webAppContext);
        } else {
            HandlerList handlerList = new HandlerList();
            handlerList.addHandler(new ShutdownHandler("yablayabla", false, true));
            handlerList.addHandler(new WebAppContext("src/main/webapp", "/"));
            server.setHandler(handlerList);
        }
        server.start();
        System.out.println(server.getURI());
    }

    private static int getPort(int defaultPort) {
        Integer serverPort = Configuration.serverPort();
        return serverPort != null ? serverPort : defaultPort;
    }
}