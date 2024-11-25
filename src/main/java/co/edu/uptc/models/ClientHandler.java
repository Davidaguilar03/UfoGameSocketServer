package co.edu.uptc.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private UfoSocketServer server;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isAdmin;

    public ClientHandler(Socket socket, UfoSocketServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.isAdmin = false;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        String inputLine;
        try {
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibido: " + inputLine);
                if (isAdmin && inputLine.contains("START_GAME")) {
                    server.startGame();
                    server.forceStartGameOrder();
                    server.incrementConnectedPlayersOrder(server.getClients().size());
                }
                if (inputLine.contains("NUMBER_OF_UFOS")) {
                    server.handleNumberOfUfos(inputLine);
                } else if (inputLine.contains("SPAWN_RATE")) {
                    server.handleSpawnRate(inputLine);
                } else if (inputLine.contains("SPEED")) {
                    server.handleSpeed(inputLine);
                } else if (inputLine.contains("REQUEST_UFO_LIST")) {
                    server.sendUfoList();
                } else if (inputLine.contains("UFO_TRAJECTORY")) {
                    server.handleTrajectoryFromClient(inputLine);
                } else if (inputLine.contains("SELECTED_POINT")) {
                    server.handleSelectedPointFromClient(inputLine);
                } else {
                    sendMessage("Eco: " + inputLine);
                }
            }
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
}
