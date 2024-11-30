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
        map.put("START_GAME", inputLine -> {
            server.startGame();
            server.forceStartGameOrder();
            server.updateConnectedPlayersOrder(server.getClients().size());
        });
        map.put("NUMBER_OF_UFOS", inputLine -> server.handleNumberOfUfos(inputLine));
        map.put("SPAWN_RATE", inputLine -> server.handleSpawnRate(inputLine));
        map.put("SPEED", inputLine -> server.handleSpeed(inputLine));
        map.put("REQUEST_UFO_LIST", inputLine -> server.sendUfoList());
        map.put("UFO_TRAJECTORY", inputLine -> server.handleTrajectoryFromClient(inputLine));
        map.put("SELECTED_POINT", inputLine -> server.handleSelectedPointFromClient(inputLine));
        map.put("UFO_IMAGE", inputLine -> server.handleSelectedUfoDesign(inputLine));
        map.put("REQUEST_UFO_DESIGN", inputLine -> server.sendSelectedUfoDesign());
        map.put("CHECK_CLIENT_MODE", inputLine -> server.setClientModeOrder());
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