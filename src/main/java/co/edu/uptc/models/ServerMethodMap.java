package co.edu.uptc.models;

import java.util.HashMap;
import lombok.Getter;

@Getter
public class ServerMethodMap {
    private UfoSocketServer server;
    private HashMap<String, MethodLibrary> map;

    public ServerMethodMap(UfoSocketServer server) {
        this.server = server;
        map = new HashMap<>();
        addToMap();
    }

    public void addToMap() {
        addStartGameMethod();
        addNumberOfUfosMethod();
        addSpawnRateMethod();
        addSpeedMethod();
        addRequestUfoListMethod();
        addUfoTrajectoryMethod();
        addSelectedPointMethod();
        addUfoImageMethod();
        addRequestUfoDesignMethod();
        addCheckClientModeMethod();
        addRequestUsersListMethod();
    }

    private void addStartGameMethod() {
        map.put("START_GAME", inputLine -> {
            server.startGame();
            server.forceStartGameOrder();
            server.updateConnectedPlayersOrder(server.getClients().size());
        });
    }

    private void addNumberOfUfosMethod() {
        map.put("NUMBER_OF_UFOS", inputLine -> server.handleNumberOfUfos(inputLine));
    }

    private void addSpawnRateMethod() {
        map.put("SPAWN_RATE", inputLine -> server.handleSpawnRate(inputLine));
    }

    private void addSpeedMethod() {
        map.put("SPEED", inputLine -> server.handleSpeed(inputLine));
    }

    private void addRequestUfoListMethod() {
        map.put("REQUEST_UFO_LIST", inputLine -> server.sendUfoList());
    }

    private void addUfoTrajectoryMethod() {
        map.put("UFO_TRAJECTORY", inputLine -> server.handleTrajectoryFromClient(inputLine));
    }

    private void addSelectedPointMethod() {
        map.put("SELECTED_POINT", inputLine -> server.handleSelectedPointFromClient(inputLine));
    }

    private void addUfoImageMethod() {
        map.put("UFO_IMAGE", inputLine -> server.handleSelectedUfoDesign(inputLine));
    }

    private void addRequestUfoDesignMethod() {
        map.put("REQUEST_UFO_DESIGN", inputLine -> server.sendSelectedUfoDesign());
    }

    private void addCheckClientModeMethod() {
        map.put("CHECK_CLIENT_MODE", inputLine -> server.setClientModeOrder());
    }

    private void addRequestUsersListMethod() {
        map.put("REQUEST_USERS_LIST", inputLine -> server.sendUsernameList());
    }

    public void run(String key, String inputLine) {
        MethodLibrary method = map.get(key);
        if (method != null) {
            method.execute(inputLine);
        }
    }

    public interface MethodLibrary {
        void execute(String inputLine);
    }
}