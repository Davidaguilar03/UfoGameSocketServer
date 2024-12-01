package co.edu.uptc.models;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import co.edu.uptc.pojos.Ufo;
import java.awt.Point;
import java.awt.Rectangle;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Getter
@Setter
public class UfoSocketServer {
    private ServerSocket serverSocket;
    private UfoRunner ufoRunner;
    private UfoSpawnRunner spawnRunner;
    private List<Ufo> ufos;
    private ArrayList<Point> trajectoryPoints;
    private Point selectedPoint;
    private int spawnRate;
    private int speed;
    private int numberofUfos;
    private String selectedUfoDesign;
    private List<ClientHandler> clients;
    private ClientHandler adminClient;
    private Gson gson;

    public UfoSocketServer() {
        this.ufos = new CopyOnWriteArrayList<>();
        ufoRunner = new UfoRunner(this);
        spawnRunner = new UfoSpawnRunner(this);
        this.trajectoryPoints = new ArrayList<>();
        this.clients = new ArrayList<>();
        gson = new Gson();
    }

    public synchronized void addUfo(int speed, int id) {
        UfoController ufoController = new UfoController(this);
        Ufo newUfo = ufoController.createUfo(speed, id);
        ufos.add(newUfo);
    }

    public void startGame() {
        Thread moveThread = new Thread(ufoRunner);
        moveThread.start();
        Thread spawnThread = new Thread(spawnRunner);
        spawnThread.start();
    }

    public synchronized void moveAll() {
        Iterator<Ufo> iterator = ufos.iterator();
        while (iterator.hasNext()) {
            Ufo ufo = iterator.next();
            UfoController ufoController = new UfoController(this);
            ufoController.moveUfo(ufo, ufos);
        }
    }

    public void startServer(int port) {
        try {
            initializeServerSocket(port);
            acceptClients();
        } catch (BindException e) {
            System.out.println("Error: El puerto " + port + " ya está en uso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeServerSocket(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        String serverIp = getEthernetIp();
        System.out.println("Servidor iniciado en la IP " + serverIp + " y puerto " + port);
    }

    private void acceptClients() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
            ClientHandler clientHandler = new ClientHandler(clientSocket, this, adminClient == null);
            clients.add(clientHandler);
            assignAdminClient(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    private void assignAdminClient(ClientHandler clientHandler) {
        if (adminClient == null) {
            adminClient = clientHandler;
            adminClient.setAdmin(true);
            System.out.println("Cliente administrador asignado: "
                    + clientHandler.getClientSocket().getInetAddress().getHostAddress());
        }
    }

    private String getEthernetIp() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (isValidNetworkInterface(networkInterface)) {
                String ip = getSiteLocalAddress(networkInterface);
                if (ip != null) {
                    return ip;
                }
            }
        }
        return null;
    }

    private boolean isValidNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        return !networkInterface.isLoopback() && networkInterface.isUp();
    }

