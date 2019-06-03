package client;

import go.Board;
import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;
import javafx.application.Platform;
import shared.LobbyMsg;
import shared.Message;
import shared.RoomEvent;
import shared.RoomMsg;
import util.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static go.GameLogic.gameLogic;

/**
 * Listener obsugujacy pokoj po stronie gracza
 */
public class ClientRoomListener implements ClientListener {
    private final Client client;
    private final RoomGUI rg;

    private final Stone myColor;

    final GameplayManager manager;
    private boolean gameStarted = false;
    private boolean gameInterrupted = false;
    private boolean isRemoval = false;
    private boolean isNomination = false;
    private boolean isAcceptance = false;

    private Set<Pair<Integer, Integer>> removalStones;

    private final boolean spectator;

    /**
     * Tworzy listener dla gracza
     */
    ClientRoomListener(RoomGUI rg, Client client, Stone color, Board.BoardSize size) {
        System.out.println("### Listener for player");
        spectator = false;
        this.rg = rg;
        this.client = client;
        client.setListener(this);
        this.myColor = color;
        manager = new GameplayManager(size, 6.5);
    }

    /**
     * Tworzy listener dla spectatora
     */
    ClientRoomListener(RoomGUI rg, Client client, Board.BoardSize size, List<GameplayManager.Move> moves, List<RoomEvent> events)  {
        System.out.println("### Listener for spectator");
        spectator = true;
        this.rg = rg;
        this.client = client;
        client.setListener(this);
        this.myColor = null;
        manager = new GameplayManager(size, 6.5);
        moves.forEach(manager::registerMove);
        System.out.println("Spectator joined: " + moves.size() + " " + events.size());
        moves.forEach(System.out::println);
        Platform.runLater(() -> events.forEach(rg::addMessage));
    }

    synchronized int getTurnCount() { return manager.getHistorySize(); }

    synchronized boolean isSpectator() { return spectator; }

    synchronized Stone getColor() { return myColor; }

    synchronized Board getBoard() { return manager.getBoard(); }

    synchronized boolean myTurn() {
        return gameStarted && !gameInterrupted && manager.inProgress() && manager.nextTurn().equals(myColor);
    }

    synchronized Set<Pair<Integer, Integer>> getRemovalStones() { return removalStones; }

    synchronized boolean isRemovalPhaseOn() { return isRemoval; }
    synchronized boolean nominating() { return isNomination; }
    synchronized boolean accepting() { return isAcceptance; }

    synchronized boolean wasInterruped() { return gameInterrupted; }

    private synchronized void makeMyMove(GameplayManager.Move move) {
        manager.registerMove(move);
        client.sendMessage(new RoomMsg.Move(move));
    }

    @Override
    public synchronized void receivedInput(Message message) {
        String msg = message.msg;

        if(!(message instanceof RoomMsg)) {
            if(message instanceof LobbyMsg && ((LobbyMsg) message).type == LobbyMsg.Type.LOBBY_JOINED) {
                rg.returnToLobby();
            } else {
                System.out.println("Unrecognized message " + message.msg);
            }
            return;
        }

        RoomMsg roomMsg = (RoomMsg) message;

        switch (roomMsg.type) {
            case GAME_BEGINS:
                gameStarted = true;

                if(!spectator) Platform.runLater(() -> rg.addMessage(new RoomEvent("You are " + myColor.pictogram, "", 0)));

                Sounds.playSound("dingdong");
                break;

            case GAME_FINISHED:
                //Platform.runLater(() -> rg.addMessage( ((RoomMsg.GameFinished)roomMsg).result.winner + " won", start));
                break;

            case MOVE_ACCEPTED:
                break;

            case MOVE_REJECTED:
                //Platform.runLater(() -> rg.addMessage("ERROR: move rejected by the server", start));
                break;

            case OPPONENT_DISCONNECTED:
                gameInterrupted = true;
                //Platform.runLater(() -> rg.addMessage(myColor.pictogram + " has disconnected", start));
                break;

            case MOVE:
                GameplayManager.Move move = ((RoomMsg.Move) roomMsg).move;
                Optional<ReasonMoveImpossible> reason = manager.registerMove(move);
                assert reason.isEmpty();

                if(move instanceof GameplayManager.Pass) {
                    //Platform.runLater(() -> rg.addMessage(myColor.opposite.pictogram + " has passed their turn", start));
                    Sounds.playSound("pass");
                } else {

                    //Platform.runLater(() -> rg.addMessage(myColor.opposite.pictogram + " places stone at " +
                            //getBoard().positionToNumeral(((GameplayManager.StonePlacement) move).position), start));
                    Sounds.playSound("stone");
                }
                break;

            case OPPONENT_JOINED:
                //Platform.runLater(() -> rg.addMessage(myColor.opposite.pictogram + " has joined the game", null));
                break;

            case CHAT:
                RoomMsg.Chat chatmsg = (RoomMsg.Chat) roomMsg;
                //Platform.runLater(() -> rg.addMessage("(" + chatmsg.player + "): " + chatmsg.msg, start));
                break;

            case ADD_EVENT:
                Platform.runLater(() -> rg.addMessage(((RoomMsg.AddEvent)roomMsg).event));
                //System.out.println("Add event!");
                break;

            case BEGIN_REMOVAL:
                System.out.println("Begin removal");
                isRemoval = true;
                break;

            case END_REMOVAL:
                System.out.println("End removal");
                isRemoval = false;
                break;

            case NOMINATE_TO_REMOVE:
                System.out.println("Nominated to remove");
                isNomination = true;
                removalStones = new HashSet<>();
                Platform.runLater(rg::showNominationButton);
                break;

            case PROPOSE_REMOVAL:
                System.out.println("Proposed removal");
                removalStones = ((RoomMsg.ProposeRemoval) roomMsg).toRemove;
                isAcceptance = true;
                Platform.runLater(rg::showAcceptanceButtons);
                break;

            case REMOVE_DEAD:
                System.out.println("Removing dead stones");
                Set<Pair<Integer, Integer>> toRemove = ((RoomMsg.RemoveDead) roomMsg).toRemove;
                manager.removeDeadTerritories(toRemove);
                isRemoval = false;
                break;

            default:
                System.out.println("UNRECOGNIZED MSG: " + roomMsg.msg);
        }

        Platform.runLater(rg::renderBoard);
    }

