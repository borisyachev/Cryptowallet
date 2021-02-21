package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.server;

import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.command.CommandHandler;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.command.CommandParser;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.errorhandling.CryptoLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;

import java.net.InetSocketAddress;

import java.util.Iterator;
import java.util.Set;

public class CryptoServer implements Runnable {

    private static final String HOST_NAME = "localhost";
    private static int PORT = 2222;

    private ByteBuffer buffer;
    private static final int BUFFER_SIZE = 2048;
    private Selector selector;

    private CommandHandler commandHandler;
    private boolean isServerWorking;

    private static String FILE_TO_LOG = "serverConnectionLogs.txt";

    public CryptoServer(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port given.");
        }
        PORT = port;
        initializeCommandHandler();
    }


    @Override
    public void run() {
        start();
    }

    public void start() {

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientMsg(clientChannel);
                            System.out.println(clientInput);
                            if (clientInput == null) {
                                continue;
                            }
                            clientInput = clientInput.replace(System.lineSeparator(), "");
                            String serverMsg = commandHandler
                                    .getServerMsg(CommandParser.newCommand(clientInput), clientChannel);

                            sendServerMsg(clientChannel, serverMsg);

                        } else if (key.isAcceptable()) {
                            acceptChannel(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    CryptoLogger.logServerError(e,
                            "Error occurred while processing client request: ",
                            FILE_TO_LOG);
                }
            }
        } catch (IOException e) {
            CryptoLogger.logServerError(e,
                    "Failed to start the crypto server.",
                    FILE_TO_LOG);
        }
    }


    public void stop() {
        isServerWorking = false;
        Set<SocketChannel> socketsToClose = commandHandler.closeServer();
        String serverShuttingDownMsg = "disconnected";
        for (SocketChannel sc : socketsToClose) {
            try {
                sendServerMsg(sc, serverShuttingDownMsg);
                sc.close();
            } catch (IOException e) {
                CryptoLogger.logServerError(e,
                        "Error disconnecting a channel.",
                        FILE_TO_LOG);
            }
        }
        try {
            selector.close();
        } catch (IOException e) {
            CryptoLogger.logServerError(e,
                    "Unexpected exception while closing the selector.",
                    FILE_TO_LOG);
        }
    }

    private void sendServerMsg(SocketChannel socketChannel, String msg) throws IOException {
        buffer.clear();
        buffer.put(msg.getBytes());

        buffer.flip();
        socketChannel.write(buffer);
    }

    private String getClientMsg(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST_NAME, PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void acceptChannel(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void initializeCommandHandler() {
        File checkForData = new File("userdata.bin");
        if (checkForData.isFile()) {
            try (FileInputStream fileInputStream = new FileInputStream(checkForData)) {
                commandHandler = new CommandHandler(fileInputStream);
            } catch (IOException | ClassNotFoundException e) {
                CryptoLogger.logServerError(e,
                        "Can't load userdata from file.",
                        FILE_TO_LOG);
            }
        } else {
            commandHandler = new CommandHandler();
        }
    }

    public static void main(String... args) {

      CryptoServer cryptoServer = new CryptoServer(2222);
      Thread serverThread = new Thread(cryptoServer);

      serverThread.start();
        try {
            Thread.sleep(10 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            cryptoServer.stop();
        }
    }
}
