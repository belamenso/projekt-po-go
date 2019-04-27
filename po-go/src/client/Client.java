package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Klient, lączy się z serwerem, w finalnej wersji uruchaminy bedzie przez GUI
 * z podanymi w nim IP i portem
 */
public class Client{
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.print("IP: ");
        String ip = s.nextLine();
        System.out.print("port: ");
        int port = s.nextInt();
        Client c = new Client();
        c.setListener(new ClientLobbyListener(c));
        c.startConnection(ip, port);
        System.out.println("Enter commands:");
        while(s.hasNext()) {
            String msg = s.nextLine();
            c.sendMessage(msg);
        }
    }

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientListener listener;

    private boolean open;

    public Client() {
        open = false;
        listener = null;
    }

    public void startConnection(String ip, int port) {
        if(open) return;
        open = true;

        try{
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Thread clientThread = new Thread(() -> {
                while(open) {
                    try {
                        String s = in.readLine();
                        if (s == null) {
                            listener.disconnected();
                            close();
                            return;
                        }
                        listener.receivedInput(s);
                    } catch (IOException ex) {
                        listener.serverClosed();
                        close();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            clientThread.setName("Client Connection");
            clientThread.setDaemon(true);
            clientThread.start();

            listener.connectedToServer();

        }catch(UnknownHostException e){
            open=false;
            listener.unknownHost();
        }catch(IOException e){
            open=false;
            listener.couldNotConnect();
        }catch(Exception e){
            open=false;
            e.printStackTrace();
        }
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    public void close() {
        try{
            if(open) {
                open = false;
                socket.close();
                in.close();
                out.close();
                listener.disconnected();
            }
            socket = null;
            in = null;
            out = null;
            listener = null;
        } catch(Exception e) { e.printStackTrace();}
    }

    public void sendMessage(String msg){ if(open) out.println(msg); }

    public boolean isConnected(){ return open; }
}