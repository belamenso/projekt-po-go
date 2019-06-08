package server;

import shared.LobbyMsg;
import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Prechowuje dane o kliencie
 * umozliwia  wysulanie danych do klienta
 * i zmiane listenera
 */

public class ServerClient {
    private static int ids = 0;

    private InetAddress ip;
    private int port;
    private ObjectOutputStream outputStream;
    private int id;
    private ServerListener listener;
    private Server server;
    private Socket socket;

    private ServerClient(ObjectOutputStream out, InetAddress ip, int port, Server server, Socket socket) {
        this.id = ids ++;
        this.outputStream = out;
        this.ip = ip;
        this.port = port;
        this.server = server;
        this.socket = socket;
    }

    static void createNewClient(Socket socket, Server server) {
        Thread clientThread = new Thread(() -> {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ServerClient clientInstance = new ServerClient(outputStream, socket.getInetAddress(), socket.getPort(), server, socket);
                clientInstance.setListener(server.defaultListener);
                clientInstance.listener.clientConnected(clientInstance);

                server.clients.add(clientInstance);

                Thread.currentThread().setName("Thread for " + clientInstance);

                while (server.open) {
                    try {
                        Message  msg = (Message) inputStream.readObject();

                        if (msg == null) throw new IOException();

                        clientInstance.listener.receivedInput(clientInstance, msg);

                    } catch (IOException e) {
                        clientInstance.listener.clientDisconnected(clientInstance);

                        clientInstance.close();

                        return;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace(); // Nie powwino się stać
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (!socket.isClosed()) {
                        socket.shutdownOutput();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clientThread.setDaemon(true);
        clientThread.start();
    }

    void close() {
        try {
            outputStream.close();

            if (!socket.isClosed()) {
                socket.shutdownOutput();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.clients.remove(this);
    }

    void setListener(ServerListener listener) {
        System.out.println(this + " changing listener to " + listener);
        this.listener = listener;
    }

    void sendMessage(Message message) {
        try {
            outputStream.reset();
            outputStream.writeObject(message);
            outputStream.flush();
        } catch(Exception e) { /* e.printStackTrace(); */ close(); }
    }

    @Override
    public String toString() {
        return "[C" + id + "@" + ip + ":" + port + "]";
    }
}
