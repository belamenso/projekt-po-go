package server;

import client.RoomData;
import go.Stone;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Podstawowy listener
 * Przyjmuje nowych klientow na serwerze
 * moze zwracac liste pokoi, tworzyc nowe pokoje
 * i laczyc klientow z istniejacymi
 */
public class LobbyListener implements ServerListener {
    private final List<ServerClient> clients;
    private final List<RoomListener> rooms;

    LobbyListener() {
        clients = new LinkedList<>();
        rooms = new LinkedList<>();
    }

    @Override
    public synchronized boolean clientConnected(ServerClient client) {
        System.out.println(client + " connected to lobby");
        clients.add(client);
        client.setListener(this);
        client.sendMessage(new LobbyMsg(LobbyMsg.Type.LOBBY_JOINED));
        return true;
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {
        System.out.println(client + " dissconected from lobby");
        clients.remove(client);
    }

    private LobbyMsg.ListMsg roomListing() {
        List<RoomData> data = new ArrayList<>();
        for(RoomListener room : rooms)
            data.add(new RoomData(room.getName(), room.getRoomState().name()));
        return new LobbyMsg.ListMsg(data);
    }

    public void broadcastRoomUpdate() {
        Message msg = roomListing();
        for (ServerClient c : clients) {
            if (c == null) {
                System.out.println("dlaczego null tutaj?");
            } else {
                c.sendMessage(msg);
            }
        }
    }

    @Override
    public synchronized void receivedInput(ServerClient client, Message message) {
        String msg = message.msg;

        System.out.println("Lobby received " + msg + " from " + client);

        if(!(message instanceof LobbyMsg)) { System.out.println("Lobby received incorrect message"); return; }

        LobbyMsg lobbyMessage = (LobbyMsg) message;

        switch(lobbyMessage.type) {
            case LIST_REQUEST:
                client.sendMessage(roomListing());
                break;

            case CREATE:
                String toCreate = ((LobbyMsg.CreateMessage) lobbyMessage).roomName;
                for(RoomListener room : rooms) {
                    if(room.getName().equals(toCreate)) {
                        client.sendMessage(new LobbyMsg(LobbyMsg.Type.NAME_TAKEN));
                        return;
                    }
                }

                System.out.println("Adding room " + toCreate);
                rooms.add(new RoomListener(toCreate, this));

                broadcastRoomUpdate();
                break;

            case JOIN:
                String toJoin = ((LobbyMsg.JoinMsg) lobbyMessage).roomName;

                for(RoomListener room : rooms) {
                    if(room.getName().equals(toJoin)) {
                        if(room.clientConnected(client))
                            clients.remove(client);
                        return;
                    }
                }

                client.sendMessage(new LobbyMsg(LobbyMsg.Type.ROOM_NOT_FOUND));
                break;

            case REMOVE:
                final String name = msg.split(" ")[1];

                if(rooms.removeIf(roomListener -> roomListener.getName().equals(name)))
                    broadcastRoomUpdate();
                break;

            default:
                System.out.println("Unsopported lobby message " + lobbyMessage.type.name());
        }
    }

    public static class LobbyMsg extends Message {
        public enum Type {
              LIST                 // Lobby       -> ClientLobby
            , ROOM_NOT_FOUND       // Lobby       -> ClientLobby
            , NAME_TAKEN           // Lobby       -> ClientLobby
            , LOBBY_JOINED         // Lobby       -> ClientLobby / ClientRoom
            , CREATE               // ClientLobby -> Lobby
            , JOIN                 // ClientLobby -> Lobby
            , LIST_REQUEST         // ClientLobby -> Lobby
            , REMOVE               // Room        -> Lobby
            , CONNECTION_REFUSED   // Room        -> ClientLobby
            , CONNECTED            // Room        -> ClientLobby
        }

        public Type type;
        public LobbyMsg(Type type) { super(type.name()); this.type = type; }

        // Trochę to syf może da się lepiej?

        static public class CreateMessage extends LobbyMsg {
            String roomName;
            public CreateMessage(String roomName) { super(Type.CREATE); this.roomName = roomName; }
        }

        static public class JoinMsg extends LobbyMsg {
            String roomName;
            public JoinMsg(String roomName) { super(Type.JOIN); this.roomName = roomName; }
        }

        static class RemoveMsg extends LobbyMsg {
            String roomName;
            RemoveMsg(String roomName) { super(Type.REMOVE); this.roomName = roomName; }
        }

        static public class ListMsg extends LobbyMsg {
            public List<RoomData> data;
            ListMsg(List<RoomData> data) { super(Type.LIST); this.data = data; }
        }

        static public class ConnectedMsg extends LobbyMsg {
            public Stone color;
            ConnectedMsg(Stone color) { super(Type.CONNECTED); this.color = color; }
        }
    }

    @Override
    public synchronized void serverClosed() {
        System.out.println("Server closed for lobby");
    }

    @Override
    public String toString() { return "LOBBY"; }
}
