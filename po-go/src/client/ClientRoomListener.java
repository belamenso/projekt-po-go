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

import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    private final boolean spectator;

    /**
     * Tworz listener dla gracza
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

                //Platform.runLater(() -> rg.addMessage("The game begins, you are " + myColor.pictogram, start));

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

            default:
                System.out.println("UNRECOGNIZED MSG: " + roomMsg.msg);
        }

        Platform.runLater(() -> {
            rg.renderBoard();
        });
    }

    /**
     * called from the GUI thread
     */
    synchronized void attemptedToPass() {
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
