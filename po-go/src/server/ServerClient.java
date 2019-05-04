package server;

import java.io.PrintWriter;
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
    private final PrintWriter out;
    private final int id;
    ServerListener listener;

    ServerClient(InetAddress ip, int port, PrintWriter out, ServerListener listener) {
        this.ip = ip;
        this.port = port;
        this.out = out;
        this.id = ids ++;
        this.listener = listener;
    }

    void setListener(ServerListener listener) {
        System.out.println(this + " changing listener to " + listener);
        this.listener = listener;
    }

    void sendMessage(String message) {
        try {
            out.println(message);
        } catch(Exception e) { e.printStackTrace(); }
    }

    @Override
    public String toString() {
        return "client " + id + " @ " + this.listener;
    } //+ " @ " + ip.toString() + ":" + port; }
}
