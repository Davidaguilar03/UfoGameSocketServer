package co.edu.uptc.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
    private PrintWriter clientOut;
    private Gson gson;

    public UfoSocketServer() {
        this.ufos = new CopyOnWriteArrayList<>();
        ufoRunner = new UfoRunner(this);
        spawnRunner = new UfoSpawnRunner(this);
        this.trajectoryPoints = new ArrayList<>();
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
                handleClient(clientSocket);
            }
        } catch (BindException e) {
            System.out.println("Error: El puerto " + port + " ya está en uso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibido: " + inputLine);
                if (inputLine.contains("NUMBER_OF_UFOS")) {
                    handleNumberOfUfos(inputLine);
                } else if (inputLine.contains("SPAWN_RATE")) {
                    handleSpawnRate(inputLine);
                } else if (inputLine.contains("SPEED")) {
                    handleSpeed(inputLine);
                } else if (inputLine.contains("START_GAME")) {
                    startGame();
                } else if (inputLine.contains("REQUEST_UFO_LIST")) {
                    sendUfoList();
                } else if (inputLine.contains("UFO_TRAJECTORY")) {
                    handleTrajectoryFromClient(inputLine);
                } else if (inputLine.contains("SELECTED_POINT")) {
                    handleSelectedPointFromClient(inputLine);
                } else {
                    clientOut.println("Eco: " + inputLine);
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("El cliente cerró la conexión.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Conexión con el cliente cerrada.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendUfoList() {
        String ufoListJson = gson.toJson(ufos);
        clientOut.println("UFO_LIST " + ufoListJson);
    }

    private void handleNumberOfUfos(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int numberOfUfos = Integer.parseInt(parts[2]);
            this.numberofUfos = numberOfUfos;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de mensaje inválido.");
        }
    }

    private void handleSpawnRate(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int spawnRate = Integer.parseInt(parts[2]);
            this.spawnRate = spawnRate;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de mensaje inválido.");
        }
    }

    private void handleSpeed(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int speed = Integer.parseInt(parts[2]);
            this.speed = speed;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de mensaje inválido.");
        }
    }

    public void handleTrajectoryFromClient(String inputline) {
        try {
            String[] parts = inputline.split(" ");
            String trajectoryJson = parts[2];
            ArrayList<Point> trajectoryPoints = gson.fromJson(trajectoryJson,
                    new com.google.gson.reflect.TypeToken<ArrayList<Point>>() {
                    }.getType());
            this.trajectoryPoints = trajectoryPoints;
            setSelectedTrayectory();
        } catch (Exception e) {
            System.out.println("Formato de mensaje inválido.");
        }
    }

    public void handleSelectedPointFromClient(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int x = Integer.parseInt(parts[2]);
            int y = Integer.parseInt(parts[3]);
            this.selectedPoint = new Point(x, y);
            selectUfo(selectedPoint);
            System.out.println("Punto seleccionado recibido: " + selectedPoint);
        } catch (Exception e) {
            System.out.println("Formato de mensaje inválido.");
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
        if (clientOut != null) {
            clientOut.println("UPDATE_UFO_COUNT " + size);
        } else {
            System.out.println("No hay cliente conectado para enviar la orden.");
        }
    }

    public void playCrashSoundOrder() {
        if (clientOut != null) {
            clientOut.println("PLAY_CRASH_SOUND");
        } else {
            System.out.println("No hay cliente conectado para enviar la orden.");
        }
    }

    public void incrementCrashedUfoCountOrder(int crashedUfos) {
        if (clientOut != null) {
            clientOut.println("INCREMENT_CRASHED_UFO_COUNT " + crashedUfos);
        } else {
            System.out.println("No hay cliente conectado para enviar la orden.");
        }
    }

    public void playLandingSoundOrder() {
        if (clientOut != null) {
            clientOut.println("PLAY_LANDING_SOUND");
        } else {
            System.out.println("No hay cliente conectado para enviar la orden.");
        }
    }

    public void incrementLandedUfoCountOrder() {
        if (clientOut != null) {
            clientOut.println("INCREMENT_LANDED_UFO_COUNT");
        } else {
            System.out.println("No hay cliente conectado para enviar la orden.");
        }
    }

    public void updateUfosOrder() {
        if (clientOut != null) {
            clientOut.println("UPDATE_UFOS");
        } else {
            System.out.println("No hay cliente conectado para enviar la orden.");
        }
    }

    private void selectUfo(Point point) {
        for (Ufo ufo : ufos) {
            Rectangle bounds = new Rectangle(ufo.getPosition().x, ufo.getPosition().y, Ufo.UFO_WIDTH, Ufo.UFO_HEIGHT);
            if (bounds.contains(point)) {
                ufo.setSelected(true);
                trajectoryPoints.clear();
            } else {
                ufo.setSelected(false);
            }
        }
        updateUfosOrder();
    }

    private void setSelectedTrayectory() {
        for (Ufo ufo : ufos) {
            if (ufo.isSelected()) {
                ufo.setTrajectory(trajectoryPoints);
            }
        }
    }
}
