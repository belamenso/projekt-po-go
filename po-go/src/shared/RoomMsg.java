package shared;

import go.GameplayManager;
import go.Stone;

public class RoomMsg extends Message {
    public enum Type {
          MOVE
        , GAME_BEGINS
        , GAME_FINISHED
        , MOVE_ACCEPTED
        , MOVE_REJECTED
        , OPPONENT_DISCONNECTED
        , OPPONENT_JOINED
        , CHAT
        , QUIT
        , ADD_EVENT
    }

    public RoomMsg(Type type) { super(type.name()); this.type = type; }

    public Type type;

    static public class Move extends RoomMsg {
        public GameplayManager.Move move;
        public Move(GameplayManager.Move move) { super(Type.MOVE); this.move = move; }

        @Override
        public String toString() { return move.toString(); }
    }

    static public class Chat extends RoomMsg {
        public Stone player;
        public String msg;
        public Chat(Stone player, String msg) { super(Type.CHAT); this.player = player; this.msg = msg; }
    }

    static public class GameFinished extends RoomMsg {
        //public GameplayManager.Result result;
        public Stone winner;
        public double blackPoints;
        public double whitePoints;
        public GameFinished(GameplayManager.Result result) { super(Type.GAME_FINISHED);
            winner = result.winner; whitePoints = result.whitePoints; blackPoints = result.blackPoints; }
    }

    static public class AddEvent extends RoomMsg {
        public RoomEvent event;
        public AddEvent(RoomEvent event) { super(Type.ADD_EVENT); this.event = event; }

        @Override
        public String toString() { return "Add event " + event.getName() + " " + event.getTime() + " " + event.turnNumber; }
    }

    @Override
    public String toString() { return type.toString(); }
}
