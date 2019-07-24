package util;

/**
 * Created by sumin on 2019-07-24.
 **/
public enum HttpMethod {
    GET,
    POST;

    public boolean isPost() {
        return this == POST;
    }

    public boolean isGet() {
        return this == GET;
    }
}
