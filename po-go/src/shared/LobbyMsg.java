package shared;

import go.Board;
import go.GameplayManager;
import go.Stone;

import java.util.List;

public class LobbyMsg extends Message {
    public enum Type {
          LISTING              // Lobby       -> ClientLobby
        , ROOM_NOT_FOUND       // Lobby       -> ClientLobby
        , NAME_TAKEN           // Lobby       -> ClientLobby
        , LOBBY_JOINED         // Lobby       -> ClientLobby / ClientRoom
        , CREATE               // ClientLobby -> Lobby
        , JOIN                 // ClientLobby -> Lobby
        , SPECTATE             // ClientLobby -> Lobby
        , LIST_REQUEST         // ClientLobby -> Lobby
        , REMOVE               // Room        -> Lobby
        , CONNECTION_REFUSED   // Room        -> ClientLobby
        , CONNECTED            // Room        -> ClientLobby
        , CONNECTED_SPECTATOR  // Room        -> ClientLobby
    }

    public Type type;
    public LobbyMsg(Type type) { super(type.name()); this.type = type; }

    // Trochę to syf może da się lepiej?

    static public class Create extends LobbyMsg {
        public String roomName;
        public Board.BoardSize size;
        public Create(String roomName, Board.BoardSize size) {
            super(Type.CREATE); this.roomName = roomName; this.size = size;
        }
    }

    static public class Join extends LobbyMsg {
        public String roomName;
        public Join(String roomName) { super(Type.JOIN); this.roomName = roomName; }
    }

    static public class Spectate extends LobbyMsg {
        public String roomName;
        public Spectate(String roomName) { super(Type.SPECTATE); this.roomName = roomName; }
    }

    static public class Remove extends LobbyMsg {
        public String roomName;
        public Remove(String roomName) { super(Type.REMOVE); this.roomName = roomName; }
    }

    static public class Listing extends LobbyMsg {
        public List<RoomData> data;
        public Listing(List<RoomData> data) { super(Type.LISTING); this.data = data; }
    }

    static public class Connected extends LobbyMsg {
        public Stone color;
        public Board.BoardSize size;
        public Connected(Stone color, Board.BoardSize size) { super(Type.CONNECTED); this.color = color; this.size = size; }
    }

    static public class ConnectedSpectator extends LobbyMsg {
        public List<GameplayManager.Move> moves;
        public List<RoomEvent> events;
        public Board.BoardSize size;
        public ConnectedSpectator(List<GameplayManager.Move> moves, List<RoomEvent> events, Board.BoardSize size)
                { super(Type.CONNECTED_SPECTATOR); this.moves = moves; this.events = events; this.size = size;
                 System.out.println("Created spectate request " + moves.size() + " " + events.size()); }
    }
}
