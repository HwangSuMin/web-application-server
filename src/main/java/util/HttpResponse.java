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
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private DataOutputStream dos;
    private Map<String, String> headers;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
        this.headers = new HashMap<>();
    }

    public void forward(String path) {
        try {
            this.dos.writeBytes("HTTP/1.1 200 OK \r\n");
            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
            this.addHeader("Content-Length", String.valueOf(body.length));
            responseHeaders();
            this.dos.write(body, 0, body.length);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String path) {
        try {
            this.dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            this.addHeader("Location", path);
            responseHeaders();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseHeaders() {
        try {
            for (Map.Entry<String, String> headerValue : headers.entrySet()) {
                this.dos.writeBytes(headerValue.getKey() + ": " + headerValue.getValue() + " \r\n");
            }
            this.dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addHeader(String header, String value) {
        this.headers.put(header, value);
    }
}
