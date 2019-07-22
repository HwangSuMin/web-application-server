package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            // index.html 응답 1단계
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
            log.debug("request line : {}", line);

            if (line == null) {
                return;
            }

            String[] tokens = line.split(" ");
            String method = tokens[0];
            String url = tokens[1];
            int contentLength = 0;
            boolean logined = false;

            while (!line.equals("")) {
                line = br.readLine();
                log.debug("header : {}", line);

                if (line.contains("Content-Length")) {
                    contentLength = getContentLength(line);
                }
                else if (line.contains("Cookie")) {
                    logined = isLogin(line);
                }
            }

//            if (url.startsWith("/user/create") && method.equals("GET")) {
//                String queryString = url.substring(url.indexOf("?") + 1);
//                Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
//                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
//                log.debug("User : {}", user);
//            }
            if (url.startsWith("/user/create") && method.equals("POST")) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                log.debug("User : {}", user);
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }
            else if (url.startsWith("/user/login") && method.equals("POST")) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = DataBase.findUserById(params.get("userId"));
                boolean isSuccess = (user != null && user.getPassword().equals(params.get("password")));
                log.debug("Login : {}", isSuccess);
                DataOutputStream dos = new DataOutputStream(out);
                if (isSuccess) {
                    response302LoginSuccessHeader(dos);
                }
                else {
                    responseResource(dos, "/user/login_failed.html");
                }
            }
            else if (url.startsWith("/user/list") && method.equals("GET")) {
                if (!logined) {
                    responseResource(out, "/user/login.html");
                    return;
                }
                DataOutputStream dos = new DataOutputStream(out);
                StringBuilder userList = new StringBuilder();
                Collection<User> users = DataBase.findAll();
                userList.append("<table border='1'>");
                for (User user : users) {
                    userList.append("<tr>")
                            .append("<td>")
                            .append(user.getUserId())
                            .append("</td>")
                            .append("<td>")
                            .append(user.getName())
                            .append("</td>")
                            .append("<td>")
                            .append(user.getEmail())
                            .append("</td>")
                            .append("</tr>");
                }
                userList.append("</table>");
                byte[] body = userList.toString().getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
            else if (url.endsWith(".css")) {
                DataOutputStream dos = new DataOutputStream(out);
                File requestFile = new File("./webapp" + url);
                byte[] body = Files.readAllBytes(requestFile.toPath());
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
            }
            else {
                responseResource(out, url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        File requestFile = new File("./webapp" + url);
        byte[] body = Files.readAllBytes(requestFile.toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8 \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css \r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}