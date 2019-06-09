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
        //, CONNECTED_SPECTATOR  // Room        -> ClientLobby
        , ROOM_CREATED         // Lobby       -> ClientLobby / ClientRoom
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
        public Stone color;
        public Join(String roomName, Stone color) { super(Type.JOIN); this.roomName = roomName; this.color = color; }
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
        public List<GameplayManager.Move> moves;
        public List<RoomEvent> events;
        public Connected(Stone color, Board.BoardSize size, List<GameplayManager.Move> moves, List<RoomEvent> events) {
            super(Type.CONNECTED); this.color = color; this.size = size; this.moves = moves; this.events = events;
        }
    }


    private static final long serialVersionUID = 6L;
}
