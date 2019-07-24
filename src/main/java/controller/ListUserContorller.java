package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

import java.util.Collection;
import java.util.Map;

/**
 * Created by sumin on 2019-07-24.
 **/
public class ListUserContorller extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ListUserContorller.class);

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        if (!isLogin(request.getHeader("Cookie"))) {
            response.sendRedirect("/user/login.html");
            return;
        }

        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        response.forwardBody(sb.toString());
    }

    private boolean isLogin(String cookie) {
        log.error("TEst-cookie: {}", cookie);
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie);
        return Boolean.parseBoolean(cookies.getOrDefault("logined", "false"));
    }
}
