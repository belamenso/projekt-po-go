package client;

import shared.Message;

import java.io.*;
import java.net.*;

class Client {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private boolean open;

    private ClientListener listener;

    Client() {
        open = false;
        listener = null;
        socket = null;
        inputStream = null;
        outputStream = null;
    }

    void startConnection(String ip, int port) {
        if (open) return;

        Thread ct = new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 2000);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                open = true;
                listener.connectedToServer();

                while (open) {
                    try {
                        Message msg = null;
                        try {
                            msg = (Message) inputStream.readObject();
                        } catch (ClassNotFoundException e) { e.printStackTrace(); }

                        if (msg == null) { throw new IOException(); }

                        listener.receivedInput(msg);
                    } catch (IOException ex) {
                        //ex.printStackTrace();
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
        System.out.println("Setting listener: " + listener.toString());
        this.listener = listener;
    }

     void close() {
        System.out.println("close client");
        if(open) {
            open = false;

            try { if(      socket != null)       socket.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if( inputStream != null)  inputStream.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if(outputStream != null) outputStream.close(); } catch (IOException e) { e.printStackTrace(); }

            if(listener != null) listener.disconnected();
        }
        socket = null;
        inputStream = null;
        outputStream = null;
     }

    void sendMessage(Message msg) {
        if(open) try {
            outputStream.reset();
            outputStream.writeObject(msg);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }
}
