package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sumin on 2019-07-24.
 **/
public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private DataOutputStream dos = null;
    private Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String url) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            if (url.endsWith(".css")) {
                this.addHeader("Content-Type", "text/css");
            }
            else if (url.endsWith(".js")) {
                this.addHeader("Content-Type", "application/javascript");
            }
            else {
                this.addHeader("Content-Type", "text/html;charset=utf-8");
            }
            this.addHeader("Content-Length", String.valueOf(body.length));
            this.response200Header();
            this.responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header() {
        try {
            this.dos.writeBytes("HTTP/1.1 200 OK \r\n");
            this.processHeaders();
            this.dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            this.dos.write(body, 0, body.length);
            this.dos.writeBytes("\r\n");
            this.dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void forwardBody(String body) {
        byte[] contents = body.getBytes();
        this.addHeader("Content-Type", "text/html;charset=utf-8");
        this.addHeader("Content-Length", String.valueOf(contents.length));
        this.response200Header();
        this.responseBody(contents);
    }

    public void sendRedirect(String redirectUrl) {
        try {
            this.dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            this.addHeader("Location", redirectUrl);
            this.processHeaders();
            this.dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders() {
        try {
            for (Map.Entry<String, String> headerValue : headers.entrySet()) {
                this.dos.writeBytes(headerValue.getKey() + ": " + headerValue.getValue() + " \r\n");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
}