    /**
     * called from the GUI thread
     */
    synchronized void attemptedToPass() {
        if(isRemoval) return;
        if (myTurn()) {
            makeMyMove(new GameplayManager.Pass(myColor));
            //rg.addMessage("You (" + myColor.pictogram + ") passed", start);
            Sounds.playSound("pass");
        } else {
            handleAttemptToSkipTurn();
        }
    }

    /**
     * called from the GUI thread
     */
    synchronized void attemptedToMakeMove(int x, int y) {
        if(isRemoval) {
            if(isNomination) nominate(x, y);
            return;
        }
        if (myTurn()) {
            Optional<ReasonMoveImpossible> reason = gameLogic.movePossible(getBoard(), x, y, myColor);
            if (reason.isPresent()) {
                System.out.println("Move impossible: " + reason.get());
            } else {
                makeMyMove(new GameplayManager.StonePlacement(myColor, x, y));
                //rg.addMessage("You (" + myColor.pictogram + ") moved to " + getBoard().positionToNumeral(new Pair<>(y, x)), start); // nie ruszać kolejności
                rg.renderBoard();
                Sounds.playSound("stone");
            }
        } else {
            handleAttemptToSkipTurn();
        }
    }

    synchronized void acceptRemoval() {
        assert isAcceptance;
        assert isRemoval;
        client.sendMessage(new RoomMsg(RoomMsg.Type.ACCEPT_REMOVAL));
        isAcceptance = false;

        Platform.runLater(rg::renderBoard);
    }

    synchronized void declineRemoval() {
        assert isAcceptance;
        assert isRemoval;
        client.sendMessage(new RoomMsg(RoomMsg.Type.DECLINE_REMOVAL));
        isAcceptance = false;

        Platform.runLater(rg::renderBoard);
    }

    synchronized void finishNominating() {
        assert isNomination;
        assert isRemoval;
        client.sendMessage(new RoomMsg.ProposeRemoval(removalStones));
        removalStones = null;
        isNomination = false;

        Platform.runLater(rg::renderBoard);
    }

    synchronized void nominate(int x, int y) {
        if(manager.getBoard().get(x, y).isEmpty() ||
           manager.getBoard().get(x, y).get().equals(myColor)) return;

        List<Pair<Integer, Integer>> stones = gameLogic.getStoneGroupAt(manager.getBoard(), x, y);

        if(removalStones.contains(new Pair<>(x, y))) {
            removalStones.removeAll(stones);
        } else {
            removalStones.addAll(stones);
        }

        Platform.runLater(rg::renderBoard);
    }

    /**
     * Sends chat message
     */
    synchronized void sendChat(String msg) {
        client.sendMessage(new RoomMsg.Chat(myColor, msg));
    }

    private synchronized void handleAttemptToSkipTurn() { // TODO
        System.out.println("Not your turn!");
    }

    synchronized void sendQuitRequest() {
        client.sendMessage(new RoomMsg(RoomMsg.Type.QUIT));
    }

    @Override
    public synchronized void disconnected() {
        SceneManager.loadConnectionScreen();
    }

    @Override
    public synchronized void couldNotConnect() {}

    @Override
    public synchronized void connectedToServer() {}
}
