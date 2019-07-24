package controller;

import util.HttpRequest;
import util.HttpResponse;

/**
 * Created by sumin on 2019-07-24.
 **/
public interface Controller {
    void service(HttpRequest request, HttpResponse response);
}
