package server;

import shared.Message;

import java.io.*;
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
        int port = 33107;
        if(args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch(NumberFormatException e) { System.out.println("Argument is not a number"); }
        }
        Server s = new Server(port, new LobbyListener());
        System.out.println("The server started at " + s.ip + " : " + s.port);
        System.out.println("Type 'close' to close the server");

        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()) {
            String in = sc.nextLine();
            if(in.equals("close"))
                break;
        }

        s.close();
    }

    ServerListener defaultListener;
    private String ip;
    private int port;
    boolean open = true;
    private ServerSocket serverSocket;
    List<ServerClient> clients = new LinkedList<>();

    Server(int port, ServerListener defaultListener) {
        this.defaultListener = defaultListener;
        try {
            this.port = port;
            serverSocket = new ServerSocket(port);
            serverSocket.getInetAddress();
            ip = InetAddress.getLocalHost().getHostAddress();

            Thread st = new Thread(() -> {
                while(open){
                    try {
                        Socket socket = serverSocket.accept();
                        ServerClient.createNewClient(socket, this);
                    } catch(Exception e) {
                        //e.printStackTrace();
                    }
                }
            });

            st.setDaemon(true);
            st.setName("Server thread");
            st.start();

        } catch(IOException e){ /*.printStackTrace();*/ }
    }

    synchronized private void close() {
        if(!open) return;
        open = false;
        try{ serverSocket.close(); } catch(IOException e){ /*e.printStackTrace();*/ }

        for(ServerClient s : clients) s.close();

        clients.clear();
        clients = null;
        serverSocket = null;
        defaultListener.serverClosed();
        defaultListener = null;
    }
}