package webserver;

import controller.Controller;
import controller.CreateUserController;
import controller.ListUserContorller;
import controller.LoginController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sumin on 2019-07-24.
 **/
public class RequestMapping {
    private static Map<String, Controller> controllers = new HashMap<>();

    static {
        controllers.put("/user/create", new CreateUserController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/list", new ListUserContorller());
    }

    public static Controller getController(String requestUrl) {
        return controllers.get(requestUrl);
    }
}
