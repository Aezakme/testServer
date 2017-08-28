package com.test.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class RequestHandler {

    private ByteBuffer readBuffer;

    private static final String RESPONSE =
            "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/%s\r\n" +

                    "Content-Length:%d \r\n" +
                    "Connection: close\r\n\r\n";

    private RequestWaiter server;
    private String rootPath;

    RequestHandler(RequestWaiter server, String rootPath) {
        this.server = server;
        this.rootPath = rootPath;
        readBuffer = ByteBuffer.allocate(1024);
    }

    void sendPage(SocketChannel channel, byte[] message, int length) throws IOException {
        readBuffer.clear();
        channel.read(readBuffer);
        readBuffer.flip();

        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer decodeBuffer = decoder.decode(ByteBuffer.wrap(message));
        String[] pathAndType = parsePage(decodeBuffer.subSequence(0, length));

        server.send(channel, readFile(pathAndType[0], pathAndType[1]));
    }


    private String[] parsePage(CharBuffer charBuffer) {

        String[] temp = charBuffer.toString().split(" ");
        String[] ret = new String[2];
        ret[0] = (temp[1].equals("/")) ? "/index.html" : temp[1];
        System.out.println(ret[0]);
        temp = ret[0].split("\\.");
        System.out.println(temp[temp.length - 1] + "\n");
        ret[1] = temp[temp.length - 1];
        return ret;

    }

    private byte[] readFile(String path, String type) throws IOException {

        StringBuilder returnString = new StringBuilder();
        try {
            BufferedReader input = new BufferedReader(new FileReader(rootPath + path));
            String s;
            try {
                while ((s = input.readLine()) != null) {
                    returnString.append(s);
                    returnString.append("\n");
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        } catch (FileNotFoundException e) {
            //TODO make 404 page
        }
        returnString.insert(0, String.format(RESPONSE, type, returnString.length()));
        return returnString.toString().getBytes();
    }


}
