package no.javazone.cake.redux;

import no.javazone.cake.redux.videoadmin.VideoAdminFilter;
import no.javazone.cake.redux.videoadmin.VideoAdminServlet;
import no.javazone.cake.redux.whyda.WhydaServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResourceFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;

import static jakarta.servlet.DispatcherType.REQUEST;

public class WebServer {

    private final Integer port;

    public WebServer(Integer port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting cakeredux: " + Arrays.toString(args));
        try {
            if (args != null && args.length > 0) {
                System.setProperty("cake-redux-config-file", args[0]);
            } else {
                System.out.println("No config file specified");
            }
            System.out.println("Enviroment : *" + Configuration.cakeLocation() + "*");
            new WebServer(getPort(8081)).start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.printf("Cakeredux failed " + ex.getMessage());
            throw ex;
        }
    }


    private WebAppContext createHandler() {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webAppContext.getSessionHandler().setMaxInactiveInterval(30);
        webAppContext.setContextPath("/");

        if (isDevEnviroment()) {
            // Development ie running in ide
            webAppContext.setBaseResource(ResourceFactory.root().newResource("src/main/resources/webapp"));
        } else {
            // Prod ie running from jar
            webAppContext.setBaseResource(ResourceFactory.root().newClassLoaderResource("webapp", false));
        }


        webAppContext.addServlet(new ServletHolder(new DataServlet()), "/secured/data/*");
        webAppContext.addServlet(new ServletHolder(new OpenDataServlet()), "/data/*");
        webAppContext.addServlet(new ServletHolder(new SigninServlet()), "/signin/");
        webAppContext.addServlet(new ServletHolder(new EntranceServlet()), "/entrance");
        webAppContext.addServlet(new ServletHolder(new WhydaServlet()), "/whydalogin");
        webAppContext.addServlet(new ServletHolder(new SlackServlet()), "/slack/signin");


        webAppContext.addFilter(new FilterHolder(new SecurityFilter()), "/secured/*", EnumSet.of(REQUEST));

        webAppContext.addFilter(new FilterHolder(new VideoAdminFilter()), "/videoadmin/*", EnumSet.of(REQUEST));
        webAppContext.addServlet(new ServletHolder(new VideoAdminServlet()), "/videoadmin/api/*");


        return webAppContext;
    }

    private static boolean isDevEnviroment() {
        return new File("pom.xml").exists();
    }


    private void start() throws Exception {
        Server server = new Server(port);
        server.setHandler(createHandler());

        server.start();
        System.out.println("Cakeredux started port " + port);
    }

    private static int getPort(int defaultPort) {
        Integer serverPort = Configuration.serverPort();
        return serverPort != null ? serverPort : defaultPort;
    }
}