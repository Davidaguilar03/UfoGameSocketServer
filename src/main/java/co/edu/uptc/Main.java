package co.edu.uptc;

import co.edu.uptc.models.UfoSocketServer;

public class Main {
    public static void main(String[] args) {
        // if (!validateArgs(args)) {
        //     System.exit(1);
        // }
        // int port = parsePort(args[0]);
        // if (port == -1) {
        //     System.exit(1);
        // }
        startServer(1234);
    }

    private static boolean validateArgs(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Main <puerto>");
            return false;
        }
        return true;
    }

    private static int parsePort(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            System.out.println("Número de puerto inválido.");
            return -1;
        }
    }

    private static void startServer(int port) {
        UfoSocketServer server = new UfoSocketServer();
        server.startServer(port);
    }
}
