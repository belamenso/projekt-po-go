package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean open;

    private ClientListener listener;

    Client() {
        open = false;
        listener = null;
        socket = null;
        in = null;
        out = null;
    }

    void startConnection(String ip, int port) {
        if (open) return;

        Thread ct = new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 2000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                open = true;
                listener.connectedToServer();

                while (open) {
                    try {
                        String input = in.readLine();

                        if (input == null) {
                            close();
                            listener.disconnected();
                            return;
                        }

                        listener.receivedInput(input);
                    } catch (IOException ex) {
                        listener.serverClosed();
                        close();
                        return;
                    }
                }
            } catch (IOException e) {
                open = false;
                listener.couldNotConnect();
            }
        });

        ct.setName("client thread");
        ct.setDaemon(true);
        ct.start();
    }

    void setListener(ClientListener listener) {
        this.listener = listener;
    }

     void close() {
        if(open) {
            open = false;
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if(in != null) in.close(); } catch (IOException e) { e.printStackTrace(); }
            if(out != null) out.close();
            if(listener != null) listener.disconnected();
        }
        socket = null;
        in = null;
        out = null;
        listener = null;
     }

    void sendMessage(String msg) {
        if(open) out.println(msg);
    }
}
