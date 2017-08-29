package httpserver;

import java.util.Map;

class HttpRequest {

    private HttpMethod method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private String body;

    HttpRequest(HttpMethod httpMethod, String path, String version, Map<String, String> headers, String body) {
        this.method = httpMethod;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    HttpMethod getMethod() {
        return method;
    }

    String getVersion() {
        return version;
    }

    String getPath() {
        return path;
    }

    Map<String, String> getHeaders() {
        return headers;
    }

    String getBody() {
        return body;
    }
}
