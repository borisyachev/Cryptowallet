package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.client;

import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.errorhandling.CryptoLogger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.Scanner;

public class CryptoClient implements Runnable {

    private static final int SERVER_PORT = 2222;
    private static final String SERVER_HOST = "localhost";
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

    public static boolean serverRunning = false;

    @Override
    public void run() {

        try (SocketChannel clientChannel = SocketChannel.open();
                Scanner scanner = new Scanner(System.in)) {

            clientChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");
            serverRunning = true;

            while (true) {
                System.out.print("Enter a service(use command <help> if you need detailed information): ");
                String message = scanner.nextLine();

                if (message.equals("quit")) {
                    break;
                }

                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                clientChannel.write(buffer);

                System.out.println("Sending request <"
                        + message
                        + "> to our cryptocurrency server..."
                        + System.lineSeparator());

                buffer.clear();
                clientChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);

                String reply = new String(byteArray, "UTF-8");

                System.out.println("Cryptocurrency server: " + reply);
            }
        } catch (Exception e) {
            String errorTextForClient = "Sorry for the inconvenience, but there "
                    + "is an error with the server connection. Contact our support "
                    + "team by sending an email and attach the log file with a name "
                    + "<clientLogFile.txt>.";
            System.out.println(errorTextForClient);
            CryptoLogger.logClientError(e);
        }
    }



    public static void main(String... args) {
        CryptoClient cryptoClient = new CryptoClient();
        new Thread(cryptoClient).start();

    }
}
