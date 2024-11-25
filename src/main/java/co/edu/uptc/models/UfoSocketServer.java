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
            serverSocket = new ServerSocket(port);
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Servidor iniciado en la IP " + serverIp + " y puerto " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                if (adminClient == null) {
                    adminClient = clientHandler;
                    adminClient.setAdmin(true);
                    System.out.println("Cliente administrador asignado: " + clientSocket.getInetAddress().getHostAddress());
                }
                new Thread(clientHandler).start();
            }
        } catch (BindException e) {
            System.out.println("Error: El puerto " + port + " ya está en uso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUfoList() {
        String ufoListJson = gson.toJson(ufos);
        broadcastMessage("UFO_LIST " + ufoListJson);
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
            System.out.println("Formato de mensaje inválido.");
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
            System.out.println("Formato de mensaje inválido en handleSpawnRate.");
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
            System.out.println("Formato de mensaje inválido en handleSpeed.");
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
            System.out.println("Formato de mensaje inválido en handleTrajectoryFromClient.");
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
            System.out.println("Formato de mensaje inválido en handleSelectedPointFromClient.");
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
        broadcastMessage("PLAY_CRASH_SOUND");
    }

    public void incrementCrashedUfoCountOrder(int crashedUfos) {
        broadcastMessage("INCREMENT_CRASHED_UFO_COUNT " + crashedUfos);
    }

    public void playLandingSoundOrder() {
        broadcastMessage("PLAY_LANDING_SOUND");
    }

    public void incrementLandedUfoCountOrder() {
        broadcastMessage("INCREMENT_LANDED_UFO_COUNT");
    }

    public void updateUfosOrder() {
        broadcastMessage("UPDATE_UFOS");
    }

    public void incrementConnectedPlayersOrder() {
        broadcastMessage("INCREMENT_CONNECTED_PLAYERS");
    }

    private void selectUfo(Point point) {
        boolean ufoSelected = false;
        for (Ufo ufo : ufos) {
            Rectangle bounds = new Rectangle(ufo.getPosition().x, ufo.getPosition().y, Ufo.UFO_WIDTH, Ufo.UFO_HEIGHT);
            if (bounds.contains(point)) {
                ufo.setSelected(true);
                System.out.println("UFO seleccionado: " + ufo);
                trajectoryPoints.clear();
                ufoSelected = true;
            } else {
                ufo.setSelected(false);
            }
        }

        if (!ufoSelected) {
            System.out.println("No se seleccionó ningún UFO.");
        }

        updateUfosOrder();
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
