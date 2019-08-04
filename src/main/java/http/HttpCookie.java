package http;

import util.HttpRequestUtils;

import java.util.Map;

/**
 * Created by sumin on 2019-08-04.
 **/
public class HttpCookie {
    private Map<String, String> cookies;

    public HttpCookie(String cookieValue) {
        this.cookies = HttpRequestUtils.parseCookies(cookieValue);
    }

    public String getCookie(String name) {
        return cookies.get(name);
    }
}