package co.edu.uptc.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import lombok.Setter;

@Setter
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private UfoSocketServer server;
    private boolean isAdmin;
    private ServerMethodMap methodMap;

    public ClientHandler(Socket socket, UfoSocketServer server, boolean isAdmin) {
        this.clientSocket = socket;
        this.server = server;
        this.isAdmin = isAdmin;
        this.methodMap = new ServerMethodMap(server);
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    methodMap.run("START_GAME", inputLine);
                } else if (isAdmin && inputLine.contains("UFO_IMAGE")) {
                    server.handleSelectedUfoDesign(inputLine);
                } else if (!isAdmin && inputLine.contains("CHECK_CLIENT_MODE")) {
                    server.setClientModeOrder();
                } else {
                    String[] keys = {"NUMBER_OF_UFOS", "SPAWN_RATE", "SPEED", "REQUEST_UFO_LIST", "UFO_TRAJECTORY", "SELECTED_POINT", "REQUEST_UFO_DESIGN"};
                    boolean handled = false;
                    for (String key : keys) {
                        if (inputLine.contains(key)) {
                            methodMap.run(key, inputLine);
                            handled = true;
                            break;
                        }
                    }
                    if (!handled) {
                        sendMessage("Eco: " + inputLine);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                server.updateConnectedPlayersOrder(-1);
                System.out.println("Conexión con el cliente cerrada.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
