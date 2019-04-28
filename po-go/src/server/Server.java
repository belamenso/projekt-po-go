package server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Startuje serwer na wolnym porcie
 * wypisuje IP i port potrzebne do polaczenia
 *
 */
public class Server {
    public static void main(String[] args) {
        Server s = new Server(0, new LobbyListener());
        System.out.println("The server started at " + s.getIp() + " : " + s.port);

        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()) {
            String in = sc.nextLine();
            if(in.equals("close"))
                break;
            else if(in.equals("clients")) {

            }
        }

        s.dispose();
    }

    private ServerListener defaultListener;
    private int port;
    private boolean open = true;
    private ServerSocket ss;
    private ArrayList<Socket> clients = new ArrayList<>();

    public Server(int port, ServerListener defaultListener) {
        this.defaultListener = defaultListener;
        try {
            ss = new ServerSocket(port);
            this.port = port == 0 ? ss.getLocalPort() : port;

            Thread serverThread = new Thread(() -> {
                while(open){
                    try {
                        createClientThread();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            serverThread.setDaemon(true);
            serverThread.setName("Server");
            serverThread.start();

        } catch(IOException e){ e.printStackTrace(); }
    }

    private void createClientThread() throws IOException {
        @SuppressWarnings("resource")
        final Socket socket = ss.accept();
        Thread clientThread = new Thread(() -> {
            try {
                clients.add(socket);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                ServerClient client = new ServerClient(socket.getInetAddress(), socket.getPort(), out, defaultListener);
                client.listener.clientConnected(client);
                Thread.currentThread().setName("Thread for " + client);

                while(open) {
                    try{
                        client.listener.receivedInput(client, in.readLine());
                    } catch(IOException e) {
                        client.listener.clientDisconnected(client);

                        try {
                            if(!socket.isClosed()){
                                socket.shutdownOutput();
                                socket.close();
                            }
                        } catch(Exception e2) { e2.printStackTrace(); }
                        clients.remove(socket);
                        return;
                    }
                }
            } catch(Exception e){ e.printStackTrace(); }

            try {
                socket.close();
            } catch(Exception e){ e.printStackTrace(); }

            clients.remove(socket);
        });

        clientThread.setDaemon(true);
        clientThread.start();
    }

    public void dispose() {
        open = false;
        try{
            ss.close();
        } catch(IOException e){ e.printStackTrace(); }

        for(Socket s : clients) {
            try{
                s.close();
            } catch(Exception exception){ exception.printStackTrace(); }
        }

        clients.clear();
        clients = null;
        ss = null;
        defaultListener.serverClosed();
        defaultListener = null;
    }

    public String getIp(){
        try{
            ss.getInetAddress();
            return InetAddress.getLocalHost().getHostAddress();
        } catch(UnknownHostException e){ e.printStackTrace(); }
        return null;
    }
}