package co.edu.uptc.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import co.edu.uptc.pojos.Ufo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UfoSocketServer {
    private ServerSocket serverSocket;
    private UfoRunner ufoRunner;
    private UfoSpawnRunner spawnRunner;
    private List<Ufo> Ufos;
    private int spawnRate;
    private int speed;
    private int numberofUfos;

    public UfoSocketServer() {
        this.Ufos = new CopyOnWriteArrayList<>();
        ufoRunner = new UfoRunner(this);
        spawnRunner = new UfoSpawnRunner(this);
    }

    public synchronized void addUfo(int speed) {
        UfoController ufoController = new UfoController(this);
        Ufo newUfo = ufoController.createUfo(speed);
        Ufos.add(newUfo);
        System.out.println("UFO añadido con velocidad: " + speed);
    }

    public void startGame() {
        Thread moveThread = new Thread(ufoRunner);
        moveThread.start();
        Thread spawnThread = new Thread(spawnRunner);
        spawnThread.start();
        System.out.println("Juego iniciado.");
    }

    public synchronized void moveAll() {
        Iterator<Ufo> iterator = Ufos.iterator();
        while (iterator.hasNext()) {
            Ufo ufo = iterator.next();
            UfoController ufoController = new UfoController(this);
            ufoController.moveUfo(ufo, Ufos);
        }
        System.out.println("Todos los UFOs movidos.");
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
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibido: " + inputLine);
                if (inputLine.contains("NUMBER_OF_UFOS")) {
                    handleNumberOfUfos(inputLine);
                } else if (inputLine.contains("SPAWN_RATE")) {
                    handleSpawnRate(inputLine);
                } else if (inputLine.contains("SPEED")) {
                    handleSpeed(inputLine);
                } else {
                    out.println("Eco: " + inputLine);
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

    private void handleNumberOfUfos(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int numberOfUfos = Integer.parseInt(parts[2]);
            this.numberofUfos = numberOfUfos;
            System.out.println("Número de ovnis asignados: " + this.numberofUfos);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de mensaje inválido.");
        }
    }

    private void handleSpawnRate(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int spawnRate = Integer.parseInt(parts[2]);
            this.spawnRate = spawnRate;
            System.out.println("Tasa de aparición asignada: " + this.spawnRate);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Formato de mensaje inválido.");
        }
    }

    private void handleSpeed(String inputLine) {
        try {
            String[] parts = inputLine.split(" ");
            int speed = Integer.parseInt(parts[2]);
            this.speed = speed;
            System.out.println("Velocidad asignada: " + this.speed);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
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
        System.out.println("Actualizando el conteo de UFOs a: " + size);
        // Implementación
    }

    public void playCrashSoundOrder() {
        System.out.println("Reproduciendo sonido de choque.");
        // Implementación
    }

    public void incrementCrashedUfoCountOrder(int crashedUfos) {
        System.out.println("Incrementando el conteo de UFOs estrellados a: " + crashedUfos);
        // Implementación
    }

    public void playLandingSoundOrder() {
        System.out.println("Reproduciendo sonido de aterrizaje.");
        // Implementación
    }

    public void incrementLandedUfoCountOrder() {
        // Implementación
    }

    public void updateUfosOrder() {
        System.out.println("Actualizando UFOs.");
        // Implementación
    }
}
