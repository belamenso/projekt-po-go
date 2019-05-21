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
    private Socket socket;
    private Server server;

    ServerClient(Socket socket, Server server) {
        this.id = ids ++;
        this.socket = socket;
        this.server = server;

        Thread clientThread = new Thread(() -> {
            try {
                server.clients.add(this);

                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                setListener(server.defaultListener);
                listener.clientConnected(this);
                Thread.currentThread().setName("Thread for " + this);

                while (server.open) {
                    try {
                        Message msg = null;
                        try {
                            msg = (Message) inputStream.readObject();
                        } catch (ClassNotFoundException e) { e.printStackTrace(); } // Powinno byc niemożliwe

                        if (msg == null) throw new IOException();

                        listener.receivedInput(this, msg);

                    } catch (IOException e) {
                        listener.clientDisconnected(this);

                        close();

                        return;
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

            close();
        });

        clientThread.setDaemon(true);
        clientThread.start();
    }

    void close()
    {
        try {
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

    /*void sendMessage(String message) {
        sendMessage(new Message(message));
    }*/

    void sendMessage(Message message) {
        try {
            outputStream.reset();
            outputStream.writeObject(message);
            outputStream.flush();
        } catch(Exception e) { /* e.printStackTrace(); */ close(); }
    }

    @Override
    public String toString() {
        return "client " + id + " @ " + this.listener;
    }
}
