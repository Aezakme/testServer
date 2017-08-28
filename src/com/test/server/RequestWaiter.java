package com.test.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

public class RequestWaiter {

    private final int PORT = 8080;

    private RequestHandler requestHandler;

    private List<ChangeRequest> changeRequests = new LinkedList<>();
    private final Map<SocketChannel, List<ByteBuffer>> pendingSent = new HashMap<>();


    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    private RequestWaiter() throws IOException {

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(PORT));
        selector = SelectorProvider.provider().openSelector();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        requestHandler = new RequestHandler(this, "html");

        this.run();

    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void run() throws IOException {
        while (true) {
            selector.select();
            Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();

            while (selectionKeys.hasNext()) {
                SelectionKey key = selectionKeys.next();
                selectionKeys.remove();
                if (!key.isValid()) continue;
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        readBuffer.clear();

        int numRead;
        try {
            numRead = channel.read(readBuffer);

        } catch (IOException e) {
            // the remote forcibly closed the connection
            key.cancel();
            channel.close();
            return;
        }

        if (numRead == -1) {
            // remote entity shut the socket down cleanly.
            channel.close();
            key.cancel();
            return;
        }
        requestHandler.sendPage(channel, readBuffer.array(), numRead);
        channel.register(selector, SelectionKey.OP_WRITE);

    }


    public static void main(String[] args) {
        try {
            new RequestWaiter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void send(SocketChannel socket, byte[] data) throws IOException {
        synchronized (changeRequests) {
            changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
            synchronized (pendingSent) {
                List<ByteBuffer> queue = pendingSent.get(socket);
                if (queue == null) {
                    queue = new ArrayList<ByteBuffer>();
                    pendingSent.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }
        selector.wakeup();
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        synchronized (pendingSent) {
            List<ByteBuffer> queue = pendingSent.get(socketChannel);
            while (!queue.isEmpty()) {
                ByteBuffer buf = queue.get(0);
                socketChannel.write(buf);
                // have more to send
                if (buf.remaining() > 0) {
                    break;
                }
                queue.remove(0);
            }
            if (queue.isEmpty()) {
                socketChannel.close();
            }
        }
    }
}
