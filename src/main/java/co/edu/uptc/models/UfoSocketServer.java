package co.edu.uptc.models;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
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
    }

    public void startGame() {
        Thread moveThread = new Thread(ufoRunner);
        moveThread.start();
        Thread spawnThread = new Thread(spawnRunner);
        spawnThread.start();
    }

    public synchronized void moveAll() {
        Iterator<Ufo> iterator = Ufos.iterator();
        while (iterator.hasNext()) {
            Ufo ufo = iterator.next();
            UfoController ufoController = new UfoController(this);
            ufoController.moveUfo(ufo,Ufos);
            //Todo: presenter.updateUfoCount(Ufos.size());
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
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             Scanner in = new Scanner(clientSocket.getInputStream())) {

            while (in.hasNextLine()) {
                String inputLine = in.nextLine();
                System.out.println("Recibido: " + inputLine);
                out.println("Eco: " + inputLine);
            }
        } catch (NoSuchElementException e) {
            System.out.println("El cliente cerró la conexión.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
