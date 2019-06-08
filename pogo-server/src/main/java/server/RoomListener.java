package server;

import go.Board;
import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;
import shared.LobbyMsg;
import shared.Message;
import shared.RoomEvent;
import shared.RoomMsg;
import util.Pair;

import java.util.*;

/**
 * Listener obsugujacy pojedynczy pokoj
 * na ta chwile jedyne co robi to wysyla otrzymane wiadomosci do obywdu graczy
 * w finalnej wersji te wiadomosci beda ruchami graczy
 */
public class RoomListener implements ServerListener {
    private List<ServerClient> clients;
    private ServerClient blackPlayer;
    private ServerClient whitePlayer;
    private List<ServerClient> spectators;
    private String name;
    private LobbyListener lobby;

    private GameplayManager manager;
    private boolean gameInterrupted = false;

    private boolean removingDeadTerritories = false;
    private Set<Pair<Integer, Integer>> stonesToRemove = null;
    private int placementsSinceLastDoublePass = 0;

    private List<RoomEvent> events;
    private Date start;

    RoomListener(String name, Board.BoardSize size, LobbyListener lobby) {
        this.name = name;
        this.lobby = lobby;
        start = null;
        events = new LinkedList<>();
        clients = new LinkedList<>();
        spectators = new LinkedList<>();
        manager = new GameplayManager(size, 6.5);
        whitePlayer = null;
        blackPlayer = null;
    }

    /**
     * @return nazwa pokoju
     */
    public String getName() { return name; }

    /**
     * @return początkowo w projekcie miały być różne gry, nie wiadomo, czy tak będzie, ale warto się zabezpieczyć
     */
    public String getGameType() { return "Go"; }

    /**
     * @return Rozmiar planszy do Go w rozgrywce
     */
    int getBoardSize() { return manager.getBoard().getSize(); }

    /**
     * @return może się zdarzyć, że klient i serwer mają różne implementacje zasad gry
     * niekompatybilne pokoje nie powinny być widoczne dla klienta
     */
    public String gameEngineVersion() { return manager.getEngineVersion(); }

    public enum RoomState {
        GameInProgress,
        GameInterrupted,
        GameFinished,
        EmptyRoom,
        WaitingForBlack,
        WaitingForWhite
    }

    synchronized RoomState getRoomState() {
        // kolejność sprawdzania jest ważna
        if (gameInterrupted) return RoomState.GameInterrupted;
        if (manager.finished()) return RoomState.GameFinished;
        if (clients.size() == 0) return RoomState.EmptyRoom;
        if (clients.size() == 1) return whitePlayer == null ? RoomState.WaitingForWhite : RoomState.WaitingForBlack;
        if (manager.inProgress()) return RoomState.GameInProgress;
        else {
            assert false;
            return RoomState.EmptyRoom; // z jakiegoś powodu Java nie pozwala na kończenie metody assert false
        }
    }

    /**
     * @return Aktualna liczba spectatorów
     */
    synchronized int numberOfSpectators() { return spectators.size(); }

    /**
     * @return TODO Późniejsza wersja będzie to implementowała
     */
    public synchronized int durationInSeconds() { return 0; }

    private synchronized void registerEvent(String event) {
        RoomEvent toAdd;
        if (start == null) {
            events.add(toAdd = new RoomEvent(event, "", manager.getHistorySize()));
        } else {
            long interval = (new Date().getTime() - start.getTime()) / 1000;
            String seconds = Long.toString(interval % 60);
            if (seconds.length() == 1) seconds = "0" + seconds;
            String timestamp = interval / 60 + ":" + seconds;
            events.add(toAdd = new RoomEvent(event, timestamp, manager.getHistorySize()));
        }

        sendToAll(new RoomMsg.AddEvent(toAdd));
    }

    private ServerClient getPlayer(Stone color) { return color == Stone.White ? whitePlayer : blackPlayer; }
    private Stone getPlayerColor(ServerClient player) { return whitePlayer == player ? Stone.White : Stone.Black; }

    /**
     * Pokój nie akceptuje już żadnych graczy w stanie interrupted.
     */
    synchronized boolean joinPlayer(ServerClient client, Stone color) {
        if (clients.size() >= 2 || gameInterrupted || manager.finished() || getPlayer(color) != null) {
            client.sendMessage(new LobbyMsg(LobbyMsg.Type.CONNECTION_REFUSED)); // -> ClientLobbyListener // TODO Reason
            return false;
        }

        clients.forEach(c -> c.sendMessage(new RoomMsg(RoomMsg.Type.OPPONENT_JOINED))); // -> ClientRoomListener

        if(color == Stone.White) whitePlayer = client;
        else blackPlayer = client;

        client.setListener(this);
        clients.add(client);

        client.sendMessage(new LobbyMsg.Connected(color, manager.getSize())); // -> ClientLobbyListener

        registerEvent(color.pictogram + " joined the game");

        lobby.broadcastRoomUpdate();

        if (clients.size() == 2) {
            sendToAll(new RoomMsg(RoomMsg.Type.GAME_BEGINS));

            start = new Date();
            registerEvent("Game begins!");
        }

        return true;
    }

