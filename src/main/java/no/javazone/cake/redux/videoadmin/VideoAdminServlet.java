package no.javazone.cake.redux.videoadmin;

import org.jsonbuddy.JsonArray;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VideoAdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        switch (req.getPathInfo()) {
            case "/all":
                JsonArray jsonArray = VideoAdminService.get().all();
                jsonArray.toJson(resp.getWriter());
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
