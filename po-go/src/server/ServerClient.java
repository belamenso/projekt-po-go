package server;

import shared.Message;

import java.io.ObjectOutputStream;
import java.net.InetAddress;

/**
 * Prechowuje dane o kliencie
 * umozliwia  wysulanie danych do klienta
 * i zmiane listenera
 */

public class ServerClient {
    private static int ids = 0;

    private final InetAddress ip;
    private final int port;
    private final ObjectOutputStream outputStream;
    private final int id;
    ServerListener listener;

    ServerClient(InetAddress ip, int port, ObjectOutputStream outputStream, ServerListener listener) {
        this.ip = ip;
        this.port = port;
        this.outputStream = outputStream;
        this.id = ids ++;
        this.listener = listener;
    }

    void setListener(ServerListener listener) {
        System.out.println(this + " changing listener to " + listener);
        this.listener = listener;
    }

    void sendMessage(String message) {
        sendMessage(new Message(message));
    }

    void sendMessage(Message message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch(Exception e) { e.printStackTrace(); }
    }

    @Override
    public String toString() {
        return "client " + id + " @ " + this.listener;
    } //+ " @ " + ip.toString() + ":" + port; }
}
