package shared;

import go.GameplayManager;
import go.Stone;
import util.Pair;

import java.util.Set;

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
        , BEGIN_REMOVAL      // Rozpocznij usuwanie
        , END_REMOVAL        // Skoncz usuwanie
        , NOMINATE_TO_REMOVE // Wyznacz gracza do wskazania martwych pol
        , PROPOSE_REMOVAL    // Zaproponuj usuniecie wskazanych
        , ACCEPT_REMOVAL     // Zaakceptuj wskazane
        , DECLINE_REMOVAL    // Odmow usuniecia wskazanych
        , REMOVE_DEAD        // Ususn wskazane martwe kamienie - konczy gre
        , UPDATE_REMOVAL     // Uaktualnia kamienie do usuniecia
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

    static private class GenericRemovalMsg extends RoomMsg {
        public Set<Pair<Integer, Integer>> toRemove;
        public GenericRemovalMsg(RoomMsg.Type type, Set<Pair<Integer, Integer>> toRemove) { super(type); this.toRemove = toRemove; }
    }

    static public class ProposeRemoval extends GenericRemovalMsg {
        public ProposeRemoval(Set<Pair<Integer, Integer>> toRemove) { super(Type.PROPOSE_REMOVAL, toRemove); }
    }

    static public class RemoveDead extends GenericRemovalMsg {
        public RemoveDead(Set<Pair<Integer, Integer>> toRemove) { super(Type.REMOVE_DEAD, toRemove); }
    }

    static public class UpdateRemoval extends  GenericRemovalMsg {
        public UpdateRemoval(Set<Pair<Integer, Integer>> toRemove) { super(Type.UPDATE_REMOVAL, toRemove); }
    }

    @Override
    public String toString() { return type.toString(); }
}
