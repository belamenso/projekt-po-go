package shared;

import go.Board;
import go.Stone;

import java.util.List;

public class LobbyMsg extends Message {
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
        public String roomName;
        public Board.BoardSize size;
        public CreateMessage(String roomName, Board.BoardSize size) {
            super(Type.CREATE); this.roomName = roomName; this.size = size;
        }
    }

    static public class JoinMsg extends LobbyMsg {
        public String roomName;
        public JoinMsg(String roomName) { super(Type.JOIN); this.roomName = roomName; }
    }

    static public class RemoveMsg extends LobbyMsg {
        public String roomName;
        public RemoveMsg(String roomName) { super(Type.REMOVE); this.roomName = roomName; }
    }

    static public class ListMsg extends LobbyMsg {
        public List<RoomData> data;
        public ListMsg(List<RoomData> data) { super(Type.LIST); this.data = data; }
    }

    static public class ConnectedMsg extends LobbyMsg {
        public Stone color;
        public Board.BoardSize size;
        public ConnectedMsg(Stone color, Board.BoardSize size) { super(Type.CONNECTED); this.color = color; this.size = size; }
    }
}
