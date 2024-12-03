package co.edu.uptc.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private UfoSocketServer server;
    private boolean isAdmin;
    private ServerMethodMap methodMap;
    private String username;

    public ClientHandler(Socket socket, UfoSocketServer server, boolean isAdmin, String username) {
        this.clientSocket = socket;
        this.server = server;
        this.isAdmin = isAdmin;
        this.username = username;
        this.methodMap = new ServerMethodMap(server);
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            processClientInput();
        } catch (IOException e) {
            System.out.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            closeClientConnection();
        }
    }

    private void processClientInput() throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (!handleInput(inputLine)) {
                break;
            }
        }
    }

    private boolean handleInput(String inputLine) {
        if (inputLine.contains("DISCONNECT")) {
            return false;
        } else {
            String[] keys = { "NUMBER_OF_UFOS", "SPAWN_RATE", "SPEED", "REQUEST_UFO_LIST", "UFO_TRAJECTORY",
                    "SELECTED_POINT", "REQUEST_UFO_DESIGN", "REQUEST_USERS_LIST" };
            for (String key : keys) {
                if (inputLine.contains(key)) {
                    methodMap.run(key, inputLine);
                    return true;
                }
            }
        }
        return false;
    }

    private void closeClientConnection() {
        try {
            clientSocket.close();
            server.updateConnectedPlayersOrder(-1);
            server.removeUsernameFromList(username);
            server.updateUserNameListOrder();
            System.out.println("Conexión con el cliente cerrada.");
        } catch (IOException e) {
            System.out.println("Error al cerrar el socket del cliente: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
