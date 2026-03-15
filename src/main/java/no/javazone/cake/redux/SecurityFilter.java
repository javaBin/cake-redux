package no.javazone.cake.redux;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

public class SecurityFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (Configuration.noAuthMode()) {
            ((HttpServletRequest) req).setAttribute("username", "Dummyuser");
            chain.doFilter(req, resp);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) req;
        Optional<String> username = JwtUtil.getUsernameFromRequest(request);
        if (username.isPresent()) {
            request.setAttribute("username", username.get());
            chain.doFilter(req, resp);
            return;
        }
        resp.setContentType("text/html");
        resp.getWriter().append("You are not logged in. Blocked.");

    }


    @Override
    public void destroy() {

    }
}