    private String getSiteLocalAddress(NetworkInterface networkInterface) {
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address.isSiteLocalAddress()) {
                return address.getHostAddress();
            }
        }
        return null;
    }

    public void sendUfoList() {
        String ufoListJson = gson.toJson(ufos);
        broadcastMessage("UFO_LIST " + ufoListJson);
    }

    public void sendSelectedUfoDesign() {
        broadcastMessage("UFO_IMAGE " + selectedUfoDesign);
        System.out.println("Selected UFO design sent to clients: " + selectedUfoDesign);
    }

    public void setClientModeOrder() {
        for (ClientHandler client : clients) {
            if (client != adminClient) {
                client.sendMessage("SET_CLIENT_MODE ");
            }
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void handleNumberOfUfos(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int numberOfUfos = Integer.parseInt(parts[2]);
            this.numberofUfos = numberOfUfos;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de Numero de Ufos inválido.");
        }
    }

    public void handleSelectedUfoDesign(String inputline) {
        try {
            System.out.println("handleSelectedUfoDesign - inputLine: " + inputline);
            String[] parts = inputline.split(" ");
            String selectedUfoDesign = parts[2];
            this.selectedUfoDesign = selectedUfoDesign;
            System.out.println("Selected UFO design set to: " + selectedUfoDesign);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de Diseño de Ufo inválido.");
        }
    }

    public void handleSpawnRate(String inputLine) {
        try {
            System.out.println("handleSpawnRate - inputLine: " + inputLine);
            String[] parts = inputLine.split(" ");
            int spawnRate = Integer.parseInt(parts[2]);
            this.spawnRate = spawnRate;
            System.out.println("Spawn rate set to: " + spawnRate);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de Aparicion inválido ");
            e.printStackTrace();
        }
    }

    public void handleSpeed(String inputLine) {
        try {
            System.out.println("handleSpeed - inputLine: " + inputLine);
            String[] parts = inputLine.split(" ");
            int speed = Integer.parseInt(parts[2]);
            this.speed = speed;
            System.out.println("Speed set to: " + speed);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de Velocidad inválido");
            e.printStackTrace();
        }
    }

    public void handleTrajectoryFromClient(String inputLine) {
        try {
            System.out.println("handleTrajectoryFromClient - inputLine: " + inputLine);
            String[] parts = inputLine.split(" ");
            String trajectoryJson = parts[2];
            ArrayList<Point> trajectoryPoints = gson.fromJson(trajectoryJson,
                    new com.google.gson.reflect.TypeToken<ArrayList<Point>>() {
                    }.getType());
            this.trajectoryPoints = trajectoryPoints;
            setSelectedTrajectory();
            System.out.println("Trajectory points set to: " + trajectoryPoints);
        } catch (Exception e) {
            System.out.println("Formato de Trayectoria Inválido.");
            e.printStackTrace();
        }
    }

    public void handleSelectedPointFromClient(String inputLine) {
        try {
            System.out.println("handleSelectedPointFromClient - inputLine: " + inputLine);
            String[] parts = inputLine.split(" ");
            int x = Integer.parseInt(parts[2]);
            int y = Integer.parseInt(parts[3]);
            this.selectedPoint = new Point(x, y);
            selectUfo(selectedPoint);
            System.out.println("Punto seleccionado recibido: " + selectedPoint);
        } catch (Exception e) {
            System.out.println("Formato de Punto selecionado inválido");
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Servidor detenido.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateUfoCountOrder(int size) {
        broadcastMessage("UPDATE_UFO_COUNT " + size);
    }

    public void playCrashSoundOrder() {
        broadcastMessage("PLAY_CRASH_SOUND ");
    }

    public void incrementCrashedUfoCountOrder(int crashedUfos) {
        broadcastMessage("INCREMENT_CRASHED_UFO_COUNT " + crashedUfos);
    }

    public void playLandingSoundOrder() {
        broadcastMessage("PLAY_LANDING_SOUND ");
    }

    public void incrementLandedUfoCountOrder() {
        broadcastMessage("INCREMENT_LANDED_UFO_COUNT ");
    }

    public void updateUfosOrder() {
        broadcastMessage("UPDATE_UFOS ");
    }

    public void updateConnectedPlayersOrder(int size) {
        broadcastMessage("UPDATE_CONNECTED_PLAYERS " + size);
    }

    public void forceStartGameOrder() {
        broadcastMessage("FORCE_START_GAME ");
    }

    private void selectUfo(Point point) {
        boolean ufoSelected = false;
        for (Ufo ufo : ufos) {
            if (isUfoSelected(ufo, point)) {
                selectUfo(ufo);
                ufoSelected = true;
            } else {
                deselectUfo(ufo);
            }
        }
        if (!ufoSelected) {
            System.out.println("No se seleccionó ningún UFO.");
        }
        updateUfosOrder();
    }

    private boolean isUfoSelected(Ufo ufo, Point point) {
        Rectangle bounds = new Rectangle(ufo.getPosition().x, ufo.getPosition().y, Ufo.UFO_WIDTH, Ufo.UFO_HEIGHT);
        return bounds.contains(point);
    }

    private void selectUfo(Ufo ufo) {
        ufo.setSelected(true);
        System.out.println("UFO seleccionado: " + ufo);
        trajectoryPoints.clear();
    }

    private void deselectUfo(Ufo ufo) {
        ufo.setSelected(false);
    }

    private void setSelectedTrajectory() {
        for (Ufo ufo : ufos) {
            if (ufo.isSelected()) {
                ufo.setTrajectory(trajectoryPoints);
                System.out.println("Trajectory points: " + trajectoryPoints);
            }
        }
    }
}
