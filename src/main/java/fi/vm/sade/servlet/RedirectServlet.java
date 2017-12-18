package fi.vm.sade.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {

    private static final String PARAMETER_TO = "to";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String redirect = req.getParameter(PARAMETER_TO);
        if (redirect == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "to-parameter is required");
            return;
        }
        resp.sendRedirect(redirect);
    }

}
