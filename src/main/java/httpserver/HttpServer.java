package httpserver;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;


public class HttpServer {

    /*
        Main class for starting the server.
     */
    private final static int DEFAULT_PORT = 8080;
    private static Selector selector;

    private final Map<SocketChannel, List<ByteBuffer>> pendingSent = new HashMap<>();
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    private HttpServer() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.socket().bind(new InetSocketAddress(DEFAULT_PORT));

        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.run();
    }

    public static void main(String... args) {
        try {
            new HttpServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        try {
            //noinspection InfiniteLoopStatement
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void send(SocketChannel socket, byte[] data) throws IOException {
        synchronized (pendingSent) {
            List<ByteBuffer> queue = pendingSent.computeIfAbsent(socket, k -> new ArrayList<>());
            queue.add(ByteBuffer.wrap(data));

        }
        socket.register(selector, SelectionKey.OP_WRITE);

        selector.wakeup();
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        synchronized (pendingSent) {
            List<ByteBuffer> queue = pendingSent.get(socketChannel);
            while (!queue.isEmpty()) {
                ByteBuffer buf = queue.get(0);
                socketChannel.write(buf);

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

    private static void accept(SelectionKey key) throws IOException {
        ServerSocketChannel sch = (ServerSocketChannel) key.channel();
        SocketChannel ch = sch.accept();
        ch.configureBlocking(false);
        ch.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        readBuffer.clear();

        if (!isValid(channel)) {
            channel.close();
            key.cancel();
            return;
        }

        readBuffer.flip();
        new SocketProcessor(this, channel, readBuffer).processRequest();



    }

    private boolean isValid(SocketChannel channel) {
        int numRead;

        try {
            numRead = channel.read(readBuffer);
        } catch (IOException e) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (numRead == -1) {
            return false;
        }

        return true;
    }
}