import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SimpleChatServer {

    private final List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        new SimpleChatServer().go();
    }

    public void go() {

        ExecutorService threadPool = Executors.newCachedThreadPool();

        try {

            ServerSocketChannel serverSocketChannel =
                    ServerSocketChannel.open();

            serverSocketChannel.bind(
                    new InetSocketAddress(5000)
            );

            System.out.println("Chat server started on port 5000...");

            while (serverSocketChannel.isOpen()) {

                SocketChannel clientSocket =
                        serverSocketChannel.accept();

                System.out.println("Client connected.");

                PrintWriter writer =
                        new PrintWriter(
                                Channels.newWriter(
                                        clientSocket,
                                        UTF_8
                                )
                        );

                clientWriters.add(writer);

                threadPool.submit(
                        new ClientHandler(clientSocket)
                );
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void tellEveryone(String message) {

        for (PrintWriter writer : clientWriters) {

            writer.println(message);
            writer.flush();
        }
    }

    public class ClientHandler implements Runnable {

        private final BufferedReader reader;
        private final SocketChannel socket;

        public ClientHandler(SocketChannel clientSocket) {

            socket = clientSocket;

            reader =
                    new BufferedReader(
                            Channels.newReader(
                                    socket,
                                    UTF_8
                            )
                    );
        }

        @Override
        public void run() {

            String message;

            try {

                while ((message = reader.readLine()) != null) {

                    System.out.println(
                            "Received: " + message
                    );

                    tellEveryone(message);
                }

            } catch (IOException ex) {

                System.out.println("Client disconnected.");
            }
        }
    }
}