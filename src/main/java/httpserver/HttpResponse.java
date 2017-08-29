package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ghost on 29.08.2017.
 */
class HttpResponse {

    private static final String VERSION = "HTTP/1.1";
    private List<String> headers = new ArrayList<>();
    private byte[] body;

    HttpResponse(HttpRequest request) {
        switch (request.getMethod()) {
            case HEAD:
                fillHeaders(Status._200);
                break;

            case GET:
                try {
                    // TODO fix dir bug http://localhost:8080/src/test
                    String path = request.getPath().equals("/") ? "/index.html" : request.getPath();

                    fillHeaders(Status._200);
                    setContentType(path);
                    fillResponse(FileReader.readFile(path));


                } catch (FileNotFoundException noFile) {
                    fillHeaders(Status._404);
                    fillResponse(Status._404.toByteArray());
                } catch (Exception e) {
                    fillHeaders(Status._400);
                    fillResponse(Status._400.toByteArray());
                }
                break;

            case UNRECOGNIZED:
                fillHeaders(Status._400);
                fillResponse(Status._400.toByteArray());
                break;

            default:
                fillHeaders(Status._501);
                fillResponse(Status._501.toByteArray());
        }
    }

    byte[] getResponse() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        for (String header : headers) {
            outStream.write(header.getBytes());
            outStream.write("\r\n".getBytes());
        }
        if (body != null) {
            outStream.write(body);
            outStream.write("\r\n".getBytes());
        }
        return outStream.toByteArray();
    }

    private void fillHeaders(Status status) {
        headers.add(HttpResponse.VERSION + " " + status.toString());
        headers.add("Connection: close");
        headers.add("Server: SimpleWebServer");
    }

    private void fillResponse(byte[] response) {
        body = Arrays.copyOf(response, response.length);
    }

    private void setContentType(String uri) {
        try {
            String ext = uri.substring(uri.lastIndexOf(".") + 1);
            headers.add(ContentType.valueOf(ext.toUpperCase()).toString());
        } catch (Exception e) {
            System.out.println("ContentType not found: " + e.toString());
        }
    }
}