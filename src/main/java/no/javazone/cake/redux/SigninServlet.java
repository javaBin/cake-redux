package no.javazone.cake.redux;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SigninServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        String sessionid = req.getSession().getId();
        // redirect to google for authorization
        StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
                .append("?client_id=").append(Configuration.getGoogleClientId()) // the client id from the api console registration
                .append("&response_type=code")
                .append("&scope=openid%20email") // scope is the api permissions we are requesting
                .append("&redirect_uri=").append(Configuration.getGoogleRedirectUrl()).append("&state=").append(sessionid)
                .append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
                .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in

        resp.sendRedirect(oauthUrl.toString());
    }
}
