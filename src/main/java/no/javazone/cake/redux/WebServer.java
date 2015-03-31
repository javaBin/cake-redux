package no.javazone.cake.redux;

import no.javazone.cake.redux.whyda.WhydaServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.EnumSet;

import static javax.servlet.DispatcherType.REQUEST;

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


    private WebAppContext createHandler() {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webAppContext.getSessionHandler().getSessionManager().setMaxInactiveInterval(30);
        webAppContext.setContextPath("/");

        if (isDevEnviroment()) {
            // Development ie running in ide
            webAppContext.setResourceBase("src/main/resources/webapp");
        } else {
            // Prod ie running from jar
            webAppContext.setBaseResource(Resource.newClassPathResource("webapp", true, false));
        }

        webAppContext.addServlet(new ServletHolder(new DataServlet()),"/secured/data/*");
        webAppContext.addServlet(new ServletHolder(new OpenDataServlet()),"/data/*");
        webAppContext.addServlet(new ServletHolder(new SigninServlet()), "/signin/");
        webAppContext.addServlet(new ServletHolder(new EntranceServlet()), "/entrance");
        webAppContext.addServlet(new ServletHolder(new WhydaServlet()),"/whydalogin");

        webAppContext.addFilter(new FilterHolder(new SecurityFilter()), "/secured/*", EnumSet.of(REQUEST));


        return webAppContext;
    }

    private static boolean isDevEnviroment() {
        return new File("pom.xml").exists();
    }


    private void start() throws Exception {
        Server server = new Server(port);
        server.setHandler(createHandler());

        server.start();
        System.out.println(server.getURI());
    }

    private static int getPort(int defaultPort) {
        Integer serverPort = Configuration.serverPort();
        return serverPort != null ? serverPort : defaultPort;
    }
}