    synchronized void joinSpectator(ServerClient client) {
        client.setListener(this);
        spectators.add(client);

        System.out.println(client + " joined " + this + " sending " +
                manager.getMoveHistory().size() + " of " + manager.getHistorySize() + " movess, and " +
                events.size() + " events");

        client.sendMessage(new LobbyMsg.ConnectedSpectator(manager.getMoveHistory(), events, manager.getSize()));

        lobby.broadcastRoomUpdate();
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {
        if(clients.contains(client)) {
            Stone color = client == whitePlayer ? Stone.White : Stone.Black;

            clients.remove(client);

            if(client == whitePlayer) whitePlayer = null;
            if(client == blackPlayer) blackPlayer = null;

            if (manager.inProgress() && !gameInterrupted) {
                gameInterrupted = true;
                for (ServerClient c : clients)
                    c.sendMessage(new RoomMsg(RoomMsg.Type.OPPONENT_DISCONNECTED));
            }

            registerEvent(color.pictogram + " left the game");

        } else {
            spectators.remove(client);
        }

        lobby.broadcastRoomUpdate();
    }

    @Override
    public synchronized void receivedInput(ServerClient client, Message message) {
        if(!(message instanceof RoomMsg)) { System.out.println("Unrecognized message in " + this); return; }

        RoomMsg roomMsg = (RoomMsg) message;

        switch(roomMsg.type) {
            case QUIT:
                clientDisconnected(client);
                lobby.clientConnected(client);
                break;

            case MOVE:
                if (clients.size() <= 1) {
                    clients.forEach(c -> c.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_REJECTED)));
                    return;
                }

                GameplayManager.Move move = ((RoomMsg.Move) roomMsg).move;

                if (manager.finished() || gameInterrupted) {
                    client.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_REJECTED));
                } else if (manager.inProgress()) {
                    Optional<ReasonMoveImpossible> result = manager.registerMove(move);
                    if (result.isPresent()) {
                        client.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_REJECTED));
                        break;
                    }

                    client.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_ACCEPTED));

                    sendToAllExcept(message, client);

                    if(move instanceof GameplayManager.Pass) {
                        registerEvent(move.player.pictogram + " has passed their turn");
                    } else {
                        ++ placementsSinceLastDoublePass;

                        Pair<Integer, Integer> pos = ((GameplayManager.StonePlacement) move).position;
                        registerEvent(move.player.pictogram + " places stone at " +
                                manager.getBoard().positionToNumeral(new Pair<>(pos.y, pos.x)));
                    }

                    if(manager.hadTwoPasses()) {
                        if(placementsSinceLastDoublePass == 0) {
                            finishTheGame();
                        } else {
                            placementsSinceLastDoublePass = 0;
                            beginRemoval();
                        }
                    }
                }
                break;

            case PROPOSE_REMOVAL:
                assert removingDeadTerritories;
                System.out.println("Player " + getPlayerColor(client) + " proposes removal");
                getPlayer(getPlayerColor(client).opposite).sendMessage(roomMsg);
                stonesToRemove = ((RoomMsg.ProposeRemoval) roomMsg).toRemove;
                break;

            case ACCEPT_REMOVAL:
                System.out.println("Player " + getPlayerColor(client) + " accepts removal");
                if(getPlayerColor(client) == manager.nextTurn()) {
                    finishTheGame();
                } else {
                    getPlayer(manager.nextTurn().opposite).sendMessage(new RoomMsg(RoomMsg.Type.NOMINATE_TO_REMOVE));
                    System.out.println("Nominating player " + manager.nextTurn().opposite + " " + this);
                }
                break;

            case DECLINE_REMOVAL:
                System.out.println("Player " + getPlayerColor(client) + " declines removal");
                cancelRemoval();
                break;

            case UPDATE_REMOVAL:
                System.out.println("Player " + getPlayerColor(client) + " updates stones to be removed");
                stonesToRemove = ((RoomMsg.UpdateRemoval) roomMsg).toRemove;
                sendToPlayersExcept(message, client);
                break;

            case CHAT:
                RoomMsg.Chat msg = (RoomMsg.Chat) message;
                registerEvent("(" + msg.player.pictogram + "): " + msg.msg);

            default:
                System.out.println("Unrecognized message: " + message.msg);
        }
    }

    private void beginRemoval() {
        System.out.println("Begin removal " + this);

        removingDeadTerritories = true;
        stonesToRemove = new HashSet<>();
        sendToPlayers(new RoomMsg(RoomMsg.Type.BEGIN_REMOVAL));
        try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
        getPlayer(manager.nextTurn()).sendMessage(new RoomMsg(RoomMsg.Type.NOMINATE_TO_REMOVE));

        System.out.println("Nominating player " + manager.nextTurn() + " " + this);
    }

    private void cancelRemoval() {
        System.out.println("Cancel removal " + this);

        removingDeadTerritories = false;
        stonesToRemove = null;
        sendToPlayers(new RoomMsg(RoomMsg.Type.END_REMOVAL));
        registerEvent("Players couldn't agree on dead groups");
    }

    private void finishTheGame() {
        System.out.println("Finish the game " + this);

        manager.registerMove(new GameplayManager.DeadStonesRemoval(stonesToRemove));
        sendToAll(new RoomMsg.RemoveDead(stonesToRemove));

        sendToAll(new RoomMsg.GameFinished(manager.result()));

        registerEvent("Game is finished: " + manager.result().winner.pictogram + " won!");
    }

    private void sendToAll(Message msg) {
        sendToPlayers(msg);
        sendToSpectators(msg);
    }

    private void sendToAllExcept(Message msg, ServerClient notTo) {
        sendToPlayersExcept(msg, notTo);
        sendToSpectators(msg);
    }

    private void sendToSpectators(Message msg) {
        spectators.forEach(s -> s.sendMessage(msg));
    }

    private void sendToPlayers(Message msg) {
        clients.forEach(c -> c.sendMessage(msg));
    }

    private void sendToPlayersExcept(Message msg, ServerClient notTo) {
        clients.forEach(c -> { if(c != notTo) c.sendMessage(msg); });
    }

    @Override
    public synchronized boolean clientConnected(ServerClient client) { return false; }

    @Override
    public synchronized void serverClosed() {
        System.out.println("server closed");
    }

    @Override
    public String toString() { return "[Room " + name + "]"; }
}
