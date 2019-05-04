package server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Startuje serwer na wolnym porcie
 * wypisuje IP i port potrzebne do polaczenia
 *
 */
public class Server {
    public static void main(String[] args) {
        Server s = new Server(33107, new LobbyListener());
        System.out.println("The server started at " + s.ip + " : " + s.port);

        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()) {
            String in = sc.nextLine();
            if(in.equals("close"))
                break;
        }

        s.close();
    }

    private ServerListener defaultListener;
    private String ip;
    private int port;
    private boolean open = true;
    private ServerSocket ss;
    private List<Socket> clients = new LinkedList<>();

    Server(int port, ServerListener defaultListener) {
        this.defaultListener = defaultListener;
        try {
            this.port = port;
            ss = new ServerSocket(port);
            ss.getInetAddress();
            ip = InetAddress.getLocalHost().getHostAddress();

            Thread st = new Thread(() -> {
                while(open){
                    try {
                        createClientThread();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            st.setDaemon(true);
            st.setName("Server thread");
            st.start();

        } catch(IOException e){ e.printStackTrace(); }
    }

    private void createClientThread() throws IOException {
        final Socket socket = ss.accept();
        Thread clientThread = new Thread(() -> {
            try {
                clients.add(socket);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                ServerClient client = new ServerClient(socket.getInetAddress(), socket.getPort(), out, defaultListener);
                client.listener.clientConnected(client);
                Thread.currentThread().setName("Thread for " + client);

                while (open) {
                    try {
                        String line = in.readLine();
                        if (line == null) throw new IOException();
                        client.listener.receivedInput(client, line);
                    } catch (IOException e) {
                        client.listener.clientDisconnected(client);

                        try {
                            if (!socket.isClosed()) {
                                socket.shutdownOutput();
                                socket.close();
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        clients.remove(socket);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try { socket.close(); } catch (Exception e) { e.printStackTrace(); }

            clients.remove(socket);
        });

        clientThread.setDaemon(true);
        clientThread.start();
    }

    private void close() {
        open = false;
        try{ ss.close(); } catch(IOException e){ e.printStackTrace(); }

        for(Socket s : clients)
            try{ s.close(); } catch(Exception e){ e.printStackTrace(); }

        clients.clear();
        clients = null;
        ss = null;
        defaultListener.serverClosed();
        defaultListener = null;
    }
}