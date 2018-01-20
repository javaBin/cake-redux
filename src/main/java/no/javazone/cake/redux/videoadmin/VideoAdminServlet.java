package no.javazone.cake.redux.videoadmin;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class VideoAdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        switch (req.getPathInfo()) {
            case "/all":
                JsonArray jsonArray = VideoAdminService.get().all();
                jsonArray.toJson(resp.getWriter());
                break;
            case "/one":
                String id = req.getParameter("id");
                if (id == null || id.trim().isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing paramater id");
                    return;
                }
                JsonObject talkobj = VideoAdminService.get().one(id);
                talkobj.toJson(resp.getWriter());
                return;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/updatevideo".equals(req.getPathInfo())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        JsonObject input;
        try (InputStream inputStream = req.getInputStream()) {
            input = JsonParser.parseToObject(inputStream);
        }
        Optional<String> id = input.stringValue("id");
        Optional<String> video = input.stringValue("video");
        if (!(id.isPresent() && video.isPresent())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Missing input");
            return;
        }
        VideoAdminService.get().update(id.get(),video.get());
        resp.setContentType("application/json;charset=UTF-8");
        JsonFactory.jsonObject().toJson(resp.getWriter());
    }
}
