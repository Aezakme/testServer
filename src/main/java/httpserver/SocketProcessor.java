package httpserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

/*
    Class for providing socket management
*/
class SocketProcessor  {

    private HttpServer server;
    private SocketChannel channel;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;


    SocketProcessor(HttpServer server, SocketChannel channel, ByteBuffer readBuffer) {
        this.server = server;
        this.channel = channel;
        this.readBuffer = readBuffer;
    }

    /*
        Run method for new thread
     */
    void processRequest() {
        byte[] response = mapRequest(getHttpRequest());
        writeResponse(response);
    }

    /*
        Constructing response by request
     */
    private byte[] mapRequest(HttpRequest httpRequest) {
        HttpResponse response = new HttpResponse(httpRequest);
        try {
            return response.getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeResponse(byte[] b) {


        writeBuffer = ByteBuffer.wrap(b);
        try {
            server.send(channel, b);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private HttpRequest getHttpRequest() {

        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer decodeBuffer = null;
        String reply = null;

        try {
            decodeBuffer = decoder.decode(readBuffer);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        if (decodeBuffer != null) {
            reply = decodeBuffer.toString();
//            System.out.println(reply);
        }
        return parseRequestLine(reply);
    }

    private HttpRequest parseRequestLine(String requestString) {
        String[] temp = requestString.split(" ");
//        System.out.print(temp[1]+" from :");
//        System.out.println(temp[3].split("\n")[0]);

//        System.out.println(temp[3] + ": " + temp[1]);
        Map<String, String> params = getParams(requestString.substring(requestString.indexOf("\n") + 1));
        String body = requestString.substring(requestString.indexOf("\r\n\r\n"));
        HttpMethod method = HttpMethod.valueOf(temp[0]);
        String path = temp[1];
        System.out.println(path);
        String version = temp[2];
        return new HttpRequest(method, path, version, params, body);
    }

    /*
        Getting parameters from request
     */
    private Map<String, String> getParams(String string) {
//        System.out.println(string);
        String[] temp = string.split(":\\s|\r\n");
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < temp.length; i++) {
            params.put(temp[i], temp[i + 1]);
            i++;
        }
        return params;
    }

    ByteBuffer getWriteBuffer() {

        return writeBuffer;
    }
}