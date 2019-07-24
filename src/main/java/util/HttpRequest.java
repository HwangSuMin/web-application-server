package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sumin on 2019-07-23.
 **/
public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private BufferedReader br;
    private String method;
    private String url;
    private String httpVersion;
    private Map<String, String> headers;
    private Map<String, String> parameters;

    public HttpRequest(InputStream in) {
        this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.parseRequest();
    }

    private void parseRequest() {
        try {
            String line = this.br.readLine();
            if (line == null) {
                return;
            }

            parseRequestLine(line);

            line = this.br.readLine();
            while (!line.equals("")) {
                parseHeader(line);
                line = this.br.readLine();
            }

            if (this.method.equals("POST")) {
                parseBody();
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void parseBody() throws IOException {
        int contentLength = Integer.parseInt(this.headers.getOrDefault("Content-Length", "0"));
        String body = IOUtils.readData(this.br, contentLength);
        this.parameters.putAll(HttpRequestUtils.parseQueryString(body));
    }

    private void parseHeader(String headerLine) {
        log.debug("header : {}", headerLine);
        String[] headerTokens = headerLine.split(": ");
        this.headers.put(headerTokens[0], headerTokens[1]);
    }

    private void parseRequestLine(String requestLine) {
        log.debug("Request Line : {}", requestLine);

        String[] requestTokens = requestLine.split(" ");
        this.method = requestTokens[0];
        this.httpVersion = requestTokens[2];

        int indexOfQueryString = requestTokens[1].indexOf("?");

        if (indexOfQueryString != -1) {
            this.url = requestTokens[1].substring(0, indexOfQueryString);
            this.parameters.putAll(HttpRequestUtils.parseQueryString(requestTokens[1].substring(indexOfQueryString + 1)));
            return;
        }

        this.url = requestTokens[1];
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.url;
    }

    public String getHeader(String headerName) {
        return this.headers.get(headerName);
    }

    public String getParameter(String parameterName) {
        return this.parameters.get(parameterName);
    }
}
