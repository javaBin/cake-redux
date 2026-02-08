package no.javazone.cake.redux.videoadmin;

import no.javazone.cake.redux.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.StringTokenizer;

public class VideoAdminFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (Configuration.noAuthMode()) {
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        if ("true".equals(request.getSession().getAttribute("basicauthuser"))) {
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        Credentials credentials = credentialsWithBasicAuthentication(request);

        if (credentials == null || !credentials.matches()) {
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"My Realm\"");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
            return;
        }

        request.getSession().setAttribute("basicauthuser","true");
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }

    private static class Credentials {
        public final String login;
        public final String password;

        public Credentials(String login, String password) {
            this.login = login;
            this.password = password;
        }

        public boolean matches() {
            return Configuration.videoAdminPassword().equals(login + ":" + password);
        }
    }

    public Credentials credentialsWithBasicAuthentication(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String login = credentials.substring(0, p).trim();
                            String password = credentials.substring(p + 1).trim();

                            return new Credentials(login,password);
                        }
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }
            }
        }

        return null;
    }

}